(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [kmg.domain-facade :as model]
            [taoensso.timbre :as log])
  (:use compojure.core
        compojure.handler
        ring.middleware.edn
        carica.core))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/edn"}
   :body (pr-str data)})

(defn recommendations
  ([user]
     (log/info "recommendations for user: " user)
     (response (model/recommendations user)))
  ([user spec]
     (log/info "recommendations for user: " user "; specialization: " spec)
     (response (model/recommendations user spec))))

(defn recommendations-completed [user]
  (log/info "recommendations-completed for user: " user)
  (response (model/recommendations-completed user)))

(defn specializations-completed [user]
  (log/info "specializations-completed for user: " user)
  (response (model/specializations-completed user)))

(defn specializations-available [user]
  (log/info "specializations-available for user: " user)
  (response (model/specializations-available user)))

(defn whole-user-data [user]
  (log/info "whole-user-data for user: " user)
  (response (model/whole-user-data user)))

(defn user-list []
  (response (model/users)))

(defn mark-as-completed [user recommendation]
  (log/info "user: " user "mark-as-completed recommendation:" recommendation)
  (model/mark-as-completed user recommendation)
  (response nil))

(defn change-goal [user specialization]
  (log/info "user: " user "change goal to specialization: " specialization)
  (model/change-goal user specialization)
  (response nil))

(defroutes recommendation-routes
  (GET "/list/:user" [user] (recommendations user))
  (GET "/list/:user/:spec" [user spec] (recommendations user spec))
  (GET "/completed/:user" [user] (recommendations-completed user))
  (POST "/mark-as-completed/:user/:recommendation" [user recommendation]
        (mark-as-completed user recommendation)))

(defroutes specialization-routes
  (GET "/completed/:user" [user] (specializations-completed user))
  (GET "/available/:user" [user] (specializations-available user))
  (POST "/change-goal/:user/:specialization" [user specialization]
       (change-goal user specialization)))


(defroutes user-routes
  (GET "/list" [] (user-list)))

(defroutes compojure-handler
  (GET "/" [] (slurp (io/resource "public/html/index.html")))
  (GET "/data/:user" [user] (whole-user-data user))
  (context "/recommendation" [] recommendation-routes)
  (context "/specialization" [] specialization-routes)
  (context "/user" [] user-routes)
  (GET "/req" request (str request))
  (route/resources "/")
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site
      wrap-edn-params))
