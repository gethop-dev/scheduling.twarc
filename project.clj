(defproject dev.gethop/scheduling.twarc "0.6.2-SNAPSHOT"
  :description "Duct library that provides Integrant keys for using Twarc scheduling library with persistent JobStore"
  :url "https://github.com/gethop-dev/scheduling.twarc"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :min-lein-version "2.9.8"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [diehard "0.10.2"]
                 [duct/core "0.8.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.1"]
                 [duct/migrator.ragtime "0.3.2"]
                 [integrant "0.8.0"]
                 [org.postgresql/postgresql "42.2.12"]
                 [twarc "0.1.15"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/CLOJARS_USERNAME
                                      :password :env/CLOJARS_PASSWORD
                                      :sign-releases false}]]
  :profiles
  {:dev [:project/dev :profiles/dev]
   :repl {:repl-options {:host "0.0.0.0"
                         :port 4001}}
   :profiles/dev {}
   :project/dev {:plugins [[jonase/eastwood "1.2.3"]
                           [lein-cljfmt "0.8.0"]]}})
