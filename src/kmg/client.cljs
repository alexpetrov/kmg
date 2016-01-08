(ns kmg.client
  (:require [enfocus.core :as ef :refer (set-attr from at get-prop do-> after
                                                  remove-node content substitute)]
            [enfocus.events :as events :refer (listen)]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(declare try-mark-as-completed try-change-goal try-load-recommendations try-load-recommendations-completed start media-title)

(enable-console-print!)

(def user (atom {:username "user2"}))
(def user-data (atom {}))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])
(em/defsnippet kmg-content tmpl "#content" [])
(em/defsnippet kmg-sidebar tmpl "#sidebar" [])

(em/defsnippet user-choose-elem tmpl ".user-option-id" [user]
  ".user-option-id" (ef/do-> (ef/content user)
                             (ef/set-attr :value user)))

(em/defsnippet background-media tmpl "#recommendation-background" [{:keys [media completed]}]
  "#background-media-title" (ef/content (str (media-title media)))
  "#background-media-status" (ef/content (str "Is completed: " completed)))

(em/defsnippet translation-media tmpl "#recommendation-translation" [{:keys [media]}]
  "#translation-media-title" (ef/content (str (media-title media)))
  "#translation-media-language" (ef/content (str (:media/locale media))))

(em/defsnippet domain-title tmpl "#domain-title" [domain]
  "#domain-title" (ef/content (str (:domain/title domain))))

(defn not-all-backgrounds-completed? [backgrounds]
  (some false? (for [b backgrounds] (:completed b))))

(defn authors-string [authors]
  (->> (map :author authors)
     (map :author/name)
     (clojure.string/join ", ")))

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

(defn media-url [media]
  (if (:media/url media)
    (str "<a href='" (:media/url media) "'>" (:media/url media) "</a> <br/> ")
    ""))

(em/defsnippet recommendation tmpl ".recommendation" [{:keys [recommendation media backgrounds translations authors]}]
  "#recommendation-title" (ef/content (media-title media))
  "#recommendation-subtitle" (ef/content (str (:media/subtitle media) " " ))
  "#recommendation-authors" (ef/content (authors-string authors))
  "#recommendation-description" (ef/content
     (str (media-url media)
          (:recommendation/description recommendation) " <br/> "
          (:media/isbn media)
          " Year of issue: " (:media/year media)
          " Necessary: " (:recommendation/necessary recommendation) " Priority: " (:recommendation/priority recommendation)
          " Type: " (:media/type media)))

  "#complete" (events/listen :click #(try-mark-as-completed recommendation))
  "#complete" (if (not-all-backgrounds-completed? backgrounds) (ef/set-attr :disabled "disabled"))
  "#recommendation-backgrounds-title" (if (empty? backgrounds) (ef/set-attr :hidden "hidden"))
  "#recommendation-translations-title" (if (empty? translations) (ef/set-attr :hidden "hidden"))

  ".recommendation-backgrounds" (ef/content (map background-media backgrounds))
  ".recommendation-translations" (ef/content (map translation-media translations)))

(em/defsnippet recommendation-completed tmpl ".recommendation-completed" [{:keys [media]}]
  ".recommendation-completed-title" (ef/content (media-title media)))

(em/defsnippet specialization-available tmpl ".specialization-available" [specialization]
  ".specialization-available-title" (ef/content (:specialization/title specialization))
  "#choose" (events/listen :click #(try-change-goal specialization)))

(em/defsnippet specialization-comleted tmpl ".specialization-completed" [specialization]
  ".specialization-completed-title"
  (ef/content (:specialization/title specialization))
  "#show-recommendations" (events/listen :click #(try-load-recommendations specialization)))

(defn error-handler [{:keys [status status-text response]}]
  (js/alert (str "something bad happened: " status " " status-text))
  (println  (str "Something bad happened: " status "; Status text: " status-text "; Response: " response)))

(defn recommendation-list [data]
  (ef/at "#inner-content"
         (ef/content (map recommendation data))))

(defn recommendation-completed-list [data]
  (ef/at "#recommenations-completed"
         (ef/content (map recommendation-completed data))))

(defn specialization-available-list [data]
  (ef/at "#specializations-available"
         (ef/content (map specialization-available data))))

(defn specialization-completed-list [data]
  (ef/at "#specializations-completed"
         (ef/content (map specialization-comleted data))))

(defn data-changed? [data data-part]
  (if (= {} @user-data) true
      (not= (data-part data) (data-part @user-data))))

(defn render-if-changed [render-function data data-part]
  (if (data-changed? data data-part) (render-function (data-part data))))

(defn render-whole-user-data [data]
  #_(recommendation-list (:recommendations data))
  (render-if-changed recommendation-list data :recommendations)
  (render-if-changed recommendation-completed-list data :recommendations-completed)
  (render-if-changed specialization-available-list data :specializations-available)
  (render-if-changed specialization-completed-list data :specializations-completed)
  (reset! user-data data))

(defn try-load-recommendations
  ([]
  (GET (str "/recommendation/list/" (:username @user))
       {:handler recommendation-list
        :error-handler error-handler}))
  ([specialization] (GET (str "/recommendation/list/" (:username @user) "/" (:specialization/id specialization))
       {:handler recommendation-list
        :error-handler error-handler})))

(defn try-load-whole-user-data []
  (GET (str "/data/" (:username @user))
       {:handler render-whole-user-data
        :error-handler error-handler}))

(defn refresh []
  (try-load-whole-user-data))

(defn try-mark-as-completed [recommendation]
  (POST (str "/recommendation/mark-as-completed/" (:username @user) "/" (:recommendation/id recommendation))
        {:handler refresh
         :error-handler error-handler}))

(defn try-change-goal [specialization]
  (POST (str "/specialization/change-goal/" (:username @user) "/" (:specialization/id specialization))
        {:handler refresh
         :error-handler error-handler}))
;; TODO: How to avoid blinking of sample value for select?
;; May be make it hide by-default and make it shown after transformation
(defn show-user-choose [users]
  (ef/at "#users-id" (ef/content (map user-choose-elem users))))

(defn show-domain [domain]
  (ef/at "#domain-title" (ef/content (:domain/title domain))))

(defn try-load-domain []
  (GET "/domain" {:handler show-domain
                  :error-handler error-handler}))

(defn try-get-users []
  (GET "/user/list" {:handler show-user-choose
                     :error-handler error-handler}))

(em/defaction observe-change-user []
  ["#users-id"]
  (events/listen :change
                 #((do (reset! user
                               {:username (ef/from "#users-id" (get-prop :value))})
                       (refresh)))))

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header))
                  (ef/append (kmg-content))
                  (ef/append (kmg-sidebar))))
  (try-load-domain)
  (refresh)
  (try-get-users)
  (observe-change-user))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))
