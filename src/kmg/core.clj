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
[{:recommendation {:recommendation/specialization {:db/id 17592186045424},
                   :recommendation/media {:db/id 17592186045429},
                   :recommendation/id "spec1_book3",
                   :recommendation/priority 800,
                   :recommendation/necessary false,
                   :recommendation/description (str "spec1 book3" user),
                   :db/id 17592186045434},
   :media {:media/id "book3",
           :media/type :media.type/book,
           :media/title "book3_title",
           :media/experience 2,
           :media/essential true,
           :db/id 17592186045429}}
 {:recommendation {:recommendation/specialization {:db/id 17592186045424},
                   :recommendation/media {:db/id 17592186045428},
                   :recommendation/id "spec1_book2",
                   :recommendation/priority 900,
                   :recommendation/necessary false,
                   :recommendation/description (str "spec1 book2" user),
                   :db/id 17592186045433},
  :media {:media/id "book2",
          :media/type :media.type/book,
          :media/title "book2_title",
          :media/experience 1,
          :media/essential true,
          :db/id 17592186045428}}]
  )

(defn recommendations [user]
  (response (model/recommendations user))
  #_(response (recommendations-stub user)))

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
