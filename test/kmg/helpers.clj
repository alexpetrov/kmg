(ns kmg.helpers
  (:require
   [datomic.api :as d]
   [datomic-schema-grapher.dot :as dot]
   [datomic-schema-grapher.core :refer (graph-datomic)]
   [clojure.test :refer :all]
   [kmg.datomic-helpers :as dh])
  (:use
   carica.core
   clojure.test))

(defn before [f]
  (with-redefs [config (override-config :db {:url "datomic:mem://test"})]
    (dh/create-db-and-import-sample-data-for-prototype)
    (f)))

(defn show-schema []
  (before
   #(graph-datomic (dh/db-url) :save-as "kmg-schema.dot")))
;; (show-schema)

(defn reset-database []
  (before #(dh/reset)))
;; (reset-database)

(defn db []
  (d/db (d/connect (dh/db-url))))

(defn project-value [field seq]
  (map #(field %) seq))

(defn entity-values-by-ids [db field seq]
  (set (map #(field (d/entity db %)) seq)))
