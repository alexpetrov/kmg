;;(ns kmg.client)
(ns kmg.client
  (:require [enfocus.core :as ef]
            [ajax.core :refer [GET POST]])
  (:require-macros [enfocus.macros :as em]))

(def tmpl "/html/kmg.html")

(em/defsnippet kmg-header tmpl ".kmg-header" [])

(defn start []
  (ef/at ".container"
         (ef/do-> (ef/content (kmg-header)))))

(set! (.-onload js/window) #(em/wait-for-load (start)))

;;(set! (.-onload js/window) (js/alert "Hello, world"))
