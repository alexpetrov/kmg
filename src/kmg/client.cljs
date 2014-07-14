(ns kmg.client
  (:require [enfocus.core :as ef :refer (set-attr from at get-prop do-> after
                                                  remove-node content substitute)]
            [enfocus.events :as events :refer (listen)]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(declare try-mark-as-completed try-load-recommendations-completed start)


(def user (atom {:username "user2"}))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])
(em/defsnippet kmg-content tmpl "#content" [])
(em/defsnippet kmg-sidebar tmpl "#sidebar" [])

(em/defsnippet user-choose-elem tmpl ".user-option-id" [user]
  ".user-option-id" (ef/do-> (ef/content user)
                             (ef/set-attr :value user)))

(em/defsnippet recommendation tmpl ".recommendation" [{:keys [recommendation media user]}]
  "#recommendation-title" (ef/content (:media/title media))
  "#recommendation-description" (ef/content
     (str (:recommendation/description recommendation)
          " Necessary: " (:recommendation/necessary recommendation) " Priority: " (:recommendation/priority recommendation))
     )
  "#complete" (events/listen :click #(try-mark-as-completed recommendation)))

(em/defsnippet recommendation-completed tmpl ".recommendation-completed" [{:keys [media]}]
  ".recommendation-completed-title" (ef/content (:media/title media)))

(em/defsnippet specialization-available tmpl ".specialization-available" [specialization]
  ".specialization-available-title" (ef/content (:specialization/title specialization)))

(defn error-handler [{:keys [status status-text]}]
  (js/alert (str "somthing bad happened: " status " " status-text))
  #_(.log js/console (str "somthing bad happened: " status " " status-text)))

(defn recommendation-list [data]
  (ef/at "#inner-content" (ef/content (map recommendation data))))

(defn recommendation-completed-list [data]
  (ef/at "#recommenations-completed" (ef/content (map recommendation-completed data))))

(defn specialization-available-list [data]
  (ef/at "#specializations-available" (ef/content (map specialization-available data))))

(defn try-load-recommendations []
  (GET (str "/recommendation/list/" (:username @user))
       {:handler recommendation-list
        :error-handler error-handler}))

(defn try-load-recommendations-completed []
  (GET (str "/recommendation/completed/" (:username @user))
       {:handler recommendation-completed-list
        :error-handler error-handler}))

(defn try-load-specializations-available []
  (GET (str "/specialization/available/" (:username @user))
       {:handler specialization-available-list
        :error-hadler error-handler}))

(defn refresh []
  (try-load-recommendations)
  (try-load-recommendations-completed)
  (try-load-specializations-available))

(defn try-mark-as-completed [recommendation]
  (POST (str "/recommendation/mark-as-completed/" (:username @user) "/" (:recommendation/id recommendation))
        {:handler refresh
         :error-handler error-handler}))

;; TODO: How to avoid blinking of sample value for select?
;; May be make it hide by-default and make it shown after transformation
(defn show-user-choose [users]
  (ef/at "#users-id" (ef/content (map user-choose-elem users))))

(defn try-get-users []
  (GET "/user/list" {:handler show-user-choose
                     :error-handler error-handler}))


(em/defaction observe-change-user []
  ["#users-id"]
  (events/listen :change
                 #((do (reset! user {:username (ef/from "#users-id" (get-prop :value))})
                       #_(js/alert (str "Before try load recommendations for user: " @user))
                       (refresh)

                       #_(js/alert (str "User changed. Current user: " @user))))))

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header))
                  (ef/append (kmg-content))
                  (ef/append (kmg-sidebar))))
  (refresh)
  #_(try-load-recommendations)
  #_(try-load-recommendations-completed)
  (try-get-users)
  (observe-change-user))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))
