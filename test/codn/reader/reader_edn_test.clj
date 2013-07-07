(ns codn.reader.reader-edn-test

  (:use [clojure.tools.reader.edn :only [parse-read-string]]
        [clojure.test :only [deftest is]])
  (:import clojure.lang.BigInt))

(load "common_tests")

(deftest read-keyword
  (is (= :foo-bar (parse-read-string ":foo-bar")))
  (is (= :foo/bar (parse-read-string ":foo/bar")))
  (is (= :*+!-_? (parse-read-string ":*+!-_?")))
  (is (= :abc:def:ghi (parse-read-string ":abc:def:ghi")))
  (is (= :abc.def/ghi (parse-read-string ":abc.def/ghi")))
  (is (= :abc/def.ghi (parse-read-string ":abc/def.ghi")))
  (is (= :abc:def/ghi:jkl.mno (parse-read-string ":abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Keyword (parse-read-string ":alphabet"))) )

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
  (let [my-unknown (fn [tag val] {:unknown-tag tag :value val})]
    (is (= {:unknown-tag 'foo :value 'bar}
           (parse-read-string {:default my-unknown} "#foo bar")))))
