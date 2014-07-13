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

(defn rec-ids [db f]
  (entity-values-by-ids db :recommendation/id (f)))

(deftest test-available-specializations
  (let [db (db)]
    (is (= (spec-ids db #(available-specialization-ids db "user2")))
        #{"spec1" "spec2" "spec3"})))

(deftest test-user-goals-history
  (let [db (db)]
    (is (= (spec-ids db #(user-goals-history db "user2"))
           #{"spec1"}))))

(deftest test-user-current-goal
  (let [db (db)]
    (is (= (spec-ids db #(vector (user-current-goal db "user2")))
           #{"spec1"}))))

(deftest test-completed-specializations
  (let [db (db)]
    (is (= (spec-ids db #(completed-specializations db "user2"))
           #{"spec1"}))))

(deftest test-children-specialization-ids
  (let [db (db)]
    (is (= (spec-ids db #(children-specialization-ids db "spec1"))
         #{"spec2" "spec3"}))
    (is (= (spec-ids db #(children-specialization-ids db "spec3"))
         #{"spec4"}))))

(deftest test-is-specialization-completed
  (let [db (db)]
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec1"))
           true))
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec2"))
           false))))

(deftest test-recommendations-default-goal
  (let [db (db)]
    (is (= (rec-ids db #(recommendation-ids db "user2")))
        #{"spec1_book3" "spec1_book4" "spec1_book5"})))

;; TODO
(deftest test-reommendations
  (let [db (db)]
    (is (= (rec-ids db #(recommendation-ids db "user2" (get-spec-id db "spec1"))))
        #{"spec1_book3" "spec1_book4" "spec1_book5"})))

(deftest test-recommendations-completed
  (let [db (db)]
    (is (= (rec-ids db #(recommendations-completed-by-user db "user2"))
           #{"spec1_book2" "spec1_book1"}))))

(deftest test-recommendatons-for-user
  (let [db (db)]
    (is (= (rec-ids db #(recommendations-for-user db "user1" (user-current-goal db "user1")))
           #{"spec2_book3" "spec5_book4" "spec2_book4"}))))
