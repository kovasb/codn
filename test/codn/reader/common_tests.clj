
(deftest read-integer
  (is (== 42 (parse-read-string "42")))
  (is (== +42 (parse-read-string "+42")))
  (is (== -42 (parse-read-string "-42")))

  (is (== 42 (parse-read-string "42N")))
  (is (== +42 (parse-read-string "+42N")))
  (is (== -42 (parse-read-string "-42N")))

  (is (== 0 (parse-read-string "0")))
  (is (== 0N (parse-read-string "0N")))

  (is (== 042 (parse-read-string "042")))
  (is (== +042 (parse-read-string "+042")))
  (is (== -042 (parse-read-string "-042")))

  (is (== 0x42e (parse-read-string "0x42e")))
  (is (== +0x42e (parse-read-string "+0x42e")))
  (is (== -0x42e (parse-read-string "-0x42e")))

  (is (instance? Long (parse-read-string "2147483647")))
  (is (instance? Long (parse-read-string "+1")))
  (is (instance? Long (parse-read-string "1")))
  (is (instance? Long (parse-read-string "+0")))
  (is (instance? Long (parse-read-string "0")))
  (is (instance? Long (parse-read-string "-0")))
  (is (instance? Long (parse-read-string "-1")))
  (is (instance? Long (parse-read-string "-2147483648")))

  (is (instance? Long (parse-read-string "2147483648")))
  (is (instance? Long (parse-read-string "-2147483649")))
  (is (instance? Long (parse-read-string "9223372036854775807")))
  (is (instance? Long (parse-read-string "-9223372036854775808")))

  (is (instance? BigInt (parse-read-string "9223372036854775808")))
  (is (instance? BigInt (parse-read-string "-9223372036854775809")))
  (is (instance? BigInt (parse-read-string "10000000000000000000000000000000000000000000000000")))
  (is (instance? BigInt (parse-read-string "-10000000000000000000000000000000000000000000000000"))))

