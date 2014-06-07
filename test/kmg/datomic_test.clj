(ns kmg.datomic-test
  (:require
       [datomic.api :as d]
       [datomic-schema-grapher.core :refer (graph-datomic)])
  (:use
    kmg.datomic
    clojure.test))


(def uri "datomic:mem://test")
(defn fresh-conn []
  (d/delete-database uri)
  (d/create-database uri)
  (d/connect uri))

(defn show-schema []
  (let [conn (time (fresh-conn))]
    (time (d/transact conn schema))
    (graph-datomic uri)
    #_(graph-datomic uri :save-as "the-schema.dot")

))

;; (show-schema)

(defn attr-spec [db field-name]
  (time (first (d/q '[:find ?type ?cardinality
               :in $ ?field-name
               :where
               [?id :db/ident ?field-name]
               [?id :db/valueType ?t]
               [?t :db/ident ?type]
               [?id :db/cardinality ?c]
               [?c :db/ident ?cardinality]]
               db field-name))))

(deftest test-schema
  (let [conn (time (fresh-conn))]
    (time (d/transact conn schema))
    (let [db (time (d/db conn))]

      (is (= (attr-spec db :media/id)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :media/type)
             [:db.type/ref :db.cardinality/one]))

      (is (= (attr-spec db :media/title)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :media/author)
             [:db.type/ref :db.cardinality/many]))
      (is (= (attr-spec db :media/annotation)
             [:db.type/string :db.cardinality/one]))
      (is (= (attr-spec db :media/prerequisite)
             [:db.type/ref :db.cardinality/many]))
      (is (= (attr-spec db :media/experience)
             [:db.type/long :db.cardinality/one]))
      (is (= (attr-spec db :media/essential)
             [:db.type/boolean :db.cardinality/one]))
      (is (= (attr-spec db :media/locale)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :media/localization)
             [:db.type/ref :db.cardinality/one]))
      (is (= (attr-spec db :media/stats)
             [:db.type/long :db.cardinality/one]))


    )))
