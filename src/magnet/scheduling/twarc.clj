;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/

(ns magnet.scheduling.twarc
  (:require [diehard.core :as diehard]
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

(def ^:private props
  {:threadPool.class "org.quartz.simpl.SimpleThreadPool"
   :threadPool.threadCount 1
   :plugin.triggHistory.class "org.quartz.plugins.history.LoggingTriggerHistoryPlugin"
   :plugin.jobHistory.class "org.quartz.plugins.history.LoggingJobHistoryPlugin"})

(def ^:private default-name "main-scheduler")

(defmethod ig/init-key :magnet.scheduling/twarc [_ {:keys [postgres-cfg scheduler-name logger max-retries backoff-ms]
                                                    :or {scheduler-name default-name
                                                         max-retries default-max-retries
                                                         backoff-ms default-backoff-ms}}]
  (log logger :report ::starting-scheduler)
  (let [db-url (compose-db-url postgres-cfg)
        {:keys [user password]} postgres-cfg
        props (merge props {:jobStore.class "org.quartz.impl.jdbcjobstore.JobStoreTX"
                            :jobStore.driverDelegateClass "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate"
                            :jobStore.tablePrefix "qrtz_"
                            :jobStore.dataSource "db"
                            :dataSource.db.driver "org.postgresql.Driver"
                            :dataSource.db.URL db-url
                            :dataSource.db.user user
                            :dataSource.db.password password})]
    (diehard/with-retry {:retry-on Exception
                         :listener (listener logger max-retries)
                         :policy (retry-policy max-retries backoff-ms)
                         :fallback (fn [_ _] (fallback logger))}
      (let [scheduler (-> (twarc/make-scheduler props {:name scheduler-name})
                          (twarc/start))]
        (log logger :report ::scheduler-started)
        {:scheduler scheduler
         :logger logger}))))

(defmethod ig/halt-key! :magnet.scheduling/twarc [_ {:keys [scheduler logger]}]
  (when scheduler
    (twarc/stop scheduler))
  (log logger :report ::scheduler-stopped))
