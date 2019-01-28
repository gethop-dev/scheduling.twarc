(ns magnet.module.scheduling.twarc-pgsql
  (:require [clojure.java.io :as io]
            [duct.core :as core]
            [integrant.core :as ig]))

(def ^:private ragtime-config
  {:duct.migrator/ragtime
   {:database (ig/ref :duct.database/sql)
    :migrations [(ig/ref :sql.migration/twarc-pgsql)]}
   [:duct.migrator.ragtime/sql :sql.migration/twarc-pgsql]
   {:up [(io/resource "migrations/001-quartz-pgsql.up.sql")]
    :down [(io/resource "migrations/001-quartz-pgsql.down.sql")]}})

(defmethod ig/prep-key :magnet.module.scheduling/twarc-pgsql [_ options]
  (assoc options ::requires (ig/ref :duct.module/sql)))

(defmethod ig/init-key :magnet.module.scheduling/twarc-pgsql [_ options]
  (fn [config]
    (core/merge-configs config ragtime-config)))
