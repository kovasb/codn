(ns codn.parser.reader-test
  (:refer-clojure :exclude [*default-data-reader-fn*])
  (:use [codn.parser.core :only [parse-string]]
        [clojure.test :only [deftest is]])
  (:import clojure.lang.BigInt))

(load "common_tests")

(deftest read-keyword
  (is (= {:head :keyword, :value :foo-bar} (parse-string ":foo-bar")))
  (is (= {:head :keyword, :value :foo/bar} (parse-string ":foo/bar")))
  (is (= {:head :autoresolved-keyword, :body [{:head :keyword, :value :foo-bar}]} (parse-string "::foo-bar")))

  (is (= {:head :keyword, :value :*+!-_?} (parse-string ":*+!-_?")))
  (is (= {:head :keyword, :value :abc:def:ghi} (parse-string ":abc:def:ghi")))
  (is (= {:head :keyword, :value :abc.def/ghi} (parse-string ":abc.def/ghi")))
  (is (= {:head :keyword, :value :abc/def.ghi} (parse-string ":abc/def.ghi")))
  (is (= {:head :keyword, :value :abc:def/ghi:jkl.mno} (parse-string ":abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Keyword (:value (parse-string ":alphabet")))) )

(deftest read-regex
  (is (= (str #"\[\]?(\")\\")
         (first (:body (parse-string "#\"\\[\\]?(\\\")\\\\\""))))))

(deftest read-quote
  (is (= '{:head :quote, :body [{:head :symbol, :value foo}]} (parse-string "'foo"))))

(deftest read-syntax-quote
  (is (= '{:head :syntax-quote, :body [{:head :symbol, :value foo}]} (parse-string "`foo") ))
  (is (= '{:head :syntax-quote, :body [{:head :symbol, :value +}]} (parse-string "`+")))
  (is (= '{:head :syntax-quote, :body [{:head :symbol, :value foo/bar}]} (parse-string "`foo/bar")))
  (is (= {:head :syntax-quote, :body [{:head :integer, :value 1}]} (parse-string "`1")))
  (is (= {:head :syntax-quote, :body [{:head :list, :body [{:head :integer, :value 1} {:head :list, :body [{:head :unquote, :body [{:head :integer, :value 2}]} {:head :unquote-splicing, :body [{:head :list, :body [{:head :integer, :value 3}]}]}]}]}]} (parse-string "`(1 (~2 ~@(3)))"))))

(deftest read-deref
  (is (= '{:head :deref, :body [{:head :symbol, :value foo}]} (parse-string "@foo"))))

(deftest read-var
  (is (= '{:head :var-quote, :body [{:head :symbol, :value foo}]} (parse-string "#'foo"))))

(deftest read-fn
  (is (= '{:head :fn, :body [{:head :list, :body [{:head :symbol, :value foo} {:head :symbol, :value bar} {:head :symbol, :value baz}]}]} (parse-string "#(foo bar baz)"))))

(deftest read-arg
  (is (= '{:head :fn, :body [{:head :list, :body [{:head :symbol, :value apply} {:head :symbol, :value +} {:head :symbol, :value %} {:head :symbol, :value %1} {:head :symbol, :value %3} {:head :symbol, :value %&}]}]} (parse-string "#(apply + % %1 %3 %&)"))))

(deftest read-eval
  (is (= '{:head :read-eval, :body [{:head :list, :body [{:head :symbol, :value +} {:head :integer, :value 1} {:head :integer, :value 2}]}]} (parse-string "#=(+ 1 2)"))))

(deftest read-tagged
   (is (= '{:head :tagged-literal, :body [{:head :symbol, :value inst} {:head :string, :value "2010-11-12T13:14:15.666"}]}
          (parse-string "#inst \"2010-11-12T13:14:15.666\"")))
   (is (= '{:head :tagged-literal, :body [{:head :symbol, :value uuid} {:head :string, :value "550e8400-e29b-41d4-a716-446655440000"}]}
         (parse-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")))
   (is (= '{:head :tagged-literal, :body [{:head :symbol, :value foo} {:head :symbol, :value bar}]} (parse-string "#foo bar"))))


(deftest read-record
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.foo} {:head :vector, :body []}]} (parse-string "#clojure.tools.reader_test.foo[]")))
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.foo} {:head :vector, :body []}]} (parse-string "#clojure.tools.reader_test.foo []"))) ;; not valid in clojure
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.foo} {:head :map, :body []}]} (parse-string "#clojure.tools.reader_test.foo{}")))
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.foo} {:head :map, :body [{:head :keyword, :value :foo} {:head :symbol, :value bar}]}]} (parse-string "#clojure.tools.reader_test.foo{:foo bar}")))

  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.bar} {:head :map, :body []}]} (parse-string "#clojure.tools.reader_test.bar{}")))
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.bar} {:head :map, :body [{:head :keyword, :value :baz} {:head :integer, :value 1}]}]} (parse-string "#clojure.tools.reader_test.bar{:baz 1}")))
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.bar} {:head :vector, :body [{:head :integer, :value 1} {:head :nil, :value nil}]}]} (parse-string "#clojure.tools.reader_test.bar[1 nil]")))
  (is (= '{:head :constructor, :body [{:head :symbol, :value clojure.tools.reader_test.bar} {:head :vector, :body [{:head :integer, :value 1} {:head :integer, :value 2}]}]}
 (parse-string "#clojure.tools.reader_test.bar[1 2]"))))
