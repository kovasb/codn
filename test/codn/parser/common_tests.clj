
(deftest read-integer
  (is (= {:head :integer, :value 42} (parse-string "42")))
  (is (= {:head :integer, :value 42} (parse-string "+42")))
  (is (= {:head :integer, :value -42} (parse-string "-42")))

  (is (= {:head :integer, :value 42N} (parse-string "42N")))
  (is (= {:head :integer, :value 42N} (parse-string "+42N")))
  (is (= {:head :integer, :value -42N} (parse-string "-42N")))

  (is (= {:head :integer, :value 0} (parse-string "0")))
  (is (= {:head :integer, :value 0N} (parse-string "0N")))

  (is (= {:head :integer, :value 042} (parse-string "042")))
  (is (= {:head :integer, :value +042} (parse-string "+042")))
  (is (= {:head :integer, :value -042} (parse-string "-042")))

  (is (= {:head :integer :value 0x42e} (parse-string "0x42e")))
  (is (= {:head :integer :value +0x42e} (parse-string "+0x42e")))
  (is (= {:head :integer :value -0x42e} (parse-string "-0x42e")))

  (is (instance? Long (:value (parse-string "2147483647"))))
  (is (instance? Long (:value (parse-string "+1"))))
  (is (instance? Long (:value (parse-string "1"))))
  (is (instance? Long (:value (parse-string "+0"))))
  (is (instance? Long (:value (parse-string "0"))))
  (is (instance? Long (:value (parse-string "-0"))))
  (is (instance? Long (:value (parse-string "-1"))))
  (is (instance? Long (:value (parse-string "-2147483648"))))

  (is (instance? Long (:value (parse-string "2147483648"))))
  (is (instance? Long (:value (parse-string "-2147483649"))))
  (is (instance? Long (:value (parse-string "9223372036854775807"))))
  (is (instance? Long (:value (parse-string "-9223372036854775808"))))

  (is (instance? BigInt (:value (parse-string "9223372036854775808"))))
  (is (instance? BigInt (:value (parse-string "-9223372036854775809"))))
  (is (instance? BigInt (:value (parse-string "10000000000000000000000000000000000000000000000000"))))
  (is (instance? BigInt (:value (parse-string "-10000000000000000000000000000000000000000000000000")))))

