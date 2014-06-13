(ns kmg.datomic-test
  (:require
       [datomic.api :as d]
       [datomic-schema-grapher.dot :as dot]
       [datomic-schema-grapher.core :refer (graph-datomic)]
       [clojure.test :refer :all])
  (:use
    kmg.datomic
    clojure.test))


(def uri "datomic:mem://test")
(defn fresh-conn []
  (d/delete-database uri)
  (d/create-database uri)
  (d/connect uri))

(defn show-schema []
  (let [conn (fresh-conn)
        db (d/db conn)]
    (d/transact conn kmg-schema)
    (graph-datomic uri)
    #_(graph-datomic uri :save-as "the-schema.dot")
))

;; (show-schema)
(comment

(defn before [] (d/create-database uri))
(defn after [] (d/delete-database uri))
(use-fixtures :each (before after))

)

(defn attr-spec [db field-name]
  (first (d/q '[:find ?type ?cardinality
               :in $ ?field-name
               :where
               [?id :db/ident ?field-name]
               [?id :db/valueType ?t]
               [?t :db/ident ?type]
               [?id :db/cardinality ?c]
               [?c :db/ident ?cardinality]]
               db field-name)))

(defn schema-enum-value [db enum]
  (ffirst (d/q '[:find ?enum
                 :in $ ?enum
                 :where
                 [?id :db/ident ?enum]]
       db enum)))

(deftest test-kmg-schema-for-media-type
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]

      (is (= (attr-spec db :media/id)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :media/type)
             [:db.type/ref :db.cardinality/one]))

      (is (= (attr-spec db :media/title)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :media/url)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :media/author)
             [:db.type/ref :db.cardinality/many]))

      (is (= (attr-spec db :media/annotation)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :media/experience)
             [:db.type/long :db.cardinality/one]))

      (is (= (attr-spec db :media/essential)
             [:db.type/boolean :db.cardinality/one]))

      (is (= (attr-spec db :media/locale)
             [:db.type/keyword :db.cardinality/one]))

      (is (= (attr-spec db :media/stats)
             [:db.type/long :db.cardinality/one])))))

(deftest test-kmg-schema-for-media-relationship
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :media.relationship/media_from)
             [:db.type/ref :db.cardinality/one]))

      (is (= (attr-spec db :media.relationship/media_to)
             [:db.type/ref :db.cardinality/one]))

      (is (= (attr-spec db :media.relationship/type)
             [:db.type/keyword :db.cardinality/one]))

      (is (= (attr-spec db :media.relationship/description)
             [:db.type/string :db.cardinality/one])))))

(deftest test-kmg-schema-for-author-type
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :author/id)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :author/name)
             [:db.type/string :db.cardinality/one]))

      (is (= (attr-spec db :author/user)
             [:db.type/ref :db.cardinality/one])))))


(deftest test-media-type-enum-values
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (schema-enum-value db :media.type/book)
             :media.type/book))
      (is (= (schema-enum-value db :media.type/article)
             :media.type/article))
      (is (= (schema-enum-value db :media.type/video)
             :media.type/video))
      (is (= (schema-enum-value db :media.type/podcast)
             :media.type/podcast))
      (is (= (schema-enum-value db :media.type/blog)
             :media.type/blog))
      (is (= (schema-enum-value db :media.type/course)
             :media.type/course)))))

(deftest test-kmg-schema-for-specialization
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :specialization/id)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :specialization/title)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :specialization/annotation)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :specialization/prerequisite)
             [:db.type/ref :db.cardinality/many])))))

(deftest test-kmg-schema-for-recommendation
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :recommendation/specialization)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :recommendation/media)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :recommendation/priority)
             [:db.type/long :db.cardinality/one]))
      (is (= (attr-spec db :recommendation/necessary)
             [:db.type/boolean :db.cardinality/one]))
      (is (= (attr-spec db :recommendation/description)
             [:db.type/string :db.cardinality/one])))))

(deftest test-kmg-schema-for-user
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :user/name)
          [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :user/start-career-year)
          [:db.type/long :db.cardinality/one]))
      (is (= (attr-spec db :user/goal)
          [:db.type/ref :db.cardinality/one])))))

(deftest test-kmg-schema-for-feedback
  (let [conn (fresh-conn)]
    (d/transact conn kmg-schema)
    (let [db (d/db conn)]
      (is (= (attr-spec db :feedback/user)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :feedback.recommendation/id)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :feedback.recommendation/tx)
             [:db.type/long :db.cardinality/one]))
      (is (= (attr-spec db :feedback/complete)
             [:db.type/boolean :db.cardinality/one]))
      (is (= (attr-spec db :feedback.comment/text)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :feedback.comment/show)
             [:db.type/boolean :db.cardinality/one]))
      (is (= (attr-spec db :feedback/complete)
             [:db.type/boolean :db.cardinality/one])))))
