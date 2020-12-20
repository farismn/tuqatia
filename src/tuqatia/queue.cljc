(ns tuqatia.queue
  (:require
   [tuqatia.interceptor :as tqt.icept]))

(defn- queue
  ([]
   #?(:clj  (clojure.lang.PersistentQueue/EMPTY)
      :cljs (cljs.core.PersistentQueue/EMPTY)))
  ([coll]
   (when (seq coll)
     (into (queue) (keep tqt.icept/as-interceptor) coll))))

(defprotocol AsQueue
  (as-queue [this]))

#?(:clj
   (extend-protocol AsQueue
     clojure.lang.ISeq
     (as-queue [this]
       (queue this))

     clojure.lang.Seqable
     (as-queue [this]
       (queue (seq this)))

     clojure.lang.PersistentQueue
     (as-queue [this]
       this)

     nil
     (as-queue [_]
       nil)))

#?(:cljs
   (extend-protocol AsQueue
     cljs.core.PersistentQueue
     (as-queue [this]
       this)

     cljs.core.List
     (as-queue [this]
       (queue this))

     cljs.core.LazySeq
     (as-queue [this]
       (queue this))

     cljs.core.EmptyList
     (as-queue [_]
       nil)

     cljs.core.PersistentVector
     (as-queue [this]
       (queue this))

     cljs.core.RSeq
     (as-queue [t]
       (queue t))

     array
     (as-queue [t]
       (queue t))

     cljs.core.Cons
     (as-queue [this]
       (conj (queue) this))

     nil
     (as-queue [_]
       nil)))
