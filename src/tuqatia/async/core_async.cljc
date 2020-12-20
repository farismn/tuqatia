(ns tuqatia.async.core-async
  (:require
   [clojure.core.async
    :as a
    #?@(:clj  [:refer [go]]
        :cljs [:refer-macros [go]])]
   [tuqatia.async :as tqt.a]
   [tuqatia.util :as tqt.u]))

(extend-protocol tqt.a/Async
  #?(:clj  clojure.core.async.impl.channels.ManyToManyChannel
     :cljs cljs.core.async.impl.channels.ManyToManyChannel)
  (async? [_]
    true)
  (continue [this f]
    (go (f (a/<! this))))
  (rescue [this f]
    (go (let [-this (a/<! this)]
          (if (tqt.u/exception? -this)
            (f -this)
            -this))))
  #?(:clj (wait-for [this]
                    (a/<!! this))))
