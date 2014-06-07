(defproject kmg "0.1.0-SNAPSHOT"
  :description "Knowledge Media Guide"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.datomic/datomic-free "0.9.4815"]
                 [datomic-schema-grapher "0.0.1"]]
  :plugins [[datomic-schema-grapher "0.0.1"]])
