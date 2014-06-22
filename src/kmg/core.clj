(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))



(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  #_(context "/article" [] article-routes)
  #_(context "/user" [] user-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  #_(route/files "/" {:root (config :external-resources)})
  (route/not-found "Not found!"))


(def app
  (-> compojure-handler
      site
      wrap-edn-params))
