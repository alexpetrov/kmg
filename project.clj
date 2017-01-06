(defproject kmg "0.1.0-SNAPSHOT"
  :description "Knowledge Media Guide"
  :url "https://github.com/alexpetrov/kmg"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.cognitect/transit-clj "0.8.259"]
                 [com.datomic/datomic-free "0.9.5078"]
                 [datomic-schema-grapher "0.0.1"]
                 [compojure "1.2.2"]
                 [http-kit "2.1.18"]
                 [enlive "1.1.6"]
                 [javax.servlet/servlet-api "2.5"]
                 [environ "1.0.0"]
                 [com.taoensso/timbre "3.3.1"]
                 ]
  :plugins [[datomic-schema-grapher "0.0.1"]
            [lein-environ "1.0.0"]]

  :ring {:handler kmg.core/app}
  :main kmg.core
  :aot [kmg.core])
