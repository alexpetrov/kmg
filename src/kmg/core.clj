(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn user-list []
  (response ["user1" "user2"]))

(defroutes user-routes
  (GET "/list" [] (user-list)))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  (context "/user" [] user-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  #_(route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))


(def app
  (-> compojure-handler
      site
      wrap-edn-params))
