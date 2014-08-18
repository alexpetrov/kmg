(ns kmg.schema-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all])
  (:use
   kmg.helpers))

(use-fixtures :each before)

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
         [:db.type/long :db.cardinality/one])))

(deftest test-kmg-schema-for-media-relationship
  (is (= (attr-spec :media.relationship/from)
         [:db.type/ref :db.cardinality/one]))

  (is (= (attr-spec :media.relationship/to)
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
         [:db.type/string :db.cardinality/one])))

(deftest test-kmg-schema-for-specialization-relationship
  (is (= (attr-spec :specialization.relationship/from)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :specialization.relationship/to)
         [:db.type/ref :db.cardinality/one]))
  (is (= (attr-spec :specialization.relationship/description)
         [:db.type/string :db.cardinality/one]))
  )

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
  (is (= (attr-spec :user/locale)
         [:db.type/keyword :db.cardinality/one]))
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
  (is (= (attr-spec :feedback/comprehensible)
         [:db.type/boolean :db.cardinality/one]))
  (is (= (attr-spec :feedback/complete)
         [:db.type/boolean :db.cardinality/one])))

(deftest test-kmg-schema-for-domain
  (is (= (attr-spec :domain/id)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :domain/title)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :domain/description)
         [:db.type/string :db.cardinality/one]))
  (is (= (attr-spec :domain/default-locale)
         [:db.type/keyword :db.cardinality/one])))
