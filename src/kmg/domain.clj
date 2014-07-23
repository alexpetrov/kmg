(ns kmg.domain
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p]
    )
  (:use carica.core
        clojure.data
        kmg.datomic-helpers))

(declare in? user-goals-history media-backgrounds media-background-dataset)

(defn query [description f]
  (p/profile :info description
             (do (p/p :sync @(d/sync (conn)))
                 (p/p :call-domain-query (f)))))

(defn command [description f]
  (p/profile :info description
             (p/p :call-domain-command (f))))

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
          (d/history db) [:user/name user]))

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

(defn specialization-title [db spec-id]
  (:specialization/title (d/entity db spec-id)))

(defn check-if-spec-is-one-of-goal-history
  [db user spec]
  (let [goal-history (user-goals-history db user)]
    (if (not (in? goal-history spec))
      (throw (IllegalArgumentException.
              (str "Trying get recommendations for specialization that is not one of users history of goals; User: "
                   user "; Specialization: " (specialization-title db spec)))))))

;; (recommendations-for-user (db) "user2")
(defn recommendation-ids
  "Gives recommendation ids for user by specialization"
  [db user spec]
  (p/p :check-permission (check-if-spec-is-one-of-goal-history db user spec))
  (let [recs (recommendations-for-user db user spec)
        completed (set (recommendations-completed-by-user db user))]
    (remove completed recs)))

;; (recommendation-ids (db) "user1")
;; (recommendation-ids (db) "user2")
;;

(defn recommendation-dbid [db id]
  (ffirst (d/q '[:find ?rid
                 :in $ ?rid]
               db [:recommendation/id id])))

(defn media-id-by-recommendation-id [db recommend-id]
  (ffirst (d/q '[:find ?mid
              :in $ ?rid
              :where
              [?rid :recommendation/media ?mid]]
            db recommend-id)))

;;(media-id-by-recommendation-id (db) 17592186045434)

#_(defn media-id-by-dbid [db dbid]
  (ffirst (d/q '[:find ?id
                 :in $ ?dbid
                 :where
                 [?dbid :media/id ?id]]
                 db dbid)))

(defn media-dbid-by-id [db id]
  (ffirst (d/q '[:find ?dbid
                 :in $ ?dbid]
                 db [:media/id id])))

;;(media-id-by-dbid (db) 17592186045434)

(defn recommendation-data [db rid]
  (let [media-dbid (media-id-by-recommendation-id db rid)
        media-backgrounds (set (every-first (media-background-dataset db media-dbid)))]
    {:recommendation (entity db rid)
     :media (entity db media-dbid)
     :backgrounds (vec (map #(entity db %) media-backgrounds))}))

;;(recommendation-data (db) (recommendation-dbid db "spec1_book2"))
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


(defn children-specialization-ids [db spec]
  (->> (d/q '[:find ?specid
              :in $ ?parent-id
;;              :in $ ?parent-spec

              :where
;;              [?parent-id :specialization/id ?parent-spec]
              [?rid :specialization.relationship/to ?parent-id]
              [?rid :specialization.relationship/from ?specid]]
            db spec)
       every-first))
;; (children-specialization-ids (db) (get-spec-id (db) "spec1"))

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
(defn completed-specialization-ids
  "All specializations that were goals of this user and that are completed"
  [db user]
  (set (filter #(is-specialization-completed? db user %)
          (user-goals-history db user))))

;;(completed-specialization-ids (db) "user2")

(defn get-spec-id [db spec]
  (ffirst (d/q '[:find ?sid
         :in $ ?spec-id
         :where
         [?sid :specialization/id ?spec-id]]
        db spec)))
;; (get-spec-id (db) "spec1")

(defn available-specialization-ids
  [db user]
  (let [completed  (completed-specialization-ids db user)]
    (->> (map #(children-specialization-ids db %) completed)
         flatten
         (remove completed))))

;; (available-specialization-ids (db) "user1")
;; (get-spec-id (db) "spec2")

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn is-specialization-available? [db user spec]
  (let [available-specs (available-specialization-ids db user)
        spec-id (get-spec-id db spec)]
    (in? available-specs spec-id)))

;; (some #(= 1 %) [1 2 3]) ;:=> true
;;(is-specialization-available?  (db) "user1" "spec2")

#_(defn media-background-dataset [db media]
  (d/q '[:find ?mto
         :in $ ?mfr
         :where
         [?mrid :media.relationship/from ?mfr]
         [?mrid :media.relationship/to ?mto]
         [?mrid :media.relationship/type :background]]
       db [:media/id media]))

(defn media-background-dataset [db media-id]
  (d/q '[:find ?mto
         :in $ ?mfr
         :where
         [?mrid :media.relationship/from ?mfr]
         [?mrid :media.relationship/to ?mto]
         [?mrid :media.relationship/type :background]]
       db media-id))

(defn media-backgrounds
  ([db media] (media-backgrounds db (media-dbid-by-id db media) "dummy"))
  ([db media-id _] (let [media-prereq (set (every-first (media-background-dataset db media-id)))]
    (set (map #(:media/id (d/entity db %)) media-prereq)))))

(defn media-feedback-complete [db media user]
 (->> (d/q '[:find ?fid
         :in $ ?mid ?user-id
         :where
         [?rid :recommendation/media ?mid]
         [?fid :feedback/recommendation ?rid]
         [?fid :feedback/user ?user-id]
         [?fid :feedback/complete true]]
       db [:media/id media] [:user/name user])
      every-first))


(defn is-media-complete? [db media user]
  (let [complete-dataset (media-feedback-complete db media user)]
    (not (empty? complete-dataset))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands

(defn create-feedback
  [user recommendation]
  [{:feedback/user [:user/name user]
   :feedback/recommendation [:recommendation/id recommendation]
   :feedback/complete true
   :db/id (d/tempid :db.part/user)}])

;; (create-feedback "user1" "spec1_book2")

(defn mark-as-completed-command [user recommendation]
  (let [feedback (create-feedback user recommendation)]
    @(d/transact (conn) feedback)))


(defn change-goal-fact [user spec]
  [[:db/add [:user/name user] :user/goal [:specialization/id spec]]])

(defn change-goal-command [user spec]
  (if (not (is-specialization-available? (db) user spec))
    (throw (IllegalArgumentException. (str "Trying to change user goal to unavailable specialization; User: " user "; Specialization: " spec))))
  @(d/transact (conn)
      (change-goal-fact user spec)))
