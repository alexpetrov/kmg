(ns kmg.domain-facade-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all])
  (:use
   kmg.helpers
   kmg.domain-facade))

(use-fixtures :each before)

(deftest test-current-speicalization
  (is (= (:specialization/id (current-specialization "user1"))
         "spec1")))

(deftest test-children-specializations
  (is (= (project-value :specialization/id
                        (children-specializations "spec1"))
         ["spec2" "spec3"]))
  (is (= (project-value :specialization/id (children-specializations "spec3"))
         ["spec4"])))

(deftest test-completed-specializations
  (is (= (project-value :specialization/id (specializations-completed "user2"))
         ["spec1"])))

(deftest test-available-specializations
  (is (= (project-value :specialization/id (specializations-available "user2"))
         ["spec2" "spec3"])))

(deftest test-whole-user-data
  (let [user-data (whole-user-data "user2")]
    (is (not= (:current-specialization user-data)
              nil))
    (is (not= (:recommendations user-data)
              nil))
    (is (not= (:recommendations-completed user-data)
              nil))
    (is (not= (:specializations-completed user-data)
              nil))
    (is (not= (:specializations-available user-data)
              nil))))
