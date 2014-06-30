(ns kmg.data
  (:require
    [datomic.api :as d]
    [clojure.edn :as edn]
    [clojure.java.io :as io])
  (:use carica.core
        kmg.schema))

(def db-url (config :db :url))

(d/create-database db-url)

(defn conn [] (d/connect db-url))
(defn db [] (d/db (conn)))

(defn reset []
  (d/release (conn))
  (d/delete-database db-url)
  (d/create-database db-url)

  #_(alter-var-root #'conn (constantly (d/connect db-url)))
  @(d/transact (conn) kmg-schema))

(defn import-knowledge-base-data [data-path]
  (let [sample-data (read-string (slurp data-path))]
    (doseq [data sample-data]
      @(d/transact (conn) (val data)))))

(defn create-db-and-import-sample-data-for-prototype
  "This function creates schema and imports knowledge base data and users sample data
It needs only for prototype"
  []
  (let [data-path (config :sample-data-path)]
    (reset)
    (import-knowledge-base-data data-path)))

;; (reset)
;; (create-db-and-import-sample-data-for-prototype)
