(ns kmg.datomic-helpers
  (:require
   [datomic.api :as d]
   [datomic-schema-grapher.core :refer (graph-datomic)]
   [environ.core :refer [env]])
  (:use
   kmg.schema))

(defn db-url [] (env :database-url))

;;(d/create-database (db-url))

(defn conn [] (d/connect (db-url)))

(defn fresh-conn []
  (d/delete-database (db-url))
  (d/create-database (db-url))
  (d/connect (db-url)))

(defn db [] (d/db (conn)))

(defn every-first [v]
  (map first v))

(defn entity [db id]
  (->> (d/touch (d/entity db id))
         (remove #(or (set? (val %))
                      (= (type (val %)) datomic.query.EntityMap)))
         (into {})))

(defn prepare-entity [data]
  (if (contains? data :db/id)
    data
    (assoc data :db/id (d/tempid :db.part/user))))

(defn prepare-entities [data]
  (map prepare-entity data))

(defn import-kb-data [data-path conn]
  (let [sample-data (read-string (slurp data-path))]
    (d/transact conn kmg-schema)
    (doseq [data sample-data]
      (do #_(print data)
          @(d/transact conn (prepare-entities data))))))

(defn import-knowledge-base-data
  ([data-path]
     (d/create-database (db-url))
     (import-kb-data data-path (conn)))
  ([data-path conn]
     (import-kb-data data-path conn)))

(defn delete-database []
  (d/delete-database (db-url))
  (println "Database successfully deleted"))

(defn create-db-and-import-knowledge-base-4-it
  "This function creates schema and imports knowledge base for IT data and users sample data"
  []
  (let [data-path (env :real-data-path)]
    (time (import-knowledge-base-data data-path))
    (println "Knowledge base for IT imported. Press Ctrl+c to exit.")))
;;(delete-database)
;;(time (create-db-and-import-knowledge-base-4-it))

(defn create-db-and-import-sample-data
  "This function creates schema and imports knowledge base data and users sample data"
  []
  (let [data-path (env :sample-data-path)]
    (time (import-knowledge-base-data data-path))
    (println "Sample data imported. Press Ctrl+c to exit.")))
;; (time (create-db-and-import-sample-data))

(defn create-db-and-import-sample-data-for-test
  "This function creates schema and imports knowledge base data and users sample data
It needs only for test in memory database."
  []
  (let [data-path (env :sample-data-path)]
    (import-knowledge-base-data data-path (fresh-conn))))

(defn show-schema []
  (graph-datomic (db-url) :save-as "kmg-schema.dot"))

;;(show-schema)
