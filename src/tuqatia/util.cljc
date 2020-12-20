(ns tuqatia.util)

(defn exception?
  [v]
  (instance? #?(:clj Throwable :cljs js/Error) v))
