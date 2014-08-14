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
     (query :recommendations
       (fn []
         (let [db (db)
               recommend-ids (take 4 (recommendation-ids db user (get-spec-id db spec)))]
                (map #(recommendation-data db % user) recommend-ids))))))

(defn recommendations-completed [user]
  (query :recommendations-completed
    (fn [] (let [db (db)
                 recommend-ids (take 10 (recommendations-completed-by-user db user))]
    (map #(recommendation-data db % user) recommend-ids)))))


(defn children-specializations
  "This function supposed to be used from presentation layer"
  [spec]
  (let [db (db)
        children-spec-ids (children-specialization-ids db (get-spec-id db spec))]
    (map #(entity db %) children-spec-ids)))
;;(children-specializations "spec1")

(defn specializations-completed [user]
  (query :specializations-completed
    (fn []
      (let [db (db)
            completed-specs (completed-specialization-ids db user)]
        (map #(entity db %) completed-specs)))))

;; (completed-specializations "user2")

(defn specializations-available [user]
  (query :specializations-available
    (fn []
      (let [db (db)
            available-specs (available-specialization-ids db user)]
        (map #(entity db %) available-specs)))))

;;(available-specializations "user2")

(defn whole-user-data [user]
  (p/profile :info :whole-user-data
             (do (p/p :sync @(d/sync (conn)))
                 {:recommendations (vec (recommendations user))
                  :recommendations-completed (vec (recommendations-completed user))
                  :specializations-available (vec (specializations-available user))
                  :specializations-completed (vec (specializations-completed user))})))

(defn domain []
  (domain-data (db)))
;; (domain)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands

(defn change-goal [user spec]
  (command :change-goal
           #(change-goal-command user spec)))

(defn mark-as-completed [user recommendation]
  (command :mark-as-completed
           #(mark-as-completed-command user recommendation)))

;; (mark-as-completed "user1" "spec1_book4")