(deftest read-floating
  (is (== 42.23 (parse-read-string "42.23")))
  (is (== +42.23 (parse-read-string "+42.23")))
  (is (== -42.23 (parse-read-string "-42.23")))

  (is (== 42.23M (parse-read-string "42.23M")))
  (is (== +42.23M (parse-read-string "+42.23M")))
  (is (== -42.23M (parse-read-string "-42.23M")))

  (is (== 42.2e3 (parse-read-string "42.2e3")))
  (is (== +42.2e+3 (parse-read-string "+42.2e+3")))
  (is (== -42.2e-3 (parse-read-string "-42.2e-3")))

  (is (== 42.2e3M (parse-read-string "42.2e3M")))
  (is (== +42.2e+3M (parse-read-string "+42.2e+3M")))
  (is (== -42.2e-3M (parse-read-string "-42.2e-3M")))

  (is (instance? Double (parse-read-string "+1.0e+1")))
  (is (instance? Double (parse-read-string "+1.e+1")))
  (is (instance? Double (parse-read-string "+1e+1")))

  (is (instance? Double (parse-read-string "+1.0e+1")))
  (is (instance? Double (parse-read-string "+1.e+1")))
  (is (instance? Double (parse-read-string "+1e+1")))

  (is (instance? Double (parse-read-string "+1.0e1")))
  (is (instance? Double (parse-read-string "+1.e1")))
  (is (instance? Double (parse-read-string "+1e1")))

  (is (instance? Double (parse-read-string "+1.0e-1")))
  (is (instance? Double (parse-read-string "+1.e-1")))
  (is (instance? Double (parse-read-string "+1e-1")))

  (is (instance? Double (parse-read-string "1.0e+1")))
  (is (instance? Double (parse-read-string "1.e+1")))
  (is (instance? Double (parse-read-string "1e+1")))

  (is (instance? Double (parse-read-string "1.0e-1")))
  (is (instance? Double (parse-read-string "1.e-1")))
  (is (instance? Double (parse-read-string "1e-1")))

  (is (instance? Double (parse-read-string "-1.0e+1")))
  (is (instance? Double (parse-read-string "-1.e+1")))
  (is (instance? Double (parse-read-string "-1e+1")))

  (is (instance? Double (parse-read-string "-1.0e1")))
  (is (instance? Double (parse-read-string "-1.e1")))
  (is (instance? Double (parse-read-string "-1e1")))

  (is (instance? Double (parse-read-string "-1.0e-1")))
  (is (instance? Double (parse-read-string "-1.e-1")))
  (is (instance? Double (parse-read-string "-1e-1")))

  (is (instance? Double (parse-read-string "+1.0")))
  (is (instance? Double (parse-read-string "+1.")))

  (is (instance? Double (parse-read-string "1.0")))
  (is (instance? Double (parse-read-string "1.")))

  (is (instance? Double (parse-read-string "+0.0")))
  (is (instance? Double (parse-read-string "+0.")))

  (is (instance? Double (parse-read-string "0.0")))
  (is (instance? Double (parse-read-string "0.")))

  (is (instance? Double (parse-read-string "-0.0")))
  (is (instance? Double (parse-read-string "-0.")))

  (is (instance? Double (parse-read-string "-1.0")))
  (is (instance? Double (parse-read-string "-1.")))

  (is (instance? BigDecimal (parse-read-string "9223372036854775808M")))
  (is (instance? BigDecimal (parse-read-string "-9223372036854775809M")))
  (is (instance? BigDecimal (parse-read-string "2147483647M")))
  (is (instance? BigDecimal (parse-read-string "+1M")))
  (is (instance? BigDecimal (parse-read-string "1M")))
  (is (instance? BigDecimal (parse-read-string "+0M")))
  (is (instance? BigDecimal (parse-read-string "0M")))
  (is (instance? BigDecimal (parse-read-string "-0M")))
  (is (instance? BigDecimal (parse-read-string "-1M")))
  (is (instance? BigDecimal (parse-read-string "-2147483648M")))

  (is (instance? BigDecimal (parse-read-string "+1.0e+1M")))
  (is (instance? BigDecimal (parse-read-string "+1.e+1M")))
  (is (instance? BigDecimal (parse-read-string "+1e+1M")))

  (is (instance? BigDecimal (parse-read-string "+1.0e1M")))
  (is (instance? BigDecimal (parse-read-string "+1.e1M")))
  (is (instance? BigDecimal (parse-read-string "+1e1M")))

  (is (instance? BigDecimal (parse-read-string "+1.0e-1M")))
  (is (instance? BigDecimal (parse-read-string "+1.e-1M")))
  (is (instance? BigDecimal (parse-read-string "+1e-1M")))

  (is (instance? BigDecimal (parse-read-string "1.0e+1M")))
  (is (instance? BigDecimal (parse-read-string "1.e+1M")))
  (is (instance? BigDecimal (parse-read-string "1e+1M")))

  (is (instance? BigDecimal (parse-read-string "1.0e1M")))
  (is (instance? BigDecimal (parse-read-string "1.e1M")))
  (is (instance? BigDecimal (parse-read-string "1e1M")))

  (is (instance? BigDecimal (parse-read-string "1.0e-1M")))
  (is (instance? BigDecimal (parse-read-string "1.e-1M")))
  (is (instance? BigDecimal (parse-read-string "1e-1M")))

  (is (instance? BigDecimal (parse-read-string "-1.0e+1M")))
  (is (instance? BigDecimal (parse-read-string "-1.e+1M")))
  (is (instance? BigDecimal (parse-read-string "-1e+1M")))

  (is (instance? BigDecimal (parse-read-string "-1.0e1M")))
  (is (instance? BigDecimal (parse-read-string "-1.e1M")))
  (is (instance? BigDecimal (parse-read-string "-1e1M")))

  (is (instance? BigDecimal (parse-read-string "-1.0e-1M")))
  (is (instance? BigDecimal (parse-read-string "-1.e-1M")))
  (is (instance? BigDecimal (parse-read-string "-1e-1M")))

  (is (instance? BigDecimal (parse-read-string "+1.0M")))
  (is (instance? BigDecimal (parse-read-string "+1.M")))

  (is (instance? BigDecimal (parse-read-string "1.0M")))
  (is (instance? BigDecimal (parse-read-string "1.M")))

  (is (instance? BigDecimal (parse-read-string "+0.0M")))
  (is (instance? BigDecimal (parse-read-string "+0.M")))

  (is (instance? BigDecimal (parse-read-string "0.0M")))
  (is (instance? BigDecimal (parse-read-string "0.M")))

  (is (instance? BigDecimal (parse-read-string "-0.0M")))
  (is (instance? BigDecimal (parse-read-string "-0.M")))

  (is (instance? BigDecimal (parse-read-string "-1.0M")))
  (is (instance? BigDecimal (parse-read-string "-1.M"))))

