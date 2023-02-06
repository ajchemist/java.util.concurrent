(ns ajchemist.java.util.concurrent.integrant
  (:require
   [integrant.core :as ig]
   [ajchemist.java.util.concurrent :as concurrent]
   )
  (:import
   java.util.concurrent.TimeUnit
   java.util.concurrent.ScheduledExecutorService
   ))


(defn prep-scheduler-config
  [config]
  (-> config
    (update :scheduler
      #(if (instance? ScheduledExecutorService %)
         %
         (concurrent/single-thread-scheduler)))
    (update :init-delay
      #(cond
         (delay? %) (force %)
         :else      %))
    (update :time-unit
      #(if (instance? TimeUnit %)
         %
         concurrent/TIMEUNIT_MILLISECONDS))))


;; * pooled-scheduler


(defmethod ig/prep-key ::pooled-scheduler
  [_ pool-size]
  (if (int? pool-size) pool-size (inc (concurrent/available-processors))))


(defmethod ig/init-key ::pooled-scheduler
  [_ pool-size]
  (concurrent/pooled-scheduler pool-size))


;; * fixed-rate


(defmethod ig/prep-key ::fixed-rate-scheduler
  [_ config]
  (prep-scheduler-config config))


(defmethod ig/init-key ::fixed-rate-scheduler
  [_ {:keys [scheduler task init-delay period time-unit]}]
  {:pre [(fn? task)]}
  (concurrent/fixed-rate-schedule
    scheduler
    (fn [] (try (task) (catch Throwable _)))
    init-delay period time-unit))


(defmethod ig/halt-key! ::fixed-rate-scheduler
  [_ future]
  (let [_ (concurrent/cancel! future false)]
    ))


;; * fixed-delay


(defmethod ig/prep-key ::fixed-delay-scheduler
  [_ config]
  (prep-scheduler-config config))


(defmethod ig/init-key ::fixed-delay-scheduler
  [_ {:keys [scheduler task init-delay delay time-unit]}]
  {:pre [(fn? task)]}
  (concurrent/fixed-delay-schedule
    scheduler
    (fn [] (try (task) (catch Throwable _)))
    init-delay delay time-unit))


(defmethod ig/halt-key! ::fixed-delay-scheduler
  [_ future]
  (let [_ (concurrent/cancel! future false)]
    ))
