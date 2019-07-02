;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.scheduling.twarc
  (:require [clojure.spec.alpha :as s]
            [diehard.core :as diehard]
            [duct.logger :refer [log]]
            [integrant.core :as ig]
            [twarc.core :as twarc]))

(def ^:const default-max-retries
  "Default limit of attempts to wait for qwartz tables to be created"
  10)

(def ^:const default-initial-delay
  "Initial delay for retries, specified in milliseconds."
  500)

(def ^:const default-max-delay
  "Maximun delay for a connection retry, specified in milliseconds. We
  are using truncated binary exponential backoff, with `max-delay` as
  the ceiling for the retry delay."
  10000)

(def ^:const default-backoff-ms
  [default-initial-delay default-max-delay 2.0])

(defn retry-policy [max-retries backoff-ms]
  (diehard/retry-policy-from-config
   {:max-retries max-retries
    :backoff-ms backoff-ms}))

(defn on-retry [logger max-retries]
  (let [remaining (- max-retries diehard/*executions*)]
    (log logger :report ::waiting-until-qwartz-tables-created
         {:retries-remaining remaining})))

(defn listener [logger max-retries]
  (diehard/listeners-from-config
   {:on-retry (fn [result-value exception-thrown]
                (on-retry logger max-retries))}))

(defn fallback [logger]
  (log logger :report ::cant-start-twarc-scheduler {:reason :tables-dont-exist}))

(defn- compose-db-url [{:keys [host port db user password]}]
  (format "jdbc:postgresql://%s:%s/%s?user=%s&password=%s"
          host port db user password))

(def ^:private default-thread-count
  "Default number of threads in the scheduler thread pool"
  ;; From Quartz scheduler documentation: "If you only
  ;; have a few jobs that fire a few times a day, then 1 thread is
  ;; plenty! If you have tens of thousands of jobs, with many firing
  ;; every minute, then you probably want a thread count more like 50 or
  ;; 100".
  ;; So set the default to some lower middle-ground.
  10)

(def ^:private props
  {:threadPool.class "org.quartz.simpl.SimpleThreadPool"
   :plugin.triggHistory.class "org.quartz.plugins.history.LoggingTriggerHistoryPlugin"
   :plugin.jobHistory.class "org.quartz.plugins.history.LoggingJobHistoryPlugin"})

(def ^:private default-name "main-scheduler")

(s/def ::postgres-cfg (s/keys ::req-un [host port db user password]))
(s/def ::scheduler-name string?)
(s/def ::pos-int? (s/and integer? pos?))
(s/def ::thread-count ::pos-int?)
(s/def ::logger #(satisfies? duct.logger/Logger %))
(s/def ::max-retries :retry/max-retries) ;; From diehard.spec
(s/def ::backoff-ms :retry/backoff-ms)   ;; From diehard.spec
(s/def ::config (s/keys :req-un [::postgres-cfg ::logger]
                        :opt-un [::scheduler-name ::thread-count ::max-retries ::backoff-ms]))

(defn start-scheduler [{:keys [postgres-cfg
                               scheduler-name
                               thread-count
                               logger
                               max-retries
                               backoff-ms]
                        :or {scheduler-name default-name
                             thread-count default-thread-count
                             max-retries default-max-retries
                             backoff-ms default-backoff-ms}
                        :as config}]
  {:pre [(s/valid? ::config config)]}
  (log logger :report ::starting-scheduler)
  (let [db-url (compose-db-url postgres-cfg)
        {:keys [user password]} postgres-cfg
        ;; Quartz tutorial suggests the following:
        ;;   If your Scheduler is busy (i.e. nearly always executing
        ;;   the same number of jobs as the size of the thread pool,
        ;;   then you should probably set the number of connections in
        ;;   the DataSource to be the about the size of the thread
        ;;   pool + 2.
        max-connections (+ 2 thread-count)
        props (merge props {:threadPool.threadCount thread-count
                            :jobStore.class "org.quartz.impl.jdbcjobstore.JobStoreTX"
                            :jobStore.driverDelegateClass "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate"
                            :jobStore.tablePrefix "qrtz_"
                            :jobStore.dataSource "db"
                            :dataSource.db.driver "org.postgresql.Driver"
                            :dataSource.db.URL db-url
                            :dataSource.db.user user
                            :dataSource.db.password password
                            :dataSource.db.maxconnections max-connections})]
    (diehard/with-retry {:retry-on Exception
                         :listener (listener logger max-retries)
                         :policy (retry-policy max-retries backoff-ms)
                         :fallback (fn [_ _] (fallback logger))}
      (let [scheduler (-> (twarc/make-scheduler props {:name scheduler-name})
                          (twarc/start))]
        (log logger :report ::scheduler-started)
        {:scheduler scheduler
         :logger logger}))))

(s/def ::start-scheduler-args (s/cat :config ::config))
(s/fdef start-scheduler
  :args ::start-scheduler-args)

(defmethod ig/init-key :magnet.scheduling/twarc [_ config]
  (start-scheduler config))

(defmethod ig/halt-key! :magnet.scheduling/twarc [_ {:keys [scheduler logger]}]
  (when scheduler
    (twarc/stop scheduler))
  (log logger :report ::scheduler-stopped))
