(ns tuqatia.async
  #?(:clj (:require
           [tuqatia.util :as tqt.u])))

(defprotocol Async
  (async? [this])
  (continue [this f])
  (rescue [this f])
  #?(:clj (wait-for [this])))

#?(:clj
   (extend-protocol Async
     java.lang.Object
     (async? [_]
       false)
     (continue [this f]
       (f this))
     (rescue [this _]
       this)
     (wait-for [this]
       this)

     clojure.lang.IDeref
     (async? [_]
       true)
     (continue [this f]
       (future (f @this)))
     (rescue [this f]
       (future (let [-this @this]
                 (if (tqt.u/exception? -this)
                   (f -this)
                   -this))))
     (wait-for [this]
       @this)))

#?(:cljs
   (extend-protocol AsyncContext
     default
     (async? [_]
       false)
     (continue [this f]
       (f this))
     (rescue [this _]
       this)

     js/Promise
     (async? [_]
       true)
     (continue [this f]
       (.then this f))
     (rescue [this f]
       (.catch this f))))
