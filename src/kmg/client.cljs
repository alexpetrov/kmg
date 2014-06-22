;;(ns kmg.client)
(ns kmg.client
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])
(em/defsnippet user-choose tmpl "#user-choose" [users]
  ".user-option-id" (em/clone-for [user users]
                        (ef/do-> (ef/content user)
                                 (ef/set-attr :value user))))

(defn try-get-users []
  ["user1" "user2"])

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header))
                  (ef/append (user-choose (try-get-users))))))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))
