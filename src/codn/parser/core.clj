(ns ^{:doc "A clojure reader in clojure"
      :author "Bronsa"}
  codn.parser.core
  (:refer-clojure :exclude [read read-line read-string char
                            default-data-readers *default-data-reader-fn*
                            *read-eval* *data-readers*])
  (:use codn.parser.reader-types
        [codn.parser utils commons])
  (:import (clojure.lang PersistentHashSet IMeta
                         RT Symbol Reflector Var IObj
                         PersistentVector IRecord Namespace)
           java.lang.reflect.Constructor))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; helpers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(declare parse macros dispatch-macros
         ^:dynamic *read-eval*
         ^:dynamic *data-readers*
         ^:dynamic *default-data-reader-fn*
         default-data-readers)

(defn- wrapping-reader2
  [sym]
  (fn [rdr _]
    {:head sym :body [(parse rdr true nil true)]}))


(defn- macro-terminating? [ch]
  (case ch
    (\" \; \@ \^ \` \~ \( \) \[ \] \{ \} \\) true
    false))

(defn- ^String read-token
  [rdr initch]
  (if-not initch
    (reader-error rdr "EOF while reading")
    (loop [sb (doto (StringBuilder.) (.append initch))
           ch (read-char rdr)]
      (if (or (whitespace? ch)
              (macro-terminating? ch)
              (nil? ch))
        (do (unread rdr ch)
            (str sb))
        (recur (.append sb ch) (read-char rdr))))))

(declare read-tagged)

(defn- read-dispatch
  [rdr _]
  (if-let [ch (read-char rdr)]
    (if-let [dm (dispatch-macros ch)]
      (dm rdr ch)
      (if-let [obj (read-tagged (doto rdr (unread ch)) ch)] ;; ctor reader is implemented as a taggged literal
        obj
        (reader-error rdr "No dispatch macro for " ch)))
    (reader-error rdr "EOF while reading character")))

(defn- read-unmatched-delimiter
  [rdr ch]
  (reader-error rdr "Unmatched delimiter " ch))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; readers
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn- read-unicode-char
  ([^String token offset length base]
     (let [l (+ offset length)]
       (when-not (== (count token) l)
         (throw (IllegalArgumentException. (str "Invalid unicode character: \\" token))))
       (loop [i offset uc 0]
         (if (== i l)
           (char uc)
           (let [d (Character/digit (int (nth token i)) (int base))]
             (if (== d -1)
               (throw (IllegalArgumentException. (str "Invalid digit: " (nth token i))))
               (recur (inc i) (long (+ d (* uc base))))))))))

  ([rdr initch base length exact?]
     (loop [i 1 uc (Character/digit (int initch) (int base))]
       (if (== uc -1)
         (throw (IllegalArgumentException. (str "Invalid digit: " initch)))
         (if-not (== i length)
           (let [ch (peek-char rdr)]
             (if (or (whitespace? ch)
                     (macros ch)
                     (nil? ch))
               (if exact?
                 (throw (IllegalArgumentException.
                         (str "Invalid character length: " i ", should be: " length)))
                 (char uc))
               (let [d (Character/digit (int ch) (int base))]
                 (read-char rdr)
                 (if (== d -1)
                   (throw (IllegalArgumentException. (str "Invalid digit: " ch)))
                   (recur (inc i) (long (+ d (* uc base))))))))
           (char uc))))))

(def ^:private ^:const upper-limit (int \uD7ff))
(def ^:private ^:const lower-limit (int \uE000))

