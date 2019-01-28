(defproject magnet/scheduling.twarc "0.2.0"
  :description "Duct library that provides Integrant keys for using Twarc scheduling library with persistent JobStore"
  :url "https://github.com/magnetcoop/scheduling.twarc"
  :license {:name "Mozilla Public Licence 2.0"
            :url "https://www.mozilla.org/en-US/MPL/2.0/"}
  :dependencies [[diehard "0.7.2"]
                 [duct/core "0.7.0"]
                 [duct/module.logging "0.4.0"]
                 [duct/module.sql "0.5.0"]
                 [duct/migrator.ragtime "0.3.0"]
                 [integrant "0.7.0"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.3.443"]
                 [org.postgresql/postgresql "42.2.5"]
                 [twarc "0.1.12" :exclusions [org.clojure/core.async]]]
  :profiles
  {:dev {:plugins [[jonase/eastwood "0.3.4"]
                   [lein-cljfmt "0.6.2"]]}
   :repl {:repl-options {:host "0.0.0.0"
                         :port 4001}
          :plugins [[cider/cider-nrepl "0.18.0"]]}})
