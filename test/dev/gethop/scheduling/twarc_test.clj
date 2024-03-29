;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns ^:integration dev.gethop.scheduling.twarc-test
  (:require [clojure.java.io :as io]
            [clojure.spec.test.alpha :as stest]
            [clojure.test :refer :all]
            [dev.gethop.scheduling.twarc]
            [duct.core :as duct]
            [integrant.core :as ig]
            [twarc.core :as twarc]))

(defn enable-instrumentation []
  (-> (stest/enumerate-namespace 'dev.gethop.scheduling.twarc) stest/instrument))

(defn get-config []
  (duct/load-hierarchy)
  (->
   (io/resource "resources/scheduling-config.edn")
   (duct/read-config)
   (duct/prep-config)))

(def config (get-config))

(defn migrate-quartz-tables []
  (->
   config
   (ig/init [:duct.migrator/ragtime])))

(defn get-scheduler []
  (->
   config
   (ig/init [:dev.gethop.scheduling/twarc])
   (:dev.gethop.scheduling/twarc)
   (:scheduler)))

(defn setup []
  (enable-instrumentation)
  (migrate-quartz-tables))

(defn teardown [])

(defn with-quartz-tables [f]
  (setup)
  (f)
  (teardown))

(use-fixtures :once with-quartz-tables)

(def my-counter (atom 0))

(twarc/defjob inc-counter-job [scheduler]
  (swap! my-counter inc))

(deftest config-test
  (is
   (let [count-to 10
         scheduler (get-scheduler)]
     (reset! my-counter 0)
     (inc-counter-job scheduler []
                      :trigger {:simple {:repeat (dec count-to) :interval 500}})
     (twarc/stop scheduler)
     (let [scheduler (get-scheduler)]
       (Thread/sleep 5000)
       (twarc/stop scheduler)
       (= @my-counter count-to)))
   (format "The job should've been ran exactly 10 times, not %d! And it should have had enough time to do so."
           @my-counter)))