(deftest read-ratio
  (is (== 4/2 (parse-read-string "4/2")))
  (is (== 4/2 (parse-read-string "+4/2")))
  (is (== -4/2 (parse-read-string "-4/2"))))


(deftest read-symbol
  (is (= 'foo (parse-read-string "foo")))
  (is (= 'foo/bar (parse-read-string "foo/bar")))
  (is (= '*+!-_? (parse-read-string "*+!-_?")))
  (is (= 'abc:def:ghi (parse-read-string "abc:def:ghi")))
  (is (= 'abc.def/ghi (parse-read-string "abc.def/ghi")))
  (is (= 'abc/def.ghi (parse-read-string "abc/def.ghi")))
  (is (= 'abc:def/ghi:jkl.mno (parse-read-string "abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Symbol (parse-read-string "alphabet")))
  (is (= "foo//" (str (parse-read-string "foo//")))) ;; the clojure reader can't read this
  (is (= (str 'NaN) (str (parse-read-string "NaN")))) ;; the clojure reader can't read this
  (is (= Double/POSITIVE_INFINITY (parse-read-string "Infinity"))) ;; the clojure reader can't read this
  (is (= Double/POSITIVE_INFINITY (parse-read-string "+Infinity"))) ;; the clojure reader can't read this
  (is (= Double/NEGATIVE_INFINITY (parse-read-string "-Infinity")))) ;; the clojure reader can't read this

(deftest read-specials
  (is (= 'nil nil))
  (is (= 'false false))
  (is (= 'true true)))

(deftest read-char
  (is (= \f (parse-read-string "\\f")))
  (is (= \u0194 (parse-read-string "\\u0194")))
  (is (= \a (parse-read-string "\\x61"))) ;; the clojure reader can't read this
  (is (= \o123 (parse-read-string "\\o123")))
  (is (= \newline (parse-read-string "\\newline")))
  (is (= (char 0) (parse-read-string "\\o0")))
  (is (= (char 0) (parse-read-string "\\o000")))
  (is (= (char 0377) (parse-read-string "\\o377")))
  (is (= \A (parse-read-string "\\u0041")))
  (is (= \@ (parse-read-string "\\@")))
  (is (= (char 0xd7ff) (parse-read-string "\\ud7ff")))
  (is (= (char 0xe000) (parse-read-string "\\ue000")))
  (is (= (char 0xffff) (parse-read-string "\\uffff"))))

(deftest parse-read-string*
  (is (= "foo bar" (parse-read-string "\"foo bar\"")))
  (is (= "foo\\bar" (parse-read-string "\"foo\\\\bar\"")))
  (is (= "foo\000bar" (parse-read-string "\"foo\\000bar\"")))
  (is (= "foo\u0194bar" (parse-read-string "\"foo\\u0194bar\"")))
  (is (= "fooabar" (parse-read-string "\"foo\\x61bar\""))) ;; the clojure reader can't read this
  (is (= "foo\123bar" (parse-read-string "\"foo\\123bar\""))))

(deftest read-list
  (is (= '() (parse-read-string "()")))
  (is (= '(foo bar) (parse-read-string "(foo bar)")))
  (is (= '(foo (bar) baz) (parse-read-string "(foo (bar) baz)"))))

(deftest read-vector
  (is (= '[] (parse-read-string "[]")))
  (is (= '[foo bar] (parse-read-string "[foo bar]")))
  (is (= '[foo [bar] baz] (parse-read-string "[foo [bar] baz]"))))

(deftest read-map
  (is (= '{} (parse-read-string "{}")))
  (is (= '{foo bar} (parse-read-string "{foo bar}")))
  (is (= '{foo {bar baz}} (parse-read-string "{foo {bar baz}}"))))

(deftest read-set
  (is (= '#{} (parse-read-string "#{}")))
  (is (= '#{foo bar} (parse-read-string "#{foo bar}")))
  (is (= '#{foo #{bar} baz} (parse-read-string "#{foo #{bar} baz}"))))

(deftest read-metadata
  (is (= {:foo true} (meta (parse-read-string "^:foo 'bar"))))
  (is (= {:foo 'bar} (meta (parse-read-string "^{:foo bar} 'baz"))))
  (is (= {:tag "foo"} (meta (parse-read-string "^\"foo\" 'bar"))))
  (is (= {:tag 'String} (meta (parse-read-string "^String 'x")))))
