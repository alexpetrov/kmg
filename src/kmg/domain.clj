(ns kmg.domain
  (:require
    [datomic.api :as d]
    )
  (:use carica.core
        clojure.data))

(def db-url (config :db :url))

(defn conn [] (d/connect db-url))
(defn db [] (d/db (conn)))

(defn every-first [v]
  (for [elem v] (first elem)))

(defn users []
  (->> (d/q '[:find ?username
         :where
         [_ :user/name ?username]]
       (db))
      every-first))
;;#((for [user %] (first user)))
;; (users)

;; (every-first #{["user2"] ["user1"]})

(defn recommendations-comleted-by-user [db user]
  (->> (d/q '[:find ?id
         :in $ ?uid
         :where
         [?uid :user/name ?user]
         [?uid :user/goal ?sid]
         [?id :recommendation/specialization ?sid]
         [?id :recommendation/priority ?priority]
;;       [?id :recommendation/media ?mid]
         [?id :recommendation/id ?rid]
;;         [?completed_rec_id :recommendation/id ?rid]

         [?fid :feedback/user ?uid]
         [?fid :feedback/recommendation ?id]
         [?fid :feedback.comment/text ?comment]
         [?fid :feedback/complete true]

;;         [?mid :media/id ?media_id]
         ]
       db [:user/name user])
       (every-first)
       set))

(defn recommendations-for-user [db user]
  (->> (d/q '[:find ?id (max ?priority)
         :in $ ?uid
         :where
         [?uid :user/name ?user]
         [?uid :user/goal ?sid]
         [?id :recommendation/specialization ?sid]
         [?id :recommendation/priority ?priority]
;;         [?id :recommendation/media ?mid]
;;         [?id :recommendation/id ?rid]
;;         [?completed_rec_id :recommendation/id ?rid]

         [?mid :media/id ?media_id]
         ]
       db [:user/name user])
  (every-first)
  set))
;;(before #(recommendations-for-user (db) "user1"))

(defn recommendation-ids [db user]
  (let [recs (recommendations-for-user db user)
        completed (recommendations-comleted-by-user db user)]
    (first (diff recs completed))))

;; (recommendation-ids (db) "user2")
;; (before #(recommendations (db) "user1"))

(defn media-id-by-recommendation-id [db recommend-id]
  (ffirst (d/q '[:find ?mid
              :in $ ?rid
              :where
              [?rid :recommendation/media ?mid]]
            db recommend-id)))

;;(media-id-by-recommendation-id (db) 17592186045434)

(defn entity [db id]
  (d/touch (d/entity db id)))
;; (entity (db) 17592186045434)
;; (entity (db) 17592186045429)


(defn recommendations [user]
  (let [db (db)
        recommend-ids (recommendation-ids db user)]
    (into [] (for [rid recommend-ids]
       {:user user :recommendation (entity db rid) :media (entity db (media-id-by-recommendation-id db rid))}))
    #_(for [rid recommend-ids]
       {:recommendation (entity db rid) :media (entity db (media-id-by-recommendation-id db rid))})))

;;(recommendations "user1")
;;(recommendations "user2")
