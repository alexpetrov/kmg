;;(ns kmg.client)
(ns kmg.client
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])
(em/defsnippet kmg-content tmpl "#content" [])

(em/defsnippet user-choose-elem tmpl [user]
  ".user-option-id" (ef/do-> (ef/content user)
                                 (ef/set-attr :value user)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "somthing bad happened: " status " " status-text)))

;; TODO: How to avoid blinking of sample value for select?
(defn show-user-choose [users]
  (ef/at "#users-id" (ef/content (map user-choose-elem users))))

(defn try-get-users []
  (GET "/user/list" {:handler show-user-choose
                     :error-handler error-handler}))

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header))
                  (ef/append (kmg-content))))
  (try-get-users))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))
