(ns kmg.core
  (:require [compojure.route :as route]
            [clojure.java.io :as io]
            [kmg.domain-facade :as model]
            [kmg.view :as view]
            [taoensso.timbre :as log]
            [environ.core :refer [env]])
  (:use compojure.core
        compojure.handler
        org.httpkit.server)
  (:gen-class))

(def current-user "user2")

(log/set-config! [:appenders :spit :enabled?] true)
(log/set-config! [:shared-appender-config :spit-filename] (env :log-file-path))

(defn redirect
  ([]
   (redirect ""))
  ([path]
   {:status 302
    :headers {"Location" (str "/" path)}
    :body ""}))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "text/html;charset=utf-8"}
   :body data})

(defn change-specialization [specialization]
  (log/info "user: " current-user "change specialization to specialization: " specialization)
  (model/change-specialization current-user specialization)
  (redirect))

(defn mark-as-completed
  ([recommendation]
   (log/info "user: " current-user "mark-as-completed recommendation:" recommendation)
   (model/mark-as-completed current-user recommendation)
   (redirect)))

(defn index []
  (->> (model/whole-user-data current-user)
       view/base
       view/render
       response))

(defroutes compojure-handler
  (GET "/" [] (index))
  (GET "/complete/:recommendation" [recommendation] (mark-as-completed recommendation))
  (GET "/choose/:specialization" [specialization] (change-specialization specialization))
  (route/resources "/")
  (route/not-found "Not found!"))

(def app
  (-> compojure-handler
      site))

(defn start []
  (run-server app {:port 3000 :join? false}))

;; (start)
(defn -main [& args]
  (if (model/db-connection-healthy?)
    (start)
    (System/exit 1)))
