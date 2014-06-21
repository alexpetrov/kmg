(ns kmg.schema-test
  (:require
   [datomic.api :as d]
   [datomic-schema-grapher.dot :as dot]
   [datomic-schema-grapher.core :refer (graph-datomic)]
   [clojure.test :refer :all])
  (:use
   kmg.schema
   clojure.test))


(def uri "datomic:mem://test")
(defn fresh-conn []
  (d/delete-database uri)
  (d/create-database uri)
  (d/connect uri))

(defn show-schema []
  (let [conn (fresh-conn)
        db (d/db conn)
        sample-data (read-string (slurp "test/kmg/sample_data.edn"))]
    (d/transact conn kmg-schema)
    @(d/transact conn (:specializations sample-data))
    @(d/transact conn (:media sample-data))
    @(d/transact conn (:recommendations sample-data))
    @(d/transact conn (:users sample-data))
    @(d/transact conn (:feedback sample-data))

    #_(graph-datomic uri)
    (graph-datomic uri :save-as "kmg-schema.dot")
    ))

;; (show-schema)

#_(def sample-data (read-string (slurp "test/kmg/sample_data.edn")))

(defn before [f]
  (let [conn (fresh-conn)
        sample-data (read-string (slurp "test/kmg/sample_data.edn"))]
    (d/transact conn kmg-schema)
    @(d/transact conn (:specializations sample-data))
    @(d/transact conn (:media sample-data))
    @(d/transact conn (:recommendations sample-data))
    @(d/transact conn (:users sample-data))
    @(d/transact conn (:feedback sample-data))
)
  (f))

(use-fixtures :each before)

(defn db []
  (d/db (d/connect uri)))

(defn attr-spec [field-name]
  (first (d/q '[:find ?type ?cardinality
                :in $ ?field-name
                :where
                [?id :db/ident ?field-name]
                [?id :db/valueType ?t]
                [?t :db/ident ?type]
                [?id :db/cardinality ?c]
                [?c :db/ident ?cardinality]]
              (db) field-name)))

(defn schema-enum-value [enum]
  (ffirst (d/q '[:find ?enum
                 :in $ ?enum
                 :where
                 [?id :db/ident ?enum]]
               (db) enum)))

(deftest test-kmg-schema-for-media-type
  (is (= (attr-spec :media/id)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :media/type)
         [:db.type/ref :db.cardinality/one]))

  (is (= (attr-spec :media/title)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :media/url)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :media/author)
         [:db.type/ref :db.cardinality/many]))

  (is (= (attr-spec :media/annotation)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :media/experience)
         [:db.type/long :db.cardinality/one]))

  (is (= (attr-spec :media/essential)
         [:db.type/boolean :db.cardinality/one]))

  (is (= (attr-spec :media/locale)
         [:db.type/keyword :db.cardinality/one]))

  (is (= (attr-spec :media/stats)
         [:db.type/long :db.cardinality/one]))
  )

(deftest test-kmg-schema-for-media-relationship
  (is (= (attr-spec :media.relationship/media_from)
         [:db.type/ref :db.cardinality/one]))

  (is (= (attr-spec :media.relationship/media_to)
         [:db.type/ref :db.cardinality/one]))

  (is (= (attr-spec :media.relationship/type)
         [:db.type/keyword :db.cardinality/one]))

  (is (= (attr-spec :media.relationship/description)
         [:db.type/string :db.cardinality/one])))

(deftest test-kmg-schema-for-author-type
  (is (= (attr-spec :author/id)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :author/name)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :author/user)
         [:db.type/ref :db.cardinality/one])))

(deftest test-media-type-enum-values
  (is (= (schema-enum-value :media.type/book)
         :media.type/book))
  (is (= (schema-enum-value :media.type/article)
         :media.type/article))
  (is (= (schema-enum-value :media.type/video)
         :media.type/video))
  (is (= (schema-enum-value :media.type/podcast)
         :media.type/podcast))
  (is (= (schema-enum-value :media.type/blog)
         :media.type/blog))
  (is (= (schema-enum-value :media.type/course)
         :media.type/course)))

(deftest test-kmg-schema-for-specialization
  (is (= (attr-spec :specialization/id)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :specialization/title)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :specialization/annotation)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :specialization/prerequisite)
         [:db.type/ref :db.cardinality/many])))

(deftest test-kmg-schema-for-recommendation
  (is (= (attr-spec :recommendation/specialization)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :recommendation/media)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :recommendation/id)
         [:db.type/string :db.cardinality/one]))

  (is (= (attr-spec :recommendation/priority)
         [:db.type/long :db.cardinality/one]))
  (is (= (attr-spec :recommendation/necessary)
         [:db.type/boolean :db.cardinality/one]))
  (is (= (attr-spec :recommendation/description)
         [:db.type/string :db.cardinality/one])))

(deftest test-kmg-schema-for-user
  (is (= (attr-spec :user/name)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :user/start-career-year)
         [:db.type/long :db.cardinality/one]))
  (is (= (attr-spec :user/goal)
         [:db.type/ref :db.cardinality/one])))

(deftest test-kmg-schema-for-feedback
  (is (= (attr-spec :feedback/user)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :feedback/recommendation)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :feedback/complete)
         [:db.type/boolean :db.cardinality/one]))
  (is (= (attr-spec :feedback.comment/text)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :feedback.comment/show)
         [:db.type/boolean :db.cardinality/one]))
  (is (= (attr-spec :feedback/relevant)
         [:db.type/boolean :db.cardinality/one]))
  (is (= (attr-spec :feedback/complete)
         [:db.type/boolean :db.cardinality/one])))

(defn every-first [v]
  (for [elem v] (first elem)))


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

;; (every-first [[17592186045432 1000] [17592186045433 900] [17592186045434 800]])
;; TODO: Here must be more elegant implementation than diff, or more speedy
(use 'clojure.data)
(defn recommendations [db user]
  (let [recs (recommendations-for-user db user)
        completed (recommendations-comleted-by-user db user)]
    (first (diff recs completed))))

;; (before #(recommendations (db) "user1"))

(deftest test-kmg-sample-data
  (is (= (d/q '[:find ?name
                :where
                [?id :user/name ?name]]
               (db))
         #{["user1"] ["user2"]}))

  (is (= (d/q '[:find ?text
                :where
                [?id :feedback.comment/text ?text]]
               (db))
         #{["spec1_book1_is_awesome"] ["spec1_book1_is_irrelevant"]}))

  (is (= (d/q '[:find ?title
                :where
                [?id :media/title ?title]]
               (db))
         #{["book2_title"] ["book3_title"] ["book1_title"] ["book4_title"]}))

  #_(is (= (recommendations (db) "user1")
         #{["book1"] ["book2"] ["book3"]}))
  (print (d/touch (d/entity (db) [:specialization/id "spec1"])))
 (is (= (:specialization/title (d/touch (d/entity (db) [:specialization/id "spec1"]))) "spec1_title"))

  )
