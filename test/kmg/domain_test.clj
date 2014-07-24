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

(deftest test-available-specialization-ids
  (let [db (db)]
    (is (= (spec-ids db #(available-specialization-ids db "user2")))
        #{"spec2" "spec3"})))

(deftest test-user-goals-history
  (let [before-db (db)]
    (is (= (spec-ids before-db #(user-goals-history before-db "user2"))
           #{"spec1"}))
    (change-goal-command "user2" "spec2")
    (let [after-db (db)]
      (is (= (spec-ids after-db #(user-goals-history after-db "user2"))
           #{"spec1" "spec2"})))
    ))

(deftest test-completed-specialization-ids
  (let [before-db (db)]
    (is (= (spec-ids before-db #(completed-specialization-ids before-db "user2"))
           #{"spec1"}))
    (change-goal-command "user2" "spec2")
    (mark-as-completed-command "user2" "spec2_book3")
    (let [after-db (db)](is (= (spec-ids after-db #(user-goals-history after-db "user2"))
           #{"spec1" "spec2"})))))

(deftest test-children-specialization-ids
  (let [db (db)]
    (is (= (spec-ids db #(children-specialization-ids db (get-spec-id db "spec1")))
         #{"spec2" "spec3"}))
    (is (= (spec-ids db #(children-specialization-ids db (get-spec-id db "spec3")))
         #{"spec4"}))))

(deftest test-is-specialization-completed
  (let [db (db)]
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec1"))
           true))
    (is (= (is-specialization-completed? db "user1" (get-spec-id db "spec2"))
           false))))

(deftest test-is-specialization-available
  (let [db (db)]
    (is (= (is-specialization-available? db "user1" "spec2")
           true))))

(deftest test-reommendations
  (let [db (db)]
    (is (= (rec-ids db #(recommendation-ids db "user2" (get-spec-id db "spec1"))))
        #{"spec1_book3" "spec1_book4" "spec1_book5"})))

(deftest test-reommendations-throws-exception-if-specializaion-is-not-one-from-goal-history
  (let [db (db)]
    (is (thrown? IllegalArgumentException (recommendation-ids db "user2" (get-spec-id db "spec2"))))))

(deftest test-recommendations-completed
  (let [db (db)]
    (is (= (rec-ids db #(recommendations-completed-by-user db "user2"))
           #{"spec1_book2" "spec1_book1"}))))

(deftest test-recommendatons-for-user
  (let [db (db)]
    (is (= (rec-ids db #(recommendations-for-user db "user1" (user-current-goal db "user1")))
           #{"spec1_book4" "spec1_book5" "spec1_book1" "spec1_book3" "spec1_book2"}))))

(deftest test-user-current-goal
  (let [db (db)]
    (is (= (spec-ids db #(vector (user-current-goal db "user2")))
           #{"spec1"}))))

(deftest test-media-backgrounds
  (let [db (db)]
    (is (= (media-backgrounds db (media-dbid-by-id db "book2"))
           #{(media-dbid-by-id db "book1")}))))

(deftest test-is-media-complete
  (let [db (db)]
    (is (= (is-media-complete? db (media-dbid-by-id db "book1") "user2")
           true))
    (is (= (is-media-complete? db (media-dbid-by-id db "book2") "user1")
           false))))

(deftest test-background-data
  (let [db (db)
        background-data (background-data db (media-dbid-by-id db "book2") "user1")]
    (is (= (:media/id (:media background-data))
           "book2"))
    (is (= (:completed background-data)
           false))))

(deftest test-recommendation-data
  (let [db (db)
        recommendation-data (recommendation-data db (recommendation-dbid db "spec1_book2") "user1")]
    (is (= (:media/id (:media recommendation-data))
           "book2"))
    (is (= (:recommendation/id (:recommendation recommendation-data))
           "spec1_book2"))
    (is (= (:media/id (:media (first (:backgrounds recommendation-data))))
        "book1"))))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Commands tests

(deftest test-change-goal
  (let [db-before (db)]
    (is (= (spec-ids db-before #(vector (user-current-goal db-before "user2")))
           #{"spec1"}))
    (change-goal-command "user2" "spec2")
    (let [db-after (db)]
          (is (= (spec-ids db-after #(vector (user-current-goal db-after "user2")))
           #{"spec2"})))))

(deftest test-change-goal-to-unavailable-spcialization
  (let [db-before (db)]
    (is (= (spec-ids db-before #(vector (user-current-goal db-before "user2")))
           #{"spec1"}))
    (is (thrown? IllegalArgumentException (change-goal-command "user2" "spec4")))
    (let [db-after (db)]
          (is (= (spec-ids db-after #(vector (user-current-goal db-after "user2")))
           #{"spec1"})))))
