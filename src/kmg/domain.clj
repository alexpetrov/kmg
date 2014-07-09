(ns kmg.domain
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p]
    )
  (:use carica.core
        clojure.data
        kmg.datomic-helpers))

(defn users []
  (->> (d/q '[:find ?username
         :where
         [_ :user/name ?username]]
       (db))
      every-first))

(defn sort-by-second
  ([coll] (sort-by-second - coll))
  ([order coll] (sort #(order (compare (last %1) (last %2))) coll)))
;; (sort-by-second + [[1 200] [3 400] [2 300]])

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
(defn recommendations-for-user [db user]
  (->> (d/q '[:find ?id ?priority
         :in $ ?uid
         :where
         [?uid :user/name ?user]
         [?uid :user/goal ?sid]
         [?id :recommendation/specialization ?sid]
         [?id :recommendation/priority ?priority]
         [?mid :media/id ?media_id]]
       db [:user/name user])
       (sort-by-second)
       (every-first)))

(defn recommendation-ids [db user]
  (let [recs (recommendations-for-user db user)
        completed (set (recommendations-completed-by-user db user))]
    (filter #(not (contains? completed %)) recs)))

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

(defn recommendations [user]
  (with-syncronized-db-do
    (fn [] (let [db (db)
                 recommend-ids (take 4 (recommendation-ids db user))]
    (map #(recommendation-data db %) recommend-ids)))))

(defn recommendations-completed [user]
  (with-syncronized-db-do
    (fn [] (let [db (db)
                 recommend-ids (take 10 (recommendations-completed-by-user db user))]
    (map #(recommendation-data db %) recommend-ids)))))

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

(defn mark-as-completed [user recommendation]
  (let [db (db)
        feedback (create-feedback user recommendation)]
    (p/p :transact/feedback @(d/transact (conn) feedback))))

;; (mark-as-completed "user1" "spec1_book4")

(defn children-specialization-ids [db spec]
  (->> (d/q '[:find ?specid
              :in $ ?parent-spec
              :where
              [?parent-id :specialization/id ?parent-spec]
              [?rid :specialization.relationship/to ?parent-id]
              [?rid :specialization.relationship/from ?specid]] db spec)
       every-first))
;; (children-specialization-ids (db) "spec1")

(defn children-specializations [spec]
  (let [db (db)
        children-spec-ids (children-specialization-ids db spec)]
    (map #(entity db %) children-spec-ids)))
;;(children-specializations "spec1")
