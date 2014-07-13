(ns kmg.domain
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p]
    )
  (:use carica.core
        clojure.data
        kmg.datomic-helpers))

(defn sort-by-second
  ([coll] (sort-by-second - coll))
  ([order coll] (sort #(order (compare (last %1) (last %2))) coll)))

;; (sort-by-second + [[1 200] [3 400] [2 300]])
(defn user-current-goal [db user]
  (ffirst (d/q '[:find ?sid
                 :in $ ?userid
                 :where
                 [?userid :user/goal ?sid]]
                db [:user/name user])))

(defn user-current-goal-id [db user]
  (:specialization/id (d/entity db (user-current-goal db user))))

;;(user-current-goal (db) "user1")

(defn user-goals-history-dataset
  [db user]
   (d/q '[:find ?sid ?timestamp
          :in $ ?userid
          :where
          [?userid :user/goal ?sid ?tx]
          [?tx :db/txInstant ?timestamp]]
          db [:user/name user]))

(defn count-incompleted-recomendations-in-spec [db user spec]
  (ffirst (d/q '[:find (count ?rec-id)
                 :in $ ?user-id ?spec-id
                 :where
                 [?fid :feedback/user ?user-id]
                 [?fid :feedback/recommendation ?rec-id]
                 []]
               db [:user/id user] [:spec/id spec])))


(defn recommendations-completed-by-user-dataset [db user]
  (d/q '[:find ?id ?timestamp
         :in $ ?uid
         :where
         [?fid :feedback/user ?uid]
         [?fid :feedback/recommendation ?id]
         [?fid :feedback/complete true ?tx]
         [?tx :db/txInstant ?timestamp]]
       db [:user/name user]))

(defn recommendations-completed-by-user [db user]
  (->> (recommendations-completed-by-user-dataset db user)
       (sort-by-second -)
       (every-first)))

;; (recommendations-completed-by-user-dataset (db) "user2")
;; (recommendations-completed-by-user (db) "user2")
(defn recommendations-for-user [db user spec]
  (->> (d/q '[:find ?id ?priority
         :in $ ?uid ?sid
         :where
         [?uid :user/name ?user]
         [?id :recommendation/specialization ?sid]
         [?id :recommendation/priority ?priority]
         [?mid :media/id ?media_id]]
       db [:user/name user] spec)
       (sort-by-second)
       (every-first)))

;; (recommendations-for-user (db) "user2")
(defn recommendation-ids
  "Gives recommendation ids for user by specialization"
  [db user spec]
     (let [recs (recommendations-for-user db user spec)
        completed (set (recommendations-completed-by-user db user))]
    (remove completed recs)))

;; (recommendation-ids (db) "user1")
;; (recommendation-ids (db) "user2")

(defn media-id-by-recommendation-id [db recommend-id]
  (ffirst (d/q '[:find ?mid
              :in $ ?rid
              :where
              [?rid :recommendation/media ?mid]]
            db recommend-id)))

;;(media-id-by-recommendation-id (db) 17592186045434)

(defn recommendation-data [db rid]
  {:recommendation (entity db rid) :media (entity db (media-id-by-recommendation-id db rid))})

(defn with-syncronized-db-do [f]
  (p/p :sync @(d/sync (conn)))
  (p/p :call-domain-function (f)))

;;(recommendations "user1")
;;(recommendations "user2")

(defn- get-feedback
  [user recommendation]
  (->> (d/q '[:find ?fid
              :in $ ?user ?recommend
              :where
              [?uid :user/name ?user]
              [?rid :recommendation/id ?recommend]
              [?fid :feedback/user ?uid]
              [?fid :feedback/recommendation ?rid]]
            (db) user recommendation)
       ffirst
       (entity (db))))

;; (get-feedback "user1" "spec1_book4")

(defn create-feedback
  [user recommendation]
  [{:feedback/user [:user/name user]
   :feedback/recommendation [:recommendation/id recommendation]
   :feedback/complete true
   :db/id (d/tempid :db.part/user)}])

;; (create-feedback "user1" "spec1_book2")

(defn children-specialization-ids [db spec]
  (->> (d/q '[:find ?specid
              :in $ ?parent-spec
              :where
              [?parent-id :specialization/id ?parent-spec]
              [?rid :specialization.relationship/to ?parent-id]
              [?rid :specialization.relationship/from ?specid]] db spec)
       every-first))
;; (children-specialization-ids (db) "spec1")

(defn required-recommendation-ids [db spec]
  (->> (d/q '[:find ?rid
              :in $ ?spec-id
              :where
              [?rid :recommendation/specialization ?spec-id]
              [?rid :recommendation/necessary true]]
              db spec)
       (every-first)
       set))
;; (required-recommendation-ids (db) "spec1")

;; set of required recommendations for this specialization is a subset of comleted recommendations for this user
(defn is-specialization-completed?
  "Specialization is completed if all required recommendations are completed by the user"
  [db user spec]
  (let [required-recommendations (required-recommendation-ids db spec)
        completed-recommendations (set (recommendations-completed-by-user db user))]
    (clojure.set/subset? required-recommendations completed-recommendations)))


(defn user-goals-history
  "All specializations, that were goals of this user, in ordered by date desc"
  [db user]
  (->> (user-goals-history-dataset db user)
       (sort-by-second -)
       (every-first)))

;;(user-goals-history (db) "user2")
(defn completed-specializations
  "All specializations that were goals of this user and that are completed"
  [db user]
  (set (filter #(is-specialization-completed? db user %)
          (user-goals-history db user))))

;;(completed-specializations (db) "user2")

(defn get-spec-id [db spec]
  (ffirst (d/q '[:find ?sid
         :in $ ?spec-id
         :where
         [?sid :specialization/id ?spec-id]]
        db spec)))
;; (get-spec-id (db) "spec1")

(defn available-specialization-ids
  [db user]
  (flatten (map #(conj (children-specialization-ids db %) (get-spec-id db %)) (completed-specializations db user))))
;; (available-specializations "user2")
