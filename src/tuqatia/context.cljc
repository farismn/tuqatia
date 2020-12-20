(ns tuqatia.context)

(defprotocol Context
  (context? [this]))

(extend-protocol Context
  #?(:clj  clojure.lang.IPersistentMap
     :cljs cljs.core.PersistentMap)
  (context? [_]
    true)

  #?(:clj  java.lang.Object
     :cljs default)
  (context? [_]
    false))

(defn halt
  [ctx]
  (dissoc ctx :queue))
