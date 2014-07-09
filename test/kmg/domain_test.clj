(ns kmg.domain-test
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all])
  (:use
   kmg.helpers
   kmg.domain))

(use-fixtures :each before)

(deftest test-children-specializations
  (is (= (project :specialization/id (children-specializations "spec1"))
         ["spec2" "spec3"]))
  (is (= (project :specialization/id (children-specializations "spec3"))
         ["spec4"])))
