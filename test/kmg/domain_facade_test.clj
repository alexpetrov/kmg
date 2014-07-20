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
  (is (not= (:recommendations (whole-user-data "user2"))
         nil))
  (is (not= (:recommendations-completed (whole-user-data "user2"))
         nil))
  (is (not= (:specializations-completed (whole-user-data "user2"))
         nil))
  (is (not= (:specializations-available (whole-user-data "user2"))
         nil))
  #_(is (= (whole-user-data "user2")
         {:recommendations [{:recommendation {:recommendation/specialization {:db/id 17592186045424}, :recommendation/media {:db/id 17592186045435}, :recommendation/id "spec1_book3", :recommendation/priority 800, :recommendation/necessary false, :recommendation/description "spec1 book3", :db/id 17592186045447}, :media {:media/id "book3", :media/type :media.type/book, :media/title "book3_title", :media/experience 2, :media/essential true, :db/id 17592186045435}} {:recommendation {:recommendation/specialization {:db/id 17592186045424}, :recommendation/media {:db/id 17592186045436}, :recommendation/id "spec1_book4", :recommendation/priority 700, :recommendation/necessary false, :recommendation/description "spec1 book4", :db/id 17592186045448}, :media {:media/id "book4", :media/type :media.type/book, :media/title "book4_title", :media/experience 3, :media/essential false, :db/id 17592186045436}} {:recommendation {:recommendation/specialization {:db/id 17592186045424}, :recommendation/media {:db/id 17592186045437}, :recommendation/id "spec1_book5", :recommendation/priority 600, :recommendation/necessary false, :recommendation/description "spec1 book5", :db/id 17592186045449}, :media {:media/id "book5", :media/type :media.type/book, :media/title "book5_title", :media/experience 0, :media/essential false, :db/id 17592186045437}}],
          :recommendations-completed [{:recommendation {:recommendation/specialization {:db/id 17592186045424}, :recommendation/media {:db/id 17592186045434}, :recommendation/id "spec1_book2", :recommendation/priority 900, :recommendation/necessary false, :recommendation/description "spec1 book2", :db/id 17592186045446}, :media {:media/id "book2", :media/type :media.type/book, :media/title "book2_title", :media/experience 1, :media/essential true, :db/id 17592186045434}} {:recommendation {:recommendation/specialization {:db/id 17592186045424}, :recommendation/media {:db/id 17592186045433}, :recommendation/id "spec1_book1", :recommendation/priority 1000, :recommendation/necessary true, :recommendation/description "spec1 book1", :db/id 17592186045445}, :media {:media/id "book1", :media/type :media.type/book, :media/title "book1_title", :media/experience 0, :media/essential true, :db/id 17592186045433}}],
          :specializations-available [{:specialization/id "spec2", :specialization/title "spec2_title", :specialization/annotation "spec2_annotation", :db/id 17592186045425} {:specialization/id "spec3", :specialization/title "spec3_title", :specialization/annotation "spec3_annotation", :db/id 17592186045426}],
          :specializations-completed [{:specialization/id "spec1", :specialization/title "spec1_title", :specialization/annotation "spec1_annotation", :db/id 17592186045424}]})))
