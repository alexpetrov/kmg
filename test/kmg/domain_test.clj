(ns kmg.domain-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all])
  (:use
   kmg.helpers
   kmg.domain))

(use-fixtures :each before)

(deftest test-children-specializations
  (is (= (project-value :specialization/id (children-specializations "spec1"))
         ["spec2" "spec3"]))
  (is (= (project-value :specialization/id (children-specializations "spec3"))
         ["spec4"])))

(deftest test-recommendations
  (let [db (db)]
    (is (= (entity-values-by-ids db :recommendation/id (recommendation-ids db "user2"))
         ["spec1_book3" "spec1_book4" "spec1_book5"]))))
