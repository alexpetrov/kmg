(ns kmg.test-schema
  (:require
       [datomic.api :as d])
  (:use
    kmg.datomic
    clojure.test))

(def uri "datomic:mem://test")
(defn fresh-conn []
  (d/delete-database uri)
  (d/create-database uri)
  (d/connect uri))

(defn attr-type-by-name [db field-name]
  (time (ffirst (d/q '[:find ?type
               :in $ ?field-name
               :where
               [?id :db/ident ?field-name]
               [?id :db/valueType ?t]
               [?t :db/ident ?type]]
               db field-name))))

(deftest test-schema-creation
  (let [conn (time (fresh-conn))]
    (time (d/transact conn schema))
    (is (= (attr-type-by-name (d/db conn) :media/name)
         :db.type/string))))
