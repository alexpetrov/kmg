(defproject kmg "0.1.0-SNAPSHOT"
  :description "Knowledge Media Guide"
  :url "https://github.com/alexpetrov/kmg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2371"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.datomic/datomic-free "0.9.5078"]
                 [datomic-schema-grapher "0.0.1"]
                 [enfocus "2.1.0"]
                 [compojure "1.2.0"]
                 [http-kit "2.1.18"]
                 [javax.servlet/servlet-api "2.5"]
                 [environ "1.0.0"]
                 [cljs-ajax "0.3.3"]
                 [com.taoensso/timbre "3.3.1"]
                 [figwheel "0.1.5-SNAPSHOT"]
                 ]
  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-figwheel "0.1.5-SNAPSHOT"]
            [datomic-schema-grapher "0.0.1"]
            [lein-environ "1.0.0"]]

  :cljsbuild {
              :builds [{:id "dev"
                        :source-paths ["src" "src-cljs-dev"]
                        :compiler {:output-to "resources/public/js/compiled/main.js"
                                   :output-dir "resources/public/js/compiled/out"
                                   :optimizations :none
                                   :source-map true}}
                       {:id "min"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  :ring {:handler kmg.core/app}
  :main kmg.core
  :aot [kmg.core]
  :figwheel {
             :http-server-root "public" ;; default and assumes "resources"
             :server-port 3449 ;; default
             :css-dirs ["public/resources/css"] ;; watch and update CSS
             :ring-handler kmg.core/app
             })