(defn- read-char*
  [rdr backslash]
  (let [ch (read-char rdr)]
    (if-not (nil? ch)
      (let [token (read-token rdr ch)
            token-len (count token)]
        (let [result
              (cond

          (== 1 token-len)  (Character/valueOf (nth token 0))

          (= token "newline") \newline
          (= token "space") \space
          (= token "tab") \tab
          (= token "backspace") \backspace
          (= token "formfeed") \formfeed
          (= token "return") \return

          (.startsWith token "u")
          (let [c (read-unicode-char token 1 4 16)
                ic (int c)]
            (if (and (> ic upper-limit)
                     (< ic lower-limit))
              (reader-error rdr "Invalid character constant: \\u" (Integer/toString ic 16))
              c))

          (.startsWith token "x")
          (read-unicode-char token 1 2 16)

          (.startsWith token "o")
          (let [len (dec token-len)]
            (if (> len 3)
              (reader-error rdr "Invalid octal escape sequence length: " len)
              (let [uc (read-unicode-char token 1 len 8)]
                (if (> (int uc) 0377)
                  (reader-error rdr "Octal escape sequence must be in range [0, 377]")
                  uc))))

          :else (reader-error rdr "Unsupported character: \\" token))]
          {:head :character :value result}
          ))
      (reader-error rdr "EOF while reading character"))))

(defn- ^PersistentVector read-delimited
  [delim rdr recursive?]
  (let [first-line (when (indexing-reader? rdr)
                     (get-line-number rdr))
        delim (char delim)]
    (loop [a (transient [])]
      (if-let [ch (read-past whitespace? rdr)]
        (if (identical? delim (char ch))
          (persistent! a)
          (if-let [macrofn (macros ch)]
            (let [mret (macrofn rdr ch)]
              (recur (if-not (identical? mret rdr) (conj! a mret) a)))
            (let [o (parse (doto rdr (unread ch)) true nil recursive?)]
              (recur (if-not (identical? o rdr) (conj! a o) a)))))
        (reader-error rdr "EOF while reading"
                      (when first-line
                        (str ", starting at line" first-line)))))))

(defn- read-list
  [rdr _]
  (let [[line column] (when (indexing-reader? rdr)
                        [(get-line-number rdr) (int (dec (get-column-number rdr)))])
        the-list (read-delimited \) rdr true)]
    (if (empty? the-list)
      {:head :list :body []} ;; changed
      (with-meta
        {:head :list :body (vec the-list)} ;; changed
        (when line
          {:line line :column column})))))

(defn- read-vector
  [rdr _]
  (let [[line column] (when (indexing-reader? rdr)
                        [(get-line-number rdr) (int (dec (get-column-number rdr)))])
        the-vector (read-delimited \] rdr true)]
    (with-meta {:head :vector :body the-vector} ;; changed
      (when line
        {:line line :column column}))))

(defn- read-map
  [rdr _]
  (let [[line column] (when (indexing-reader? rdr)
                        [(get-line-number rdr) (int (dec (get-column-number rdr)))])
        the-map (read-delimited \} rdr true)
        map-count (count the-map)]
    (when (odd? map-count)
      (reader-error rdr "Map literal must contain an even number of forms"))
    (with-meta
      (if (zero? map-count)
        {:head :map :body []}
        {:head :map :body (vec the-map)}) ;; changed
      (when line
        {:line line :column column}))))

(defn- read-number
  [reader initch]
  (loop [sb (doto (StringBuilder.) (.append initch))
         ch (read-char reader)]
    (if (or (whitespace? ch) (macros ch) (nil? ch))
      (let [s (str sb)]
        (unread reader ch)
        (let [n (match-number s)]
          (if n
            (cond
             ;;(decimal? n) {:head :decimal :value n}
             (or (decimal? n) (float? n)) {:head :float :value n}
             (integer? n) {:head :integer :value n}
             (ratio? n) {:head :ratio :body [{:head :integer :value (numerator n)} {:head :integer :value (denominator n)}]}
             )
           (reader-error reader "Invalid number format [" s "]"))))
      (recur (doto sb (.append ch)) (read-char reader)))))

