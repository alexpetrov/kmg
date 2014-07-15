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
     (with-synchronized-db-do :recommendations
       (fn []
         (let [db (db)
               recommend-ids (take 4 (recommendation-ids db user (get-spec-id db spec)))]
                (map #(recommendation-data db %) recommend-ids))))))


(defn recommendations-completed [user]
  (with-synchronized-db-do :recommendations-completed
    (fn [] (let [db (db)
                 recommend-ids (take 10 (recommendations-completed-by-user db user))]
    (map #(recommendation-data db %) recommend-ids)))))


(defn children-specializations
  "This function supposed to be used from presentation layer"
  [spec]
  (let [db (db)
        children-spec-ids (children-specialization-ids db (get-spec-id db spec))]
    (map #(entity db %) children-spec-ids)))
;;(children-specializations "spec1")

(defn specializations-completed [user]
  (with-synchronized-db-do :specializations-completed
    (fn []
      (let [db (db)
            completed-specs (completed-specialization-ids db user)]
        (map #(entity db %) completed-specs)))))

;; (completed-specializations "user2")

(defn specializations-available [user]
  (with-synchronized-db-do :specializations-available
    (fn []
      (let [db (db)
            available-specs (available-specialization-ids db user)]
        (map #(entity db %) available-specs)))))

;;(available-specializations "user2")

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands

(defn change-goal [user spec]
  (p/profile :info :change-goal
             (p/p :command (change-goal-command user spec))))

(defn mark-as-completed [user recommendation]
  (p/profile :info :mark-as-completed
             (p/p :command (mark-as-completed-command user recommendation))))

;; (mark-as-completed "user1" "spec1_book4")
