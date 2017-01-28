(ns kmg.view
  (:require [net.cgrand.enlive-html :as html]
            [kmg.domain-facade :as model]))

(defn render [t]
  (reduce str t))

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

(html/defsnippet current-specialization tmpl [:div#current-specialization] [{:keys [specialization/title specialization/annotation]}]
  [:.current-specialization-title] (html/content title)
  [:.current-specialization-annotation] (html/content annotation))

(html/defsnippet specialization-available tmpl [:div.specialization-available] [specialization]
  [:.specialization-available-title] (html/content (:specialization/title specialization))
  [:.choose] (html/set-attr :href (str "choose/" (:specialization/id specialization))))

(html/defsnippet specialization-completed tmpl [:div.specialization-completed] [specialization]
  [:.specialization-completed-title] (html/content (:specialization/title specialization))
  [:.show-recommendations] nil) ;; TODO: Implement somehow showing completed specialization recommendations in future

(html/defsnippet translation-media tmpl [:.recommendation-translation] [{:keys [media]}]
  [:.translation-media-title] (html/html-content (media-title media))
  [:.translation-media-language] (html/content (:media/locale media)))

(html/defsnippet background-media tmpl [:.recommendation-background] [{:keys [media completed]}]
  [:.background-media-title] (html/html-content (media-title media))
  [:.background-media-status] (html/content (str "Is completed: " completed)))

(html/defsnippet recommendation tmpl [:div.recommendation]
  [{:keys [recommendation media backgrounds translations authors]}]
  [:.recommendation-title] (html/html-content (media-title media))
  [:.recommendation-subtitle] (html/content (:media/subtitle media))
  [:.recommendation-authors] (html/content (authors-string authors))
  [:.recommendation-description] (html/html-content
                                  (str (media-url media)
                                       " Why bother: "(:recommendation/description recommendation) " <br/> "
                                       " ISBN: " (:media/isbn media)
                                       " Year of issue: " (:media/year media)
                                       " Necessary: " (:recommendation/necessary recommendation) " Priority: " (:recommendation/priority recommendation)
                                       " Type: " (:media/type media)
                                       " media/id: " (:media/id media)))
  [:.complete] (if (not-all-backgrounds-completed? backgrounds)
                 (html/set-attr :disabled "disabled")
                 (html/set-attr :href (str "complete/" (:recommendation/id recommendation))))
  [:.recommendation-backgrounds-title] (if (empty? backgrounds) (html/substitute nil) identity)
  [:.recommendation-backgrounds] (html/content (map background-media backgrounds))
  [:.recommendation-translations-title] (if (empty? translations) (html/substitute nil) identity)
  [:.recommendation-translations] (html/content (map translation-media translations)))

(html/deftemplate base tmpl
  [data]
  [:span#domain-title] (html/content (:domain/title (model/domain)))
  [:div#current-specialization] (html/substitute (current-specialization (:current-specialization data)))
  [:div#inner-content] (html/content (map recommendation (:recommendations data)))
  [:.specializations-available-title] (if (empty? (:specializations-available data)) (html/substitute nil) identity)
  [:div#specializations-available] (html/substitute (map specialization-available (:specializations-available data)))
  [:.specializations-completed-title] (if (empty? (:specializations-completed data)) (html/substitute nil) identity)
  [:div#specializations-completed] (html/substitute (map specialization-completed (:specializations-completed data)))
  [:.recommendations-completed-title] (if (empty? (:recommendations-completed data)) (html/substitute nil) identity)
  [:div#recommendations-completed] (html/substitute (map recommendation-completed (:recommendations-completed data))))
