(ns tuqatia.core
  (:require
   [tuqatia.async :as tqt.a]
   [tuqatia.context :as tqt.ctx]
   [tuqatia.queue :as tqt.q]))

(defn- -try
  [ctx f]
  (if (nil? f)
    ctx
    (try
      (let [result (f ctx)]
        (if (tqt.a/async? result)
          (tqt.a/rescue result #(assoc ctx :error %))
          result))
      (catch Throwable error
        (assoc ctx :error error)))))

(defn- invalid-context-type!
  [ctx phase]
  (throw (ex-info (str "Invalid Context on: " phase) {:ctx ctx})))

(defn- run-enter
  [ctx]
  (cond
    (tqt.a/async? ctx)
    (tqt.a/continue ctx run-enter)

    (tqt.ctx/context? ctx)
    (let [queue       (:queue ctx)
          stack       (:stack ctx)
          interceptor (peek queue)]
      (if (or (nil? interceptor) (some? (:error ctx)))
        ctx
        (recur (let [enter-f (:enter interceptor)]
                 (-> ctx
                     (assoc :queue (pop queue)
                            :stack (conj stack interceptor))
                     (-try enter-f))))))

    :else
    (invalid-context-type! ctx :enter)))

(defn- run-leave
  [ctx]
  (cond
    (tqt.a/async? ctx)
    (tqt.a/continue ctx run-leave)

    (tqt.ctx/context? ctx)
    (let [stack       (:stack ctx)
          interceptor (peek stack)]
      (if (nil? interceptor)
        ctx
        (recur (let [leave-f (if (some? (:error ctx))
                               (:error interceptor)
                               (:leave interceptor))]
                 (-> ctx
                     (assoc :stack (pop stack))
                     (-try leave-f))))))

    :else
    (invalid-context-type! ctx :leave)))

(defn- sanitize-result
  [ctx]
  (dissoc ctx :queue :stack))

(defn- wait-for-result
  [ctx]
  (cond
    (tqt.a/async? ctx)
    (recur (tqt.a/wait-for ctx))

    (tqt.ctx/context? ctx)
    (if-let [error (:error ctx)]
      (throw error)
      (sanitize-result ctx))

    :else
    (invalid-context-type! ctx :result)))

(defn- deliver-result
  [ctx on-complete on-error]
  (cond
    (tqt.a/async? ctx)
    (tqt.a/continue ctx #(deliver-result % on-complete on-error))

    (tqt.ctx/context? ctx)
    (if (some?  (:error ctx))
      (on-error (sanitize-result ctx))
      (on-complete (sanitize-result ctx)))

    :else
    (invalid-context-type! ctx :result)))

(defn execute
  ([interceptors m]
   (if-let [queue (tqt.q/as-queue interceptors)]
     (-> (assoc m :queue queue :stack (list))
         (run-enter)
         (run-leave)
         (wait-for-result))
     m))
  ([interceptors m on-complete on-error]
   (if-let [queue (tqt.q/as-queue interceptors)]
     (try
       (-> (assoc m :queue queue :stack (list))
           (run-enter)
           (run-leave)
           (deliver-result on-complete on-error))
       (catch Exception error
         (on-error error)))
     (on-complete m))))

(defn halt
  [ctx]
  (tqt.ctx/halt ctx))
