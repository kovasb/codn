(ns codn.parser.ExceptionInfo
  (:gen-class :extends java.lang.Exception
              :init init
              :constructors {[String clojure.lang.IPersistentMap] [String]
                             [String clojure.lang.IPersistentMap Throwable] [String Throwable]}
              :state data
              :methods [[getData [] clojure.lang.IPersistentMap]]))

(defn -init
  ([s data]
     [[s] data])
  ([s data throwable]
     [[s throwable] data]))

(defn -getData [^codn.parse.ExceptionInfo this]
  (.data this))

(defn -toString [^codn.parse.ExceptionInfo this]
  (str "codn.parse.ExceptionInfo: " (.getMessage this) " " (str (.data this))))