(deftest read-floating
  (is (= {:head :float, :value 42.23} (parse-string "42.23")))
  (is (= {:head :float, :value +42.23} (parse-string "+42.23")))
  (is (= {:head :float, :value -42.23}  (parse-string "-42.23")))

  (is (= {:head :float, :value 42.23M}  (parse-string "42.23M")))
  (is (= {:head :float, :value +42.23M}  (parse-string "+42.23M")))
  (is (= {:head :float, :value -42.23M}  (parse-string "-42.23M")))

  (is (= {:head :float, :value 42.2e3} (parse-string "42.2e3")))
  (is (= {:head :float, :value 42.2e+3}  (parse-string "+42.2e+3")))
  (is (= {:head :float, :value -42.2e-3}  (parse-string "-42.2e-3")))

  (is (= {:head :float, :value 42.2e3M}  (parse-string "42.2e3M")))
  (is (= {:head :float, :value +42.2e+3M}  (parse-string "+42.2e+3M")))
  (is (= {:head :float, :value -42.2e-3M} (parse-string "-42.2e-3M")))

  (is (instance? Double (:value (parse-string "+1.0e+1"))))
  (is (instance? Double (:value (parse-string "+1.e+1"))))
  (is (instance? Double (:value (parse-string "+1e+1"))))

  (is (instance? Double (:value (parse-string "+1.0e+1"))))
  (is (instance? Double (:value (parse-string "+1.e+1"))))
  (is (instance? Double (:value (parse-string "+1e+1"))))

  (is (instance? Double (:value (parse-string "+1.0e1"))))
  (is (instance? Double (:value (parse-string "+1.e1"))))
  (is (instance? Double (:value (parse-string "+1e1"))))

  (is (instance? Double (:value (parse-string "+1.0e-1"))))
  (is (instance? Double (:value (parse-string "+1.e-1"))))
  (is (instance? Double (:value (parse-string "+1e-1"))))

  (is (instance? Double (:value (parse-string "1.0e+1"))))
  (is (instance? Double (:value (parse-string "1.e+1"))))
  (is (instance? Double (:value (parse-string "1e+1"))))

  (is (instance? Double (:value (parse-string "1.0e-1"))))
  (is (instance? Double (:value (parse-string "1.e-1"))))
  (is (instance? Double (:value (parse-string "1e-1"))))

  (is (instance? Double (:value (parse-string "-1.0e+1"))))
  (is (instance? Double (:value (parse-string "-1.e+1"))))
  (is (instance? Double (:value (parse-string "-1e+1"))))

  (is (instance? Double (:value (parse-string "-1.0e1"))))
  (is (instance? Double (:value (parse-string "-1.e1"))))
  (is (instance? Double (:value (parse-string "-1e1"))))

  (is (instance? Double (:value (parse-string "-1.0e-1"))))
  (is (instance? Double (:value (parse-string "-1.e-1"))))
  (is (instance? Double (:value (parse-string "-1e-1"))))

  (is (instance? Double (:value (parse-string "+1.0"))))
  (is (instance? Double (:value (parse-string "+1."))))

  (is (instance? Double (:value (parse-string "1.0"))))
  (is (instance? Double (:value (parse-string "1."))))

  (is (instance? Double (:value (parse-string "+0.0"))))
  (is (instance? Double (:value (parse-string "+0."))))

  (is (instance? Double (:value (parse-string "0.0"))))
  (is (instance? Double (:value (parse-string "0."))))

  (is (instance? Double (:value (parse-string "-0.0"))))
  (is (instance? Double (:value (parse-string "-0."))))

  (is (instance? Double (:value (parse-string "-1.0"))))
  (is (instance? Double (:value (parse-string "-1."))))

  (is (instance? BigDecimal (:value (parse-string "9223372036854775808M"))))
  (is (instance? BigDecimal (:value (parse-string "-9223372036854775809M"))))
  (is (instance? BigDecimal (:value (parse-string "2147483647M"))))
  (is (instance? BigDecimal (:value (parse-string "+1M"))))
  (is (instance? BigDecimal (:value (parse-string "1M"))))
  (is (instance? BigDecimal (:value (parse-string "+0M"))))
  (is (instance? BigDecimal (:value (parse-string "0M"))))
  (is (instance? BigDecimal (:value (parse-string "-0M"))))
  (is (instance? BigDecimal (:value (parse-string "-1M"))))
  (is (instance? BigDecimal (:value (parse-string "-2147483648M"))))

  (is (instance? BigDecimal (:value (parse-string "+1.0e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1.e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1e+1M"))))

  (is (instance? BigDecimal (:value (parse-string "+1.0e1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1.e1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1e1M"))))

  (is (instance? BigDecimal (:value (parse-string "+1.0e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1.e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "+1e-1M"))))

  (is (instance? BigDecimal (:value (parse-string "1.0e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "1.e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "1e+1M"))))

  (is (instance? BigDecimal (:value (parse-string "1.0e1M"))))
  (is (instance? BigDecimal (:value (parse-string "1.e1M"))))
  (is (instance? BigDecimal (:value (parse-string "1e1M"))))

  (is (instance? BigDecimal (:value (parse-string "1.0e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "1.e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "1e-1M"))))

  (is (instance? BigDecimal (:value (parse-string "-1.0e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1.e+1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1e+1M"))))

  (is (instance? BigDecimal (:value (parse-string "-1.0e1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1.e1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1e1M"))))

  (is (instance? BigDecimal (:value (parse-string "-1.0e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1.e-1M"))))
  (is (instance? BigDecimal (:value (parse-string "-1e-1M"))))

  (is (instance? BigDecimal (:value (parse-string "+1.0M"))))
  (is (instance? BigDecimal (:value (parse-string "+1.M"))))

  (is (instance? BigDecimal (:value (parse-string "1.0M"))))
  (is (instance? BigDecimal (:value (parse-string "1.M"))))

  (is (instance? BigDecimal (:value (parse-string "+0.0M"))))
  (is (instance? BigDecimal (:value (parse-string "+0.M"))))

  (is (instance? BigDecimal (:value (parse-string "0.0M"))))
  (is (instance? BigDecimal (:value (parse-string "0.M"))))

  (is (instance? BigDecimal (:value (parse-string "-0.0M"))))
  (is (instance? BigDecimal (:value (parse-string "-0.M"))))

  (is (instance? BigDecimal (:value (parse-string "-1.0M"))))
  (is (instance? BigDecimal (:value (parse-string "-1.M")))))

(deftest read-ratio
  (is (= {:head :ratio, :body [{:head :integer, :value 5} {:head :integer, :value 2}]} (parse-string "5/2")))
  (is (= {:head :ratio, :body [{:head :integer, :value 5} {:head :integer, :value 2}]} (parse-string "+5/2")))
  (is (= {:head :ratio, :body [{:head :integer, :value -5} {:head :integer, :value 2}]} (parse-string "-5/2"))))


(deftest read-symbol
  (is (= '{:head :symbol, :value foo} (parse-string "foo")))
  (is (= '{:head :symbol, :value foo/bar} (parse-string "foo/bar")))
  (is (= '{:head :symbol, :value *+!-_?} (parse-string "*+!-_?")))
  (is (= '{:head :symbol, :value abc:def:ghi} (parse-string "abc:def:ghi")))
  (is (= '{:head :symbol, :value abc.def/ghi} (parse-string "abc.def/ghi")))
  (is (= '{:head :symbol, :value abc/def.ghi} (parse-string "abc/def.ghi")))
  (is (= '{:head :symbol, :value abc:def/ghi:jkl.mno} (parse-string "abc:def/ghi:jkl.mno")))
  (is (instance? clojure.lang.Symbol (:value (parse-string "alphabet"))))
  (is (= "foo//" (str (:value (parse-string "foo//"))))) ;; the clojure reader can't read this
  (is (= (str 'NaN) (str (:value (parse-string "NaN"))))) ;; the clojure reader can't read this
  (is (= {:head :positive-infinity, :value Double/POSITIVE_INFINITY}  (parse-string "Infinity"))) ;; the clojure reader can't read this
  (is (= {:head :positive-infinity, :value Double/POSITIVE_INFINITY}  (parse-string "+Infinity"))) ;; the clojure reader can't read this
  (is (= {:head :negative-infinity, :value Double/NEGATIVE_INFINITY}  (parse-string "-Infinity")))) ;; the clojure reader can't read this

(deftest read-specials
  (is (= '{:head :nil, :value nil} (parse-string "nil")))
  (is (= '{:head :boolean, :value false} (parse-string "false")))
  (is (= '{:head :boolean, :value true} (parse-string "true"))))

(deftest read-char
  (is (= {:head :character, :value \f} (parse-string "\\f")))
  (is (= {:head :character, :value \u0194}  (parse-string "\\u0194")))
  (is (= {:head :character, :value \a} (parse-string "\\x61"))) ;; the clojure reader can't read this
  (is (= {:head :character, :value  \o123} (parse-string "\\o123")))
  (is (= {:head :character, :value \newline }  (parse-string "\\newline")))
  (is (= {:head :character, :value  (char 0) } (parse-string "\\o0")))
  (is (= {:head :character, :value (char 0) }  (parse-string "\\o000")))
  (is (= {:head :character, :value (char 0377) }  (parse-string "\\o377")))
  (is (= {:head :character, :value \A }  (parse-string "\\u0041")))
  (is (= {:head :character, :value \@ }  (parse-string "\\@")))
  (is (= {:head :character, :value (char 0xd7ff)}  (parse-string "\\ud7ff")))
  (is (= {:head :character, :value (char 0xe000) }  (parse-string "\\ue000")))
  (is (= {:head :character, :value (char 0xffff)} (parse-string "\\uffff"))))

(deftest parse-string*
  (is (= {:head :string, :value "foo bar"}  (parse-string "\"foo bar\"")))
  (is (= {:head :string, :value "foo\\bar"} (parse-string "\"foo\\\\bar\"")))
  (is (= {:head :string, :value "foo\000bar"} (parse-string "\"foo\\000bar\"")))
  (is (= {:head :string, :value "foo\u0194bar"}  (parse-string "\"foo\\u0194bar\"")))
  (is (= {:head :string, :value  "fooabar" } (parse-string "\"foo\\x61bar\""))) ;; the clojure reader can't read this
  (is (= {:head :string, :value "foo\123bar"}  (parse-string "\"foo\\123bar\""))))

(deftest read-list
  (is (= '{:head :list, :body []} (parse-string "()")))
  (is (= '{:head :list, :body [{:head :symbol, :value foo} {:head :symbol, :value bar}]} (parse-string "(foo bar)")))
  (is (= '{:head :list, :body [{:head :symbol, :value foo} {:head :list, :body [{:head :symbol, :value bar}]} {:head :symbol, :value baz}]} (parse-string "(foo (bar) baz)"))))

(deftest read-vector
  (is (= '{:head :vector, :body []} (parse-string "[]")))
  (is (= '{:head :vector, :body [{:head :symbol, :value foo} {:head :symbol, :value bar}]} (parse-string "[foo bar]")))
  (is (= '{:head :vector, :body [{:head :symbol, :value foo} {:head :vector, :body [{:head :symbol, :value bar}]} {:head :symbol, :value baz}]} (parse-string "[foo [bar] baz]"))))

(deftest read-map
  (is (= '{:head :map, :body []} (parse-string "{}")))
  (is (= '{:head :map, :body [{:head :symbol, :value foo} {:head :symbol, :value bar}]} (parse-string "{foo bar}")))
  (is (= '{:head :map, :body [{:head :symbol, :value foo} {:head :map, :body [{:head :symbol, :value bar} {:head :symbol, :value baz}]}]} (parse-string "{foo {bar baz}}"))))

(deftest read-set
  (is (= '{:head :set, :body []} (parse-string "#{}")))
  (is (= '{:head :set, :body [{:head :symbol, :value foo} {:head :symbol, :value bar}]} (parse-string "#{foo bar}")))
  (is (= '{:head :set, :body [{:head :symbol, :value foo} {:head :set, :body [{:head :symbol, :value bar}]} {:head :symbol, :value baz}]} (parse-string "#{foo #{bar} baz}"))))

(deftest read-metadata
  (is (= '{:head :meta, :body [{:head :keyword, :value :foo} {:head :quote, :body [{:head :symbol, :value bar}]}]} (parse-string "^:foo 'bar")))
  (is (= '{:head :meta, :body [{:head :map, :body [{:head :keyword, :value :foo} {:head :symbol, :value bar}]} {:head :quote, :body [{:head :symbol, :value baz}]}]} (parse-string "^{:foo bar} 'baz")))
  (is (= '{:head :meta, :body [{:head :string, :value "foo"} {:head :quote, :body [{:head :symbol, :value bar}]}]} (parse-string "^\"foo\" 'bar")))
  (is (= '{:head :meta, :body [{:head :symbol, :value String} {:head :quote, :body [{:head :symbol, :value x}]}]} (parse-string "^String 'x"))))
