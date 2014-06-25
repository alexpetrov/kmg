(ns kmg.domain
  (:require
    [datomic.api :as d]
    )
  (:use carica.core
        clojure.data))

(def db-url (config :db :url))

(def conn (d/connect db-url))
(defn db [] (d/db conn))

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

;;(before #(recommendations-comleted-by-user (db) "user1"))
;;(before #(recommendations (db) "spec1"))
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

;;         [?mid :media/id ?media_id]
         ]
       db [:user/name user])
  (every-first)
  set))
;;(before #(recommendations-for-user (db) "user1"))

(defn recommendations [db user]
  (let [recs (recommendations-for-user db user)
        completed (recommendations-comleted-by-user db user)]
    (first (diff recs completed))))

;; (recommendations (db) "user1")
;; (before #(recommendations (db) "user1"))
