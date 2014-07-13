(ns kmg.domain-facade
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p])
  (:use carica.core
        clojure.data
        kmg.domain
        kmg.datomic-helpers))

(defn users []
  (->> (d/q '[:find ?username
         :where
         [_ :user/name ?username]]
       (db))
      every-first))

(defn recommendations
  ([user]
     (recommendations user (user-current-goal-id (db) user)))
  ([user spec]
     (with-synchronized-db-do
       (fn []
         (let [db (db)
               recommend-ids (take 4 (recommendation-ids db user (get-spec-id db spec)))]
                (map #(recommendation-data db %) recommend-ids))))))


(defn recommendations-completed [user]
  (with-synchronized-db-do
    (fn [] (let [db (db)
                 recommend-ids (take 10 (recommendations-completed-by-user db user))]
    (map #(recommendation-data db %) recommend-ids)))))

(defn mark-as-completed [user recommendation]
  (let [db (db)
        feedback (create-feedback user recommendation)]
    (p/p :transact/feedback @(d/transact (conn) feedback))))

;; (mark-as-completed "user1" "spec1_book4")

(defn children-specializations
  "This function supposed to be used from presentation layer"
  [spec]
  (let [db (db)
        children-spec-ids (children-specialization-ids db spec)]
    (map #(entity db %) children-spec-ids)))
;;(children-specializations "spec1")

(defn completed-specializations [user]
  (with-synchronized-db-do
    (fn []
      (let [db (db)
            completed-specs (completed-specialization-ids db user)]
        (map #(entity db %) completed-specs)))))

;; (completed-specializations "user2")
