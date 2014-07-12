(ns kmg.domain-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all]
   [taoensso.timbre.profiling :as p])
  (:use
   kmg.helpers
   kmg.domain))

(use-fixtures :each before)

(defn spec-ids [db f]
  (entity-values-by-ids db :specialization/id (f)))

(deftest test-available-specializations
  (let [db (db)]
    (is (= (spec-ids db #(available-specializations "user2")))
        #{"spec1" "spec2" "spec3"})))

(deftest test-user-goals-history
  (let [db (db)]
    (is (= (spec-ids db #(user-goals-history db "user2"))
           #{"spec1"})))
  )

(deftest test-completed-specializations
  (let [db (db)]
    (is (= (spec-ids db #(completed-specializations db "user2"))
           #{"spec1"}))))

(deftest test-children-specializations
  (is (= (project-value :specialization/id (children-specializations "spec1"))
         ["spec2" "spec3"]))
  (is (= (project-value :specialization/id (children-specializations "spec3"))
         ["spec4"])))

(deftest test-is-specialization-completed
  (let [db (db)]
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec1"))
           true))
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec2"))
           false))))

(defn rec-ids [db f]
  (entity-values-by-ids db :recommendation/id (f)))

(deftest test-recommendations
  (let [db (db)]
    (is (= (rec-ids db #(recommendation-ids db "user2")))
        #{"spec1_book3" "spec1_book4" "spec1_book5"})))

(deftest test-recommendations-completed
  (let [db (db)]
    (is (= (rec-ids db #(recommendations-completed-by-user db "user2"))
           #{"spec1_book2" "spec1_book1"}))))
