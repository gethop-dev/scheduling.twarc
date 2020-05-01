(ns magnet.module.scheduling.twarc-pgsql
  (:require [clojure.java.io :as io]
            [duct.core :as core]
            [integrant.core :as ig]))

(def ^:private default-table
  "ragtime_scheduling_twarc")

(def ^:private ragtime-cfg-key
  [:duct.migrator/ragtime ::twarc-pgsql])

(def ^:private ragtime-config
  {ragtime-cfg-key
   {:database (ig/ref :duct.database/sql)
    :migrations [(ig/ref ::001-quartz-pgsql)]}
   [:duct.migrator.ragtime/sql ::001-quartz-pgsql]
   {:up [(io/resource "magnet.scheduling.twarc/migrations/001-quartz-pgsql.up.sql")]
    :down [(io/resource "magnet.scheduling.twarc/migrations/001-quartz-pgsql.down.sql")]}})

(defn- get-config
  [migrations-table]
  (assoc-in ragtime-config [ragtime-cfg-key :migrations-table] migrations-table))

(defmethod ig/prep-key :magnet.module.scheduling/twarc-pgsql [_ options]
  (assoc options ::requires (ig/ref :duct.module/sql)))

(defmethod ig/init-key :magnet.module.scheduling/twarc-pgsql [_ {:keys [migrations-table]
                                                                 :or {migrations-table default-table}
                                                                 :as options}]
  (fn [config]
    (core/merge-configs config (get-config migrations-table))))
