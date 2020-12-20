(ns tuqatia.interceptor)

(defrecord Interceptor [name enter leave error])

(defprotocol AsInterceptor
  (as-interceptor [this]))

(extend-protocol AsInterceptor
  #?(:clj  clojure.lang.IPersistentMap
     :cljs cljs.core.PersistentMap)
  (as-interceptor [this]
    (map->Interceptor this))

  #?(:clj  clojure.lang.IPersistentVector
     :cljs cljs.core.PersistentVector)
  (as-interceptor [this]
    (as-interceptor (apply (first this) (rest this))))

  #?(:clj  clojure.lang.Fn
     :cljs function)
  (as-interceptor [this]
    (as-interceptor {:enter this}))

  Interceptor
  (as-interceptor [this]
    this)

  nil
  (as-interceptor [_]
    nil))
