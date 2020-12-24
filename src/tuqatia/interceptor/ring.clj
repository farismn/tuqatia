(ns tuqatia.interceptor.ring
  (:require
   [tuqatia.interceptor :as tqt.icept]))

(defn ring-interceptor
  []
  {:name  ::ring
   :enter (fn [ctx]
            (let [queue   (:queue ctx)
                  stack   (:stack ctx)
                  request (dissoc ctx :queue :stack)]
              {:request request
               :queue   queue
               :stack   stack}))
   :leave :response
   :error :response})

(defrecord HandlerInterceptor [id handler]
  tqt.icept/AsInterceptor
  (as-interceptor [_]
    {:name  id
     :enter (fn [ctx]
              (let [request  (:request ctx)
                    response (handler request)]
                (assoc ctx :response response)))}))

(defn handler-interceptor
  [m]
  (map->HandlerInterceptor m))
