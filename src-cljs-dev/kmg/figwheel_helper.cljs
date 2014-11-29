(ns kmg.figwheel-helper
  (:require [figwheel.client :as fw]
            [kmg.client :as kmg]
            [enfocus.core :as ef :refer (set-attr from at get-prop do-> after
                                                  remove-node content substitute)]
            [enfocus.events :as events :refer (listen)])
  (:require-macros [enfocus.macros :as em]))

(fw/watch-and-reload
 :jsload-callback (fn []
;;                    (start) ;; (stop-and-start-my app)
                    (kmg/refresh)
                    ))

(set! (.-onload js/window) #(em/wait-for-load (kmg/start)))
