(ns kmg.domain
  (:require
    [datomic.api :as d]
    )
  (:use carica.core))

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
