(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [kmg.domain-facade :as model]
            [taoensso.timbre :as log]
            [taoensso.timbre.profiling :as p])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn recommendations [user]
  (log/info "recommendations for user: " user)
  (response (p/profile :info :recommendations
                       (model/recommendations user))))

(defn recommendations-completed [user]
  (log/info "recommendations-completed for user: " user)
  (response (p/profile :info :recommendations-completed
                       (model/recommendations-completed user))))

(defn user-list []
  (response (model/users)))

(defn mark-as-completed [user recommendation]
  (p/profile :info :mark-as-completed
             (do
               (log/info "user: " user "recommendation:" recommendation)
               (model/mark-as-completed user recommendation)))
  (response nil))


(defroutes recommendation-routes
  (GET "/list/:user" [user] (recommendations user))
  (GET "/completed/:user" [user] (recommendations-completed user))
  (POST "/mark-as-completed/:user/:recommendation" [user recommendation] (mark-as-completed user recommendation)))

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
