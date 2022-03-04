(ns user.java.util.concurrent
  (:require
   [user.java.lang.runtime :refer [available-processors]]
   )
  (:import
   java.lang.Runnable
   java.util.Collection
   java.util.concurrent.TimeUnit
   java.util.concurrent.Executors
   java.util.concurrent.ExecutorService
   java.util.concurrent.ThreadFactory
   java.util.concurrent.ScheduledExecutorService
   java.util.concurrent.ScheduledFuture
   java.util.concurrent.ScheduledThreadPoolExecutor
   ))


(defn fixed-thread-pool
  ([]
   (fixed-thread-pool (+ 2 (available-processors))))
  ([nthreads]
   (Executors/newFixedThreadPool nthreads))
  ([nthreads ^ThreadFactory thread-factory]
   (Executors/newFixedThreadPool nthreads thread-factory)))


(defn submit
  {:style/indent [:defn]}
  [^ExecutorService pool ^Runnable task]
  (. pool submit task))


(defn invoke-all
  ([^ExecutorService pool ^Collection tasks]
   (. pool invokeAll tasks))
  ([^ExecutorService pool ^Collection tasks ^Long timeout ^TimeUnit unit]
   (. pool invokeAll tasks timeout unit)))


;; * time-unit


(def ^TimeUnit TIMEUNIT_NANOSECONDS TimeUnit/NANOSECONDS)
(def ^TimeUnit TIMEUNIT_MICROSECONDS TimeUnit/MICROSECONDS)
(def ^TimeUnit TIMEUNIT_MILLISECONDS TimeUnit/MILLISECONDS)
(def ^TimeUnit TIMEUNIT_SECONDS TimeUnit/SECONDS)
(def ^TimeUnit TIMEUNIT_MINUTES TimeUnit/MINUTES)
(def ^TimeUnit TIMEUNIT_HOURS TimeUnit/HOURS)
(def ^TimeUnit TIMEUNIT_DAYS TimeUnit/DAYS)


;; * future


(defprotocol IFuture
  (cancel! [this may-interrupt-if-running])
  (^Boolean cancelled? [this])
  (^Boolean done? [this]))


;; * scheduled executors


(defn ^ScheduledExecutorService single-thread-scheduler
  []
  (Executors/newSingleThreadScheduledExecutor))


(defn ^ScheduledThreadPoolExecutor
  pooled-scheduler
  [pool-size]
  #_(Executors/newScheduledThreadPool pool-size)
  (ScheduledThreadPoolExecutor. pool-size))


(defprotocol IScheduler
  (schedule [scheduler f delay] [scheduler f delay time-unit])
  (^ScheduledFuture fixed-rate-schedule [scheduler f init-delay period] [scheduler f init-delay period time-unit])
  (^ScheduledFuture fixed-delay-schedule [scheduler f init-delay delay] [scheduler f init-delay delay time-unit])
  (shutdown [scheduler])
  (shutdown-sync [scheduler]))


(alter-meta! #'schedule assoc :style/indent [:defn])
(alter-meta! #'fixed-rate-schedule assoc :style/indent [:defn])
(alter-meta! #'fixed-delay-schedule assoc :style/indent [:defn])


(extend-type ScheduledFuture
  IFuture
  (cancel! [this may-interrupt-if-running] (. this cancel may-interrupt-if-running))
  (cancelled? [this] (. this isCancelled))
  (done? [this] (. this isDone)))


(extend-type ScheduledExecutorService
  IScheduler
  (schedule
    ([scheduler f delay]
     (schedule scheduler f delay TimeUnit/MILLISECONDS))
    ([scheduler ^Runnable f delay ^TimeUnit time-unit]
     (. scheduler schedule f (long delay) time-unit)))
  (fixed-rate-schedule
    ([scheduler f init-delay period]
     (fixed-rate-schedule scheduler f init-delay period TimeUnit/MILLISECONDS))
    ([scheduler ^Runnable f init-delay period ^TimeUnit time-unit]
     (. scheduler scheduleAtFixedRate f (long init-delay) period time-unit)))
  (fixed-delay-schedule
    ([scheduler f init-delay delay]
     (fixed-delay-schedule scheduler f init-delay delay TimeUnit/MILLISECONDS))
    ([scheduler ^Runnable f init-delay delay ^TimeUnit time-unit]
     (. scheduler scheduleWithFixedDelay f (long init-delay) delay time-unit)))
  (shutdown
    [scheduler]
    (. scheduler shutdown))
  (shutdown-sync
    [scheduler]
    (. scheduler shutdownNow)))


(comment
  (instance? java.util.concurrent.Callable (fn []))
  TimeUnit/MILLISECONDS
  TimeUnit/SECONDS
  )
