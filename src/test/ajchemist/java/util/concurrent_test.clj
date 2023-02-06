(ns ajchemist.java.util.concurrent-test
  (:require
   [clojure.test :as test :refer [deftest is are testing]]
   [ajchemist.java.util.concurrent :as concurrent]
   ))


(defonce vol-01 (volatile! 0))
(defonce vol-02 (volatile! 0))


(deftest fixed-rate-scheduler
  (let [_         (vreset! vol-01 0)
        scheduler (concurrent/fixed-rate-schedule
                    (concurrent/single-thread-scheduler)
                    (fn [] (try (println ::fixed-rate (System/currentTimeMillis))
                               (vswap! vol-01 inc)
                               (catch Throwable _)))
                    250
                    250)]
    ;; 1. 250
    ;; 2. 500
    ;; 3. 750
    ;; 4. 1000
    (Thread/sleep 1010)
    (concurrent/cancel! scheduler false)
    (is (= @vol-01 4))))


(deftest fixed-delay-scheduler
  (let [_         (vreset! vol-02 0)
        scheduler (concurrent/fixed-delay-schedule
                    (concurrent/single-thread-scheduler)
                    (fn [] (try (println ::fixed-delay (System/currentTimeMillis))
                               (vswap! vol-02 inc)
                               (Thread/sleep 250)
                               (catch Throwable _)))
                    0
                    250)]
    ;; 1. +1 - 0
    ;; 2. end - 250
    ;; 3. +1 - 500
    ;; 4. end - 750
    ;; 5. +1 - 1000
    (Thread/sleep 990)
    (concurrent/cancel! scheduler false)
    (is (= @vol-02 2))))
