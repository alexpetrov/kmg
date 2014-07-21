(defproject kmg "0.1.0-SNAPSHOT"
  :description "Knowledge Media Guide"
  :url "https://github.com/alexpetrov/kmg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2234"]
                 [com.datomic/datomic-free "0.9.4815.12"]
                 [datomic-schema-grapher "0.0.1"]
                 [enfocus "2.1.0-SNAPSHOT"]
                 [compojure "1.1.8"]
                 [sonian/carica "1.1.0" :exclusions [[cheshire]]]
                 [fogus/ring-edn "0.2.0"]
                 [cljs-ajax "0.2.3"]
                 [com.taoensso/timbre "2.7.1"]
                 ]
  :profiles {:dev {:plugins [[lein-cljsbuild "1.0.3"]]}}
  :plugins [[datomic-schema-grapher "0.0.1"]]
  :cljsbuild {
    :builds [{
        :source-paths ["src"]
        :compiler {
          :output-to "resources/public/js/main.js"
          :optimizations :whitespace
          :pretty-print true}}]}
  :ring {:handler kmg.core/app})
