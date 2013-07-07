(ns codn.reader.reader-test
  (:use [codn.reader.core :only [read-codn]]
        [codn.parser.core :only [parse-string]]
        [clojure.test :only [deftest is]])
  (:import clojure.lang.BigInt))

(defn parse-read-string [x]
  (read-codn (parse-string x)))

(load "common_tests")

(deftest read-keyword
  (is (= :foo-bar (parse-read-string ":foo-bar")))
  (is (= :foo/bar (parse-read-string ":foo/bar")))
  (is (= :user/foo-bar (binding [*ns* (the-ns 'user)]
                         (parse-read-string "::foo-bar"))))
  (is (= :clojure.core/foo-bar
         (do (alias 'core 'clojure.core)
             (parse-read-string "::core/foo-bar"))))
  (is (= :*+!-_? (parse-read-string ":*+!-_?")))
  (is (= :abc:def:ghi (parse-read-string ":abc:def:ghi")))
  (is (= :abc.def/ghi (parse-read-string ":abc.def/ghi")))
  (is (= :abc/def.ghi (parse-read-string ":abc/def.ghi")))
  (is (= :abc:def/ghi:jkl.mno (parse-read-string ":abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Keyword (parse-read-string ":alphabet"))) )

(deftest read-regex
  (is (= (str #"\[\]?(\")\\")
         (str (parse-read-string "#\"\\[\\]?(\\\")\\\\\"")))))

(deftest read-quote
  (is (= ''foo (parse-read-string "'foo"))))

(deftest read-syntax-quote
  (is (= '`user/foo (binding [*ns* (the-ns 'user)]
                      (parse-read-string "`foo"))))
  (is (= '`+ (parse-read-string "`+")))
  (is (= '`foo/bar (parse-read-string "`foo/bar")))
  (is (= '`1 (parse-read-string "`1")))
  (is (= '`(1 (~2 ~@(3))) (parse-read-string "`(1 (~2 ~@(3)))"))))

(deftest read-deref
  (is (= '@foo (parse-read-string "@foo"))))

(deftest read-var
  (is (= '(var foo) (parse-read-string "#'foo"))))

(deftest read-fn
  (is (= '(fn* [] (foo bar baz)) (parse-read-string "#(foo bar baz)"))))

(deftest read-arg
  (is (= 14 ((eval (parse-read-string "#(apply + % %1 %3 %&)")) 1 2 3 4 5))))

(deftest read-eval
  (is (= 3 (parse-read-string "#=(+ 1 2)"))))

(deftest read-tagged
  ;; (is (= #inst "2010-11-12T13:14:15.666"
  ;;        (parse-read-string "#inst \"2010-11-12T13:14:15.666\"")))
  ;; (is (= #inst "2010-11-12T13:14:15.666"
  ;;        (parse-read-string "#inst\"2010-11-12T13:14:15.666\"")))
  ;; (is (= #uuid "550e8400-e29b-41d4-a716-446655440000"
  ;;        (parse-read-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")))
  ;; (is (= #uuid "550e8400-e29b-41d4-a716-446655440000"
  ;;        (parse-read-string "#uuid\"550e8400-e29b-41d4-a716-446655440000\"")))
  (is (= (java.util.UUID/fromString "550e8400-e29b-41d4-a716-446655440000")
         (parse-read-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")))
  (is (= (java.util.UUID/fromString "550e8400-e29b-41d4-a716-446655440000")
                  (parse-read-string "#uuid\"550e8400-e29b-41d4-a716-446655440000\"")))
  (when *default-data-reader-fn*
    (let [my-unknown (fn [tag val] {:unknown-tag tag :value val})]
      (is (= {:unknown-tag 'foo :value 'bar}
             (binding [*default-data-reader-fn* my-unknown]
               (parse-read-string "#foo bar")))))))


(defrecord bar [baz buz])


(defrecord foo [])


(deftest read-record
  (is (= (foo.)
         (parse-read-string "#codn.reader.reader_test.foo[]")))
  (is (= (foo.) (parse-read-string "#codn.reader.reader_test.foo []"))) ;; not valid in clojure
  (is (= (foo.) (parse-read-string "#codn.reader.reader_test.foo{}")))
  (is (= (assoc (foo.) :foo 'bar) (parse-read-string "#codn.reader.reader_test.foo{:foo bar}")))

  (is (= (map->bar {}) (parse-read-string "#codn.reader.reader_test.bar{}")))
  (is (= (bar. 1 nil) (parse-read-string "#codn.reader.reader_test.bar{:baz 1}")))
  (is (= (bar. 1 nil) (parse-read-string "#codn.reader.reader_test.bar[1 nil]")))
  (is (= (bar. 1 2) (parse-read-string "#codn.reader.reader_test.bar[1 2]"))))