(defn- escape-char [sb rdr]
  (let [ch (read-char rdr)]
    (case ch
      \t "\t"
      \r "\r"
      \n "\n"
      \\ "\\"
      \" "\""
      \b "\b"
      \f "\f"
      \u (let [ch (read-char rdr)]
           (if (== -1 (Character/digit (int ch) 16))
             (reader-error rdr "Invalid unicode escape: \\u" ch)
             (read-unicode-char rdr ch 16 4 true)))
      \x (let [ch (read-char rdr)]
           (if (== -1 (Character/digit (int ch) 16))
             (reader-error rdr "Invalid unicode escape: \\x" ch)
             (read-unicode-char rdr ch 16 2 true)))
      (if (numeric? ch)
        (let [ch (read-unicode-char rdr ch 8 3 false)]
          (if (> (int ch) 0337)
            (reader-error rdr "Octal escape sequence must be in range [0, 377]")
            ch))
        (reader-error rdr "Unsupported escape character: \\" ch)))))

(defn- read-string*
  [reader _]
  (loop [sb (StringBuilder.)
         ch (read-char reader)]
    (case ch
      nil (reader-error reader "EOF while reading string")
      \\ (recur (doto sb (.append (escape-char sb reader)))
                (read-char reader))
      \" {:head :string :value (str sb)}
      (recur (doto sb (.append ch)) (read-char reader)))))

(defn- read-symbol
  [rdr initch]
  (when-let [token (read-token rdr initch)]
    (let [[line column] (when (indexing-reader? rdr)
                          [(get-line-number rdr) (int (dec (get-column-number rdr)))])]
      (case token

        ;; special symbols
        "nil" {:head :nil :value nil}
        "true" {:head :boolean :value true}
        "false" {:head :boolean :value false}
        "/"  {:head :symbol :value '/}
        "NaN" {:head :NaN :value Double/NaN}
        "-Infinity" {:head :negative-infinity :value Double/NEGATIVE_INFINITY}
        ("Infinity" "+Infinity") {:head :positive-infinity :value Double/POSITIVE_INFINITY}

        (or (when-let [p (parse-symbol token)]
              (with-meta {:head :symbol :value (symbol (p 0) (p 1))}
                (when line
                  {:line line :column column})))
            (reader-error rdr "Invalid token: " token))))))

(defn- resolve-ns [sym]
  (or ((ns-aliases *ns*) sym)
      (find-ns sym)))


(defn- read-keyword
  [reader initch]
  (let [ch (read-char reader)]
    (if-not (whitespace? ch)
      (let [token (read-token reader ch)
            s (parse-symbol token)]
        (if s
          (let [^String ns (s 0)
                ^String name (s 1)]
            (if (identical? \: (nth token 0))
              (if ns
                {:head :autoresolved-keyword :body [{:head :keyword :value (keyword (subs ns 1) name)}] }
                {:head :autoresolved-keyword :body [{:head :keyword :value (keyword  (subs name 1))}]})
              {:head :keyword :value (keyword ns name)}))
          (reader-error reader "Invalid token: :" token)))
      (reader-error reader "Invalid token: :"))))

(defn- wrapping-reader
  [sym]
  (fn [rdr _]
    (list sym (parse rdr true nil true))))

(defn- read-meta
  [rdr _]
  (let [[line column] (when (indexing-reader? rdr)
                        [(get-line-number rdr) (int (dec (get-column-number rdr)))])
        m (desugar-meta (parse rdr true nil true))]
    (when-not (map? m)
      (reader-error rdr "Metadata must be Symbol, Keyword, String or Map"))
    (let [o (parse rdr true nil true)]
      (if (instance? IMeta o)
        (let [m (if (and line
                         (seq? o))
                  (assoc m :line line
                           :column column)
                  m)]
          (if (instance? IObj o)
            {:head :meta :body [m o]}
            {:head :meta :body [m o]}))
         {:head :meta :body [m o]}))))


(defn- read-set
  [rdr _]
  {:head :set :body (vec (read-delimited \} rdr true))}
  )

(defn- read-discard
  [rdr _]
  (parse rdr true nil true)
  rdr)

