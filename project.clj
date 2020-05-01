(defproject magnet/scheduling.twarc "0.5.0"
  :description "Duct library that provides Integrant keys for using Twarc scheduling library with persistent JobStore"
  :url "https://github.com/magnetcoop/scheduling.twarc"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :min-lein-version "2.9.0"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [diehard "0.9.4"]
                 [duct/core "0.8.0"]
                 [duct/module.logging "0.5.0"]
                 [duct/module.sql "0.6.0"]
                 [duct/migrator.ragtime "0.3.2"]
                 [integrant "0.8.0"]
                 [org.postgresql/postgresql "42.2.12"]
                 [twarc "0.1.13"]]
  :deploy-repositories [["snapshots" {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]
                        ["releases"  {:url "https://clojars.org/repo"
                                      :username :env/clojars_username
                                      :password :env/clojars_password
                                      :sign-releases false}]]
  :profiles
  {:dev [:project/dev :profiles/dev]
   :repl {:repl-options {:host "0.0.0.0"
                         :port 4001}}
   :profiles/dev {}
   :project/dev {:plugins [[jonase/eastwood "0.3.11"]
                           [lein-cljfmt "0.6.7"]]}})
