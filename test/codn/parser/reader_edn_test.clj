(ns codn.parser.reader-edn-test
  (:use [codn.parser.core :only [parse-string]]
        [clojure.test :only [deftest is]])
  (:import clojure.lang.BigInt))

(load "common_tests")

(deftest read-keyword
  (is (= :foo-bar (parse-string ":foo-bar")))
  (is (= :foo/bar (parse-string ":foo/bar")))
  (is (= :*+!-_? (parse-string ":*+!-_?")))
  (is (= :abc:def:ghi (parse-string ":abc:def:ghi")))
  (is (= :abc.def/ghi (parse-string ":abc.def/ghi")))
  (is (= :abc/def.ghi (parse-string ":abc/def.ghi")))
  (is (= :abc:def/ghi:jkl.mno (parse-string ":abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Keyword (parse-string ":alphabet"))) )

(deftest read-tagged
  ;; (is (= #inst "2010-11-12T13:14:15.666"
  ;;        (parse-string "#inst \"2010-11-12T13:14:15.666\"")))
  ;; (is (= #inst "2010-11-12T13:14:15.666"
  ;;        (parse-string "#inst\"2010-11-12T13:14:15.666\"")))
  ;; (is (= #uuid "550e8400-e29b-41d4-a716-446655440000"
  ;;        (parse-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")))
  ;; (is (= #uuid "550e8400-e29b-41d4-a716-446655440000"
  ;;        (parse-string "#uuid\"550e8400-e29b-41d4-a716-446655440000\"")))
  (is (= (java.util.UUID/fromString "550e8400-e29b-41d4-a716-446655440000")
         (parse-string "#uuid \"550e8400-e29b-41d4-a716-446655440000\"")))
  (is (= (java.util.UUID/fromString "550e8400-e29b-41d4-a716-446655440000")
         (parse-string "#uuid\"550e8400-e29b-41d4-a716-446655440000\"")))
  (let [my-unknown (fn [tag val] {:unknown-tag tag :value val})]
    (is (= {:unknown-tag 'foo :value 'bar}
           (parse-string {:default my-unknown} "#foo bar")))))
