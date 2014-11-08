(ns kmg.helpers
  (:require
   [datomic.api :as d]
   [clojure.test :refer :all]
   [kmg.datomic-helpers :as dh])
  (:use
   clojure.test))

(defn before [f]
  (dh/create-db-and-import-sample-data-for-test)
  (f))

(defn db []
  (d/db (d/connect (dh/db-url))))

(defn project-value [field seq]
  (map #(field %) seq))

(defn entity-values-by-ids [db field seq]
  (set (map #(field (d/entity db %)) seq)))
