(ns kmg.data
  (:require
    [datomic.api :as d]
    [clojure.edn :as edn]
    [clojure.java.io :as io])
  (:use carica.core
        kmg.schema
        kmg.datomic-helpers))

;; (defn db-url [] (config :db :url))

;; (d/create-database (db-url))

;; (defn conn [] (d/connect (db-url)))
;; (defn db [] (d/db (conn)))

;; (defn every-first [v]
;;   (for [elem v] (first elem)))

;; (defn entity [db id]
;;   (d/touch (d/entity db id)))
;; ;; (entity (db) 17592186045434)
;; ;; (entity (db) 17592186045429)


;;   #_(alter-var-root #'conn (constantly (d/connect (db-url))))
;;   @(d/transact (conn) kmg-schema))

;; (defn prepare-entity [data]
;;   (if (contains? data :db/id)
;;     data
;;     (merge data {:db/id (d/tempid :db.part/user)})))

;; (defn prepare-entities [data]
;;   (map prepare-entity data))

;; (defn import-knowledge-base-data [data-path]
;;   (let [sample-data (read-string (slurp data-path))]
;;     (doseq [data sample-data]
;;       @(d/transact (conn) (prepare-entities (val data))))))

;; (defn- reset []
;;   (d/release (conn))
;;   (d/delete-database (db-url))
;;   (d/create-database (db-url)))

;; (defn create-db-and-import-sample-data-for-prototype
;;   "This function creates schema and imports knowledge base data and users sample data
;; It needs only for prototype"
;;   []
;;   (let [data-path (config :sample-data-path)]
;;     (reset)
;;     (import-knowledge-base-data data-path)))
;; (reset)
;; (create-db-and-import-sample-data-for-prototype)
