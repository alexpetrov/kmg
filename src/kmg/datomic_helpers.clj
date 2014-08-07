(ns kmg.datomic-helpers
  (:require
   [datomic.api :as d])
  (:use
   kmg.schema
   carica.core))

(defn db-url [] (config :db :url))

#_(d/create-database (db-url))

(defn conn [] (d/connect (db-url)))

(defn fresh-conn []
  (d/delete-database (db-url))
  (d/create-database (db-url))
  (d/connect (db-url)))

(defn db [] (d/db (conn)))

(defn every-first [v]
  (for [elem v] (first elem)))

(defn entity [db id]
  (d/touch (d/entity db id)))

(defn prepare-entity [data]
  (if (contains? data :db/id)
    data
    (merge data {:db/id (d/tempid :db.part/user)})))

(defn prepare-entities [data]
  (map prepare-entity data))

(defn import-knowledge-base-data [data-path conn]
  (let [sample-data (read-string (slurp data-path))]
    (d/transact conn kmg-schema)
    (doseq [data sample-data]
      @(d/transact conn (prepare-entities (val data))))))

(defn reset []
  (d/release (conn))
  (d/delete-database (db-url))
  (d/create-database (db-url)))

(defn create-db-and-import-sample-data-for-prototype
  "This function creates schema and imports knowledge base data and users sample data
It needs only for prototype"
  []
  (let [data-path (config :sample-data-path)]
    #_(reset)
    (import-knowledge-base-data data-path (fresh-conn))))
;; (reset)
;; (time (create-db-and-import-sample-data-for-prototype))