(def ^:private ^:dynamic arg-env)


(defn- read-fn
  [rdr _]
  (if (thread-bound? #'arg-env)
    (throw (IllegalStateException. "Nested #()s are not allowed")))
  (binding [arg-env (sorted-map)]
    (let [form (parse (doto rdr (unread \()) true nil true)]
      {:head :fn :body [form]})))


 ;; should never hit this

(declare read-symbol)


(defn- read-unquote
  [rdr comma]
  (if-let [ch (peek-char rdr)]
    (if (identical? \@ ch)
      ((wrapping-reader2 :unquote-splicing) (doto rdr read-char) \@)
      ((wrapping-reader2 :unquote) rdr \~))))


(defn- macros [ch]
  (case ch
    \" read-string*
    \: read-keyword
    \; read-comment
    \' (wrapping-reader2 :quote)
    \@ (wrapping-reader2 :deref)
    \^ read-meta
    \` (wrapping-reader2 :syntax-quote)
    \~ read-unquote
    \( read-list
    \) read-unmatched-delimiter
    \[ read-vector
    \] read-unmatched-delimiter
    \{ read-map
    \} read-unmatched-delimiter
    \\ read-char*
    \# read-dispatch
    nil))


(defn read-regex2
  [rdr ch]
  (let [sb (StringBuilder.)]
    (loop [ch (read-char rdr)]
      (if (identical? \" ch)
        {:head :regex :body [(str sb)]}
        (if (nil? ch)
          (reader-error rdr "EOF while reading regex")
          (do
            (.append sb ch )
            (when (identical? \\ ch)
              (let [ch (read-char rdr)]
                (if (nil? ch)
                  (reader-error rdr "EOF while reading regex"))
                (.append sb ch)))
            (recur (read-char rdr))))))))

(defn- dispatch-macros [ch]
  (case ch
    \^ read-meta                ;deprecated
    \' (wrapping-reader2 :var-quote)
    \( read-fn
    \= (wrapping-reader2 :read-eval) ;; read-eval
    \{ read-set
    \< (throwing-reader "Unreadable form")
    \" read-regex2
    \! read-comment
    \_ read-discard
    nil))


(defn- read-tagged [rdr initch]
  (let [tag (parse rdr true nil false)]
    (if-not (symbol? (:value tag))
      (reader-error rdr "Reader tag must be a symbol"))
    (if (.contains (name (:value tag)) ".")
      {:head :constructor :body [tag (parse rdr true nil true)]}
      {:head :tagged-literal :body [tag (parse rdr true nil true)]})))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Public API
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


(defn parse
  "Parses the first object from an IPushbackReader or a java.io.PushbackReader."
  ([] (parse *in*))
  ([reader] (parse reader true nil))
  ([reader eof-error? sentinel] (parse reader eof-error? sentinel false))
  ([reader eof-error? sentinel recursive?]
     (try
       (let [ch (read-char reader)]
         (cond
          (whitespace? ch) (parse reader eof-error? sentinel recursive?)
          (nil? ch) (if eof-error? (reader-error reader "EOF") sentinel)
          (number-literal? reader ch) (read-number reader ch)
          (comment-prefix? ch) (parse (read-comment reader ch) eof-error? sentinel recursive?)
          :else (let [f (macros ch)]
                  (if f
                    (let [res (f reader ch)]
                      (if (identical? res reader)
                        (parse reader eof-error? sentinel recursive?)
                        res))
                    (read-symbol reader ch)))))
       (catch Exception e
         (if (ex-info? e)
           (throw e)
           (throw (ex-info (.getMessage e)
                           (merge {:type :reader-exception}
                                  (if (indexing-reader? reader)
                                    {:line (get-line-number reader)
                                     :column (get-column-number reader)}))
                           e)))))))

(defn parse-string
  "Parses one object from the string s."
  [s]
  (when (and s (not (identical? s "")))
    (parse (string-push-back-reader s) true nil false)))
