(ns kmg.domain
  (:require
    [datomic.api :as d]
    [taoensso.timbre.profiling :as p])
  (:use clojure.data
        kmg.datomic-helpers))

(declare in? user-specializations-history media-backgrounds media-background-dataset is-media-complete?)

(defn query [description f]
  (p/profile :info description
             (p/p :call-domain-query (f))))

(defn command [description f]
  (p/profile :info description
             (p/p :call-domain-command (f))))

(defn sort-by-second
  ([coll] (sort-by-second - coll))
  ([order coll] (sort #(order (compare (last %1) (last %2))) coll)))

(defn domain-data [db]
  (->> (d/q '[:find ?did
             :where
             [?did :domain/id]]
           db)
      ffirst
      (entity db)))

(defn user-current-specialization [db user]
  (ffirst (d/q '[:find ?sid
                 :in $ ?userid
                 :where
                 [?userid :user/specialization ?sid]]
                db [:user/name user])))

(defn user-current-specialization-id [db user]
  (:specialization/id (d/entity db (user-current-specialization db user))))

(defn user-specializations-history-dataset
  [db user]
   (d/q '[:find ?sid ?timestamp
          :in $ ?userid
          :where
          [?userid :user/specialization ?sid ?tx]
          [?tx :db/txInstant ?timestamp]]
          (d/history db) [:user/name user]))

(defn count-incompleted-recomendations-in-spec [db user spec]
  (ffirst (d/q '[:find (count ?rec-id)
                 :in $ ?user-id ?spec-id
                 :where
                 [?fid :feedback/user ?user-id]
                 [?fid :feedback/recommendation ?rec-id]]
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

(defn check-if-spec-is-one-of-specialization-history
  [db user spec]
  (let [specialization-history (user-specializations-history db user)]
    (if (not (in? specialization-history spec))
      (throw (IllegalArgumentException.
              (str "Trying get recommendations for specialization that is not one of users history of specializations; User: "
                   user "; Specialization: " (specialization-title db spec)))))))

(defn recommendation-ids
  "Gives recommendation ids for user by specialization"
  [db user spec]
  (p/p :check-permission (check-if-spec-is-one-of-specialization-history db user spec))
  (let [recs (recommendations-for-user db user spec)
        completed (set (recommendations-completed-by-user db user))]
    (remove completed recs)))

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

(defn media-dbid-by-id [db id]
  (ffirst (d/q '[:find ?dbid
                 :in $ ?id
                 :where
                 [?dbid :media/id ?id]]
                 db id)))

(def translation-rule
  '[[(translation ?mid1 ?mid2)
     [?rid :media.relationship/from ?mid1]
     [?rid :media.relationship/to ?mid2]
     [?rid :media.relationship/type :translation]]
    [(translation ?mid1 ?mid2)
     [?rid :media.relationship/from ?mid2]
     [?rid :media.relationship/to ?mid1]
     [?rid :media.relationship/type :translation]]])

(defn media-translations-dataset [db media-id]
  (d/q '[:find ?mid2
         :in $ % ?mid
         :where
         (translation ?mid ?mid2)
         [(< ?mid ?mid2)]]
       db translation-rule media-id))

(defn media-translations [db media-id]
  (set (every-first (media-translations-dataset db media-id))))

(defn media-translation-data [db media-id]
  {:media (entity db media-id)})

(defn authors-dataset [db media-id]
  (d/q '[:find ?aid
         :in $ ?media-id
         :where
         [?media-id :media/author ?aid]]
       db media-id))

(defn media-authors [db media-id]
  (set (every-first (authors-dataset db media-id))))

(defn author-data [db author-id]
  {:author (entity db author-id)})

(defn background-data [db media-id user]
  {:media (entity db media-id) :completed (is-media-complete? db media-id user)})

(defn recommendation-data [db rid user]
  (let [media-dbid (media-id-by-recommendation-id db rid)
        media-backgrounds (media-backgrounds db media-dbid)
        media-translations (media-translations db media-dbid)
        media-authors (media-authors db media-dbid)]
    {:recommendation (entity db rid)
     :media (entity db media-dbid)
     :authors (vec (map #(author-data db %) media-authors))
     :backgrounds (vec (map #(background-data db % user) media-backgrounds))
     :translations (vec (map #(media-translation-data db %) media-translations))}))

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

(defn children-specialization-ids [db spec]
  (->> (d/q '[:find ?specid
              :in $ ?parent-id
              :where
              [?rid :specialization.relationship/to ?parent-id]
              [?rid :specialization.relationship/from ?specid]]
            db spec)
       every-first))

(defn required-recommendation-ids [db spec]
  (->> (d/q '[:find ?rid
              :in $ ?spec-id
              :where
              [?rid :recommendation/specialization ?spec-id]
              [?rid :recommendation/necessary true]]
              db spec)
       (every-first)
       set))

(defn is-specialization-completed?
  "Specialization is completed if all required recommendations are completed by the user"
  [db user spec]
  (let [required-recommendations (required-recommendation-ids db spec)
        completed-recommendations (set (recommendations-completed-by-user db user))]
    (clojure.set/subset? required-recommendations completed-recommendations)))


(defn user-specializations-history
  "All specializations, that were specializations of this user, ordered by date desc"
  [db user]
  (->> (user-specializations-history-dataset db user)
       (sort-by-second -)
       (every-first)))

(defn completed-specialization-ids
  "All specializations that were specializations of this user and that are completed"
  [db user]
  (set (filter #(is-specialization-completed? db user %)
          (user-specializations-history db user))))

(defn get-spec-id [db spec]
  (ffirst (d/q '[:find ?sid
         :in $ ?spec-id
         :where
         [?sid :specialization/id ?spec-id]]
        db spec)))

(defn available-specialization-ids
  [db user]
  (let [completed (completed-specialization-ids db user)]
    (->> (map #(children-specialization-ids db %) completed)
         flatten
         (remove completed))))

(defn in?
  "true if seq contains elm"
  [seq elm]
  (some #(= elm %) seq))

(defn is-specialization-available? [db user spec]
  (let [available-specs (available-specialization-ids db user)
        spec-id (get-spec-id db spec)]
    (in? available-specs spec-id)))

(defn media-background-dataset [db media-id]
  (d/q '[:find ?mto
         :in $ ?mfr
         :where
         [?mrid :media.relationship/from ?mfr]
         [?mrid :media.relationship/to ?mto]
         [?mrid :media.relationship/type :background]]
       db media-id))

(defn media-backgrounds
    [db media-id]
    (set (every-first (media-background-dataset db media-id))))

(defn media-feedback-complete [db media user]
 (->> (d/q '[:find ?fid
         :in $ ?mid ?user-id
         :where
         [?rid :recommendation/media ?mid]
         [?fid :feedback/recommendation ?rid]
         [?fid :feedback/user ?user-id]
         [?fid :feedback/complete true]]
       db media [:user/name user])
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

(defn mark-as-completed-command [user recommendation]
  (let [feedback (create-feedback user recommendation)]
    @(d/transact (conn) feedback)))


(defn change-specialization-fact [user spec]
  [[:db/add [:user/name user] :user/specialization [:specialization/id spec]]])

(defn change-specialization-command [user spec]
  (if (not (is-specialization-available? (db) user spec))
    (throw (IllegalArgumentException. (str "Trying to change user specialization to unavailable specialization; User: " user "; Specialization: " spec))))
  @(d/transact (conn)
      (change-specialization-fact user spec)))
