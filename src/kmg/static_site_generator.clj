(ns kmg.static-site-generator
  (:require
   [kmg.datomic-helpers :as dh]
   [kmg.domain-facade :as model]
   [kmg.view :as view]))

(defn prototype []
  (dh/create-db-and-import-knowledge-base-4-it)
  (->> (model/whole-user-data "user1")
       view/base
       view/render
       println))
