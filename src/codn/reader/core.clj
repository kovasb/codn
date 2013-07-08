(ns codn.reader.core
  (:refer-clojure :exclude [read read-line read-string char
                           ])
 ;; (:use clojure.walk)
  (:use codn.parser.reader-types
        [codn.parser utils commons])
  (:import (clojure.lang PersistentHashSet IMeta
                         RT Symbol Reflector Var IObj
                         PersistentVector IRecord Namespace)
           java.lang.reflect.Constructor))

(defn- resolve-ns [sym]
  (or ((ns-aliases *ns*) sym)
      (find-ns sym)))

(defn- garg [n]
  (symbol (str (if (== -1 n) "rest" (str "p" n))
               "__" (RT/nextID) "#")))

(declare read-symbol)

(def ^:private ^:dynamic gensym-env nil)


(declare syntax-quote*)

(defn- unquote-splicing? [form]
  (and (seq? form)
       (= (first form) 'clojure.core/unquote-splicing)))

(defn- unquote? [form]
  (and (seq? form)
       (= (first form) 'clojure.core/unquote)))

(defn- expand-list [s]
  (loop [s (seq s) r (transient [])]
    (if s
      (let [item (first s)
            ret (conj! r
                       (cond
                        (unquote? item)          (list 'clojure.core/list (second item))
                        (unquote-splicing? item) (second item)
                        :else                    (list 'clojure.core/list (syntax-quote* item))))]
        (recur (next s) ret))
      (seq (persistent! r)))))


(defn- flatten-map [form]
  (loop [s (seq form) key-vals (transient [])]
    (if s
      (let [e (first s)]
        (recur (next s) (-> key-vals
                            (conj! (key e))
                            (conj! (val e)))))
      (seq (persistent! key-vals)))))

(defn- register-gensym [sym]
  (if-not gensym-env
    (throw (IllegalStateException. "Gensym literal not in syntax-quote")))
  (or (get gensym-env sym)
      (let [gs (symbol (str (subs (name sym)
                                  0 (dec (count (name sym))))
                            "__" (RT/nextID) "__auto__"))]
        (set! gensym-env (assoc gensym-env sym gs))
        gs)))

(defn- resolve-symbol [s]
  (if (pos? (.indexOf (name s) "."))
    s
    (if-let [ns-str (namespace s)]
      (let [^Namespace ns (resolve-ns (symbol ns-str))]
        (if (or (nil? ns)
                (= (name (ns-name ns)) ns-str)) ;; not an alias
          s
          (symbol (name (.name ns)) (name s))))
      (if-let [o ((ns-map *ns*) s)]
        (if (class? o)
          (symbol (.getName ^Class o))
          (if (var? o)
            (symbol (-> ^Var o .ns .name name) (-> ^Var o .sym name))))
        (symbol (name (ns-name *ns*)) (name s))))))

(defn- add-meta [form ret]
  (if (and (instance? IObj form)
           (dissoc (meta form) :line :column))
    (list 'clojure.core/with-meta ret (syntax-quote* (meta form)))
    ret))

(defn- syntax-quote-coll [type coll]
  (let [res (list 'clojure.core/seq
                  (cons 'clojure.core/concat
                        (expand-list coll)))]
    (if type
      (list 'clojure.core/apply type res)
      res)))


(defn- syntax-quote? [form]
  (and (seq? form)
       (= (first form) 'read-syntax-quote*)))

(declare read-syntax-quote*)

(defn- syntax-quote* [form]
  (->>
   (cond
    (special-symbol? form) (list 'quote form)

    (symbol? form)
    (list 'quote
          (if (namespace form)
            (let [maybe-class ((ns-map *ns*)
                               (symbol (namespace form)))]
              (if (class? class)
                (symbol (.getName ^Class maybe-class) (name form))
                (resolve-symbol form)))
            (let [sym (name form)]
              (cond
               (.endsWith sym "#")
               (register-gensym form)

               (.startsWith sym ".")
               form

               (.endsWith sym ".")
               (let [csym (symbol (subs sym 0 (dec (count sym))))]
                 (symbol (.concat (name (resolve-symbol csym)) ".")))
               :else (resolve-symbol form)))))

  (syntax-quote? form) (read-syntax-quote* (second form))
  (unquote? form) (second form)
    (unquote-splicing? form) (throw (IllegalStateException. "splice not in list"))

    (coll? form)
    (cond
     (instance? IRecord form) form
     (map? form) (syntax-quote-coll 'clojure.core/hash-map (flatten-map form))
     (vector? form) (syntax-quote-coll 'clojure.core/vector form)
     (set? form) (syntax-quote-coll 'clojure.core/hash-set form)
     (or (seq? form) (list? form))
     (let [seq (seq form)]
       (if seq
         (syntax-quote-coll nil seq)
         '(clojure.core/list)))
     :else (throw (UnsupportedOperationException. "Unknown Collection type")))

    (or (keyword? form)
        (number? form)
        (char? form)
        (string? form))
    form

    :else (list 'quote form))
   (add-meta form)))

(defn- read-syntax-quote*
  [form]
  (binding [gensym-env {}]
    (syntax-quote* (second form))))


(defn read-tagged-literal [[tag value]]
  (if-let [f (*data-readers* tag)]
    (f value)
    (if-let [f (default-data-readers tag)]
      (f value)
      (if *default-data-reader-fn*
        (*default-data-reader-fn* value)
        (throw (Exception. "No reader function for tag ") (name tag))))))


(defn read-constructor [[class-name value]]

  (RT/baseLoader)
  (let [ class (if (class? class-name) class-name (Class/forName (name class-name) ))]
    (if (vector? value)
      (do  (def xxx [class value])
           (Reflector/invokeConstructor class (to-array value)))
      (if (map? value )
        (Reflector/invokeStaticMethod class "create" (object-array [value]))
        (throw (Exception. "constructor value not a vector or map."))))))


(defn read-autoresolved-keyword [kw]
  (if (namespace kw)
    (keyword (str (resolve-ns (symbol (namespace kw)))) (name kw))
    (keyword (str *ns*) (name kw))))



(defn make-expr [head body]
  (condp = head
    :nil nil
    :list (apply list body)
    :vector (vec body)
    :map (apply hash-map body)
    :set (set body)
    :fn (list 'read-fn (first body))
    :deref (list 'clojure.core/deref (first body))
    :var-quote (list 'var (first body))
    :quote (list 'quote (first body))
    :unquote (list 'clojure.core/unquote (first body))
    :unquote-splicing (list 'clojure.core/unquote-splicing (first body))
    :syntax-quote (list 'read-syntax-quote* (first body))
    :read-eval (eval (first body))
    :tagged-literal (read-tagged-literal body)
    :constructor (read-constructor body)
    :autoresolved-keyword (read-autoresolved-keyword (first body))
    :regex (re-pattern (first body))
    :ratio (apply / body)
    :meta (with-meta (second body) (desugar-meta (first body)))
    (throw (Exception. (str "Unknown codn head: " head)))))


(defn un-edn [x]
  (if (not (#{clojure.lang.PersistentArrayMap clojure.lang.PersistentHashMap} (class x)))
    x
    (if (:value x)
      (:value x)
      (make-expr (:head x) (:body x)))))

(defn un-syntax-quote [x]
  (if ( syntax-quote? x) (read-syntax-quote* x) x))


(defn get-slot [s]
  (if (= '%& s) :rest (Integer/parseInt (subs (name s) 1))))


(defn- garg2 [n]
  (symbol (str (if (= :rest n) "rest" (str "p" n))
               "__" (RT/nextID) "#")))

(defn match-head? [form head]
  (and (seq? form)
       (= (first form) head)))

(defn arg? [x] (and (symbol? x) (= "%" (subs (name x) 0 1)))) ;; bad

(declare fn-slots)

(defn remap-arg [fn-slots arg]
  (let [s (get-slot ({'% '%1} arg arg))]
    (if (@fn-slots s)
     (@fn-slots s)
     (let [arg2 (garg2 s)] (swap! fn-slots assoc s arg2) arg2))))

(defn build-fn-args [slots]
  (vec (concat
        (let [positional-slots (map first (dissoc slots :rest))]
          (if (not (empty? positional-slots))
            (map #(slots % (garg2 %)) (range 1 (+ 1 (apply max positional-slots))))
            []))
        (if (slots :rest) ['& (slots :rest)] []))))

(defn un-fn-postwalk [x fn-slots]
  (cond
   (arg? x) (remap-arg fn-slots x)
   (match-head? x 'read-fn)
   (let [slots @fn-slots]
     (list
      'fn*
      (build-fn-args slots)
       (second x)))
   true x))

(defn un-fn-prewalk [x fn-slots]
  (if (match-head? x 'read-fn)
    (reset! fn-slots {}))
  x)



(defn walk
  [inner outer form]
  (cond
   ;;(list? form) (outer (apply list (map inner form)))
   (list? form) (outer (into (empty form) (reverse (map inner form))))

   (instance? clojure.lang.IRecord form) (outer (read-constructor [(class form) (into {} (map inner form))]))
   (instance? clojure.lang.IMapEntry form) (outer (vec (map inner form)))
   (seq? form) (outer (doall (map inner form)))
   (coll? form) (outer (into (empty form) (map inner form)))
   :else (outer form)))

(defn postwalk
  [f form]
  (walk (partial postwalk f) f form))

(defn prewalk
  [f form]
  (walk (partial prewalk f) identity (f form)))

(defn read-codn [expr]
  (let [fn-slots (atom {})]
    (prewalk
     un-syntax-quote
     (postwalk
           #(un-fn-postwalk % fn-slots)
           (postwalk un-edn expr)))))
