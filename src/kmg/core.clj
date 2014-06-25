(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [kmg.domain :as model])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn recommendations-stub [user]
  [{:media/id "book1" :media/title (str "book1 for " user)}
    {:media/id "book2" :media/title (str "book2 for " user)}])

(defn recommendations [user]
  (response (model/recommendations user)))

(defn user-list []
  (response (model/users)))

(defroutes recommendation-routes
  (GET "/list/:user" [user] (recommendations user)))

(defroutes user-routes
  (GET "/list" [] (user-list)))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))

  (context "/recommendation" [] recommendation-routes)

  (context "/user" [] user-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))
