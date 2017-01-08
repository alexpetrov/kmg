(ns kmg.core
  (:import (java.io ByteArrayOutputStream))
  (:require [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [kmg.domain-facade :as model]
            [taoensso.timbre :as log]
            [cognitect.transit :as transit]
            [environ.core :refer [env]])
  (:use compojure.core
        compojure.handler
        org.httpkit.server)
  (:gen-class))

(def current-user "user2")

(log/set-config! [:appenders :spit :enabled?] true)
(log/set-config! [:shared-appender-config :spit-filename] (env :log-file-path))

(defn write [x]
  (let [baos (ByteArrayOutputStream.)
        w    (transit/writer baos :json)
        _    (transit/write w x)
        ret  (.toString baos)]
    (.reset baos)
    ret))

(defn response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/transit+json; charset=utf-8"}
   :body (write data)})

(defn html-response [data & [status]]
  {:status (or status 200)
   :body data})

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

(defn change-goal [user specialization]
  (log/info "user: " user "change goal to specialization: " specialization)
  (model/change-goal user specialization)
  (response nil))

;; View layer
;; FIXME: Extract View layer to separate namespace


(defn render [t]
  (reduce str t))

(def render-to-response
  (comp html-response render))

(def type->icon {:media.type/book "book"
                 :media.type/article "file"
                 :media.type/video "film"
                 :media.type/podcast "headphones"
                 :media.type/course "book"
                 :media.type/blog "file"})

(defn media-icon-span [media]
  (str "<span class=\"glyphicon glyphicon-" (type->icon (:media/type media)) "\"></span> "))

(defn media-title [media]
  (str (media-icon-span media) (:media/title media) " " ))

(defn authors-string [authors]
  (->> (map :author authors)
       (map :author/name)
       (clojure.string/join ", ")))

(defn media-url [media]
  (if (:media/url media)
    (str "<a href='" (:media/url media) "'>" (:media/url media) "</a> <br/> ")
    ""))

(defn not-all-backgrounds-completed? [backgrounds]
  (some false? (for [b backgrounds] (:completed b))))

(def tmpl "public/html/kmg.html")

(html/defsnippet recommendation-completed tmpl [:div.recommendation-completed] [{:keys [media]}]
  [:.recommendation-completed-title] (html/html-content (media-title media)))

(html/defsnippet specialization-available tmpl [:div.specialization-available] [specialization]
  [:.specialization-available-title] (html/content (:specialization/title specialization)))

(html/defsnippet specialization-completed tmpl [:div.specialization-complete] [specialization]
  [:.specialization-complete-title] (html/content (:specialization/title specialization)))

(html/defsnippet translation-media tmpl [:#recommendation-translation] [{:keys [media]}]
  [:#translation-media-title] (html/html-content (media-title media))
  [:#translation-media-language] (html/content (:media/locale media)))

(html/defsnippet background-media tmpl [:#recommendation-background] [{:keys [media completed]}]
  [:#background-media-title] (html/html-content (media-title media))
  [:#background-media-status] (html/content (str "Is completed: " completed)))

(html/defsnippet recommendation tmpl [:div.recommendation]
  [{:keys [recommendation media backgrounds translations authors]}]
  [:#recommendation-title] (html/html-content (media-title media))
  [:#recommendation-subtitle] (html/content (:media/subtitle media))
  [:#recommendation-authors] (html/content (authors-string authors))
  [:#recommendation-description] (html/html-content
                                  (str (media-url media)
                                       " Why bother: "(:recommendation/description recommendation) " <br/> "
                                       " ISBN: " (:media/isbn media)
                                       " Year of issue: " (:media/year media)
                                       " Necessary: " (:recommendation/necessary recommendation) " Priority: " (:recommendation/priority recommendation)
                                       " Type: " (:media/type media)
                                       " media/id: " (:media/id media)))
  [:#complete] (if (not-all-backgrounds-completed? backgrounds) (html/set-attr :disabled "disabled") (html/set-attr :href (str "complete/" (:recommendation/id recommendation))))
  [:.recommendation-backgrounds] (html/content (map background-media backgrounds))
  [:.recommendation-translations] (html/content (map translation-media translations)))

(html/deftemplate base tmpl
  [data]
  [:span#domain-title] (html/content (:domain/title (model/domain)))
  [:div#inner-content] (html/content (map recommendation (:recommendations data)))
  [:div#specializations-available] (html/substitute (map specialization-available (:specializations-available data)))
  [:div#specializations-completed] (html/substitute (map specialization-completed (:specializations-completed data)))
  [:div#recommendations-completed] (html/substitute (map recommendation-completed (:recommendations-completed data)))
  )

(defn index []
  (render-to-response (base (model/whole-user-data current-user))))

(comment
  (index)
  (model/whole-user-data current-user)
)

(defn mark-as-completed
  ([recommendation]
   (log/info "user: " current-user "mark-as-completed recommendation:" recommendation)
   (model/mark-as-completed current-user recommendation)
   {:status 302
    :headers {"Location" "/"}
    :body ""})
  ([user recommendation]
   (log/info "user: " user "mark-as-completed recommendation:" recommendation)
   (model/mark-as-completed user recommendation)
   (response nil))
  )

;; End of view layer

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
  #_(GET "/" [] (slurp (io/resource "public/html/kmg.html")))
  (GET "/" [] (index))
  (GET "/complete/:recommendation" [recommendation] (mark-as-completed recommendation))

  (GET "/data/:user" [user] (whole-user-data user))
  (context "/recommendation" [] recommendation-routes)
  (context "/specialization" [] specialization-routes)
  (context "/user" [] user-routes)
  (GET "/domain" [] (response (model/domain)))
  (GET "/req" request (str request))
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
