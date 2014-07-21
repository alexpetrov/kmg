(ns kmg.domain-facade-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all]
   [taoensso.timbre.profiling :as p])
  (:use
   kmg.helpers
   kmg.domain
   kmg.domain-facade))

(use-fixtures :each before)

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
    (is (not= (:recommendations user-data)
              nil))
    (is (not= (:recommendations-completed user-data)
              nil))
    (is (not= (:specializations-completed user-data)
              nil))
    (is (not= (:specializations-available user-data)
              nil))))
