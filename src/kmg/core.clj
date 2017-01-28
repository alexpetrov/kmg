(ns kmg.core
  (:require [compojure.route :as route]
            [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [kmg.domain-facade :as model]
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

;; View layer
;; FIXME: Extract View layer to separate namespace
(defn render [t]
  (reduce str t))

(def render-to-response
  (comp response render))

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
  [:.specialization-available-title] (html/content (:specialization/title specialization))
  [:#choose] (html/set-attr :href (str "choose/" (:specialization/id specialization))))

(html/defsnippet specialization-completed tmpl [:div.specialization-completed] [specialization]
  [:.specialization-completed-title] (html/content (:specialization/title specialization))
  [:#show-recommendations] nil) ;; TODO: Implement somehow showing completed specialization recommendations in future

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
  [:#complete] (if (not-all-backgrounds-completed? backgrounds)
                 (html/set-attr :disabled "disabled")
                 (html/set-attr :href (str "complete/" (:recommendation/id recommendation))))
  [:.recommendation-backgrounds] (html/content (map background-media backgrounds))
  [:.recommendation-translations] (html/content (map translation-media translations)))

(html/deftemplate base tmpl
  [data]
  [:span#domain-title] (html/content (:domain/title (model/domain)))
  [:div#inner-content] (html/content (map recommendation (:recommendations data)))
  [:div#specializations-available] (html/substitute (map specialization-available (:specializations-available data)))
  [:div#specializations-completed] (html/substitute (map specialization-completed (:specializations-completed data)))
  [:div#recommendations-completed] (html/substitute (map recommendation-completed (:recommendations-completed data))))

(defn index []
  (render-to-response (base (model/whole-user-data current-user))))

;; End of view layer

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
