(ns user.java.util.concurrent.model.integrant
  (:require
   [integrant.core :as ig]
   [user.timbre.alpha :as u.timbre]
   [user.java.util.concurrent :as concurrent]
   [user.java.lang.runtime :as runtime]
   )
  (:import
   java.util.concurrent.TimeUnit
   java.util.concurrent.ScheduledExecutorService
   ))


;; * pooled-scheduler


(defmethod ig/prep-key ::pooled-scheduler
  [_ pool-size]
  (if (int? pool-size)
    pool-size
    (inc (runtime/available-processors))))


(defmethod ig/init-key ::pooled-scheduler
  [_ pool-size]
  (concurrent/pooled-scheduler pool-size))


;; * fixed-rate


(defn fixed-rate-scheduler--default-ex-handler [_ident] (fn [_]))
(defn fixed-rate-scheduler--default-on-close [ident] (fn [] (u.timbre/info-halt (u.timbre/ident ident))))
(defn fixed-rate-scheduler--default-init-callback [ident] (fn [] (u.timbre/info-init (u.timbre/ident ident))))


(defmethod ig/init-key ::fixed-rate-scheduler
  [ident {:keys [scheduler task init-delay period time-unit on-error]
          :or   {on-error (fixed-rate-scheduler--default-ex-handler ident)}}]
  {:pre [(fn? task)
         (fn? on-error)]}
  (let [scheduler  (if (instance? ScheduledExecutorService scheduler)
                     scheduler
                     (concurrent/single-thread-scheduler))
        init-delay (cond
                     (delay? init-delay) (force init-delay)
                     :else               init-delay)
        time-unit  (if (instance? TimeUnit time-unit)
                     time-unit
                     concurrent/TIMEUNIT_MILLISECONDS)
        future     (concurrent/fixed-rate-schedule
                     scheduler
                     (fn []
                       (try
                         (task)
                         (catch Throwable e (on-error e))))
                     init-delay period time-unit)]
    (u.timbre/info-init (u.timbre/ident ident))
    future))


(defmethod ig/halt-key! ::fixed-rate-scheduler
  [ident future]
  (let [_ (concurrent/cancel! future false)]
    (u.timbre/info-halt (u.timbre/ident ident))))


;; * fixed-delay


(defn fixed-delay-scheduler--default-ex-handler [_ident] (fn [_]))
(defn fixed-delay-scheduler--default-on-close [ident] (fn [] (u.timbre/info-halt (u.timbre/ident ident))))
(defn fixed-delay-scheduler--default-init-callback [ident] (fn [] (u.timbre/info-init (u.timbre/ident ident))))


(defmethod ig/init-key ::fixed-delay-scheduler
  [ident {:keys [scheduler task init-delay delay time-unit on-error]
          :or   {on-error (fixed-delay-scheduler--default-ex-handler ident)}}]
  {:pre [(fn? task)
         (fn? on-error)]}
  (let [scheduler (if (instance? ScheduledExecutorService scheduler)
                    scheduler
                    (concurrent/single-thread-scheduler))
        init-delay (cond
                     (delay? init-delay) (force init-delay)
                     :else               init-delay)
        time-unit (if (instance? TimeUnit time-unit)
                    time-unit
                    concurrent/TIMEUNIT_MILLISECONDS)
        future    (concurrent/fixed-delay-schedule
                    scheduler
                    (fn []
                      (try
                        (task)
                        (catch Throwable e (on-error e))))
                    init-delay delay time-unit)]
    (u.timbre/info-init (u.timbre/ident ident))
    future))


(defmethod ig/halt-key! ::fixed-delay-scheduler
  [ident future]
  (let [_ (concurrent/cancel! future false)]
    (u.timbre/info-halt (u.timbre/ident ident))))
