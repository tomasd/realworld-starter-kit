(ns realworld.components
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]))

(defn format-date
  [date]
  (.toDateString (js/Date. date)))

(defn tags-list
  [tags-list]
  [:ul.tag-list
   (for [tag tags-list]
     ^{:key tag} [:li.tag-default.tag-pill.tag-outline tag])])

(defn article-meta [{:keys [avatar author date heart slug favorited
                            ]}]
  [:div.article-meta
   [:a {:href "#"}
    [:img {:src (if (seq avatar)
                  avatar)}]]

   [:div.info
    [:a.author {:href "#"}
     author]
    [:span.date date]]
   [:button.btn.btn-outline-primary.btn-sm.pull-xs-right
    {:type     :button
     :on-click #(re-frame/dispatch [:toggle-favorite-article slug (not favorited)])}
    [:i.ion-heart heart]]])

(defn article-meta-other [{:keys [avatar author date favorite follow
                                  ]}]
  [:div.article-meta
   [:a {:href "#"}
    [:img {:src (if (seq avatar)
                  avatar)}]]

   [:div.info
    [:a.author {:href "#"}
     author]
    [:span.date date]]
   [:button.btn.btn-sm.btn-outline-secondary
    [:i.ion-plus-round]
    (str "Follow " author " ") [:span.counter (str "(" follow ")")]]

   [:button.btn.btn-sm.btn-outline-primary
    [:i.ion-heart]
    "Favorite Post" [:span.counter (str "(" favorite ")")]]])

(defn article [article-sub]
  (let [{:keys [author  favoritesCount
                createdAt
                title description tagList slug favorited]} @article-sub]
    [:div.article-preview
     [article-meta {:avatar (:image author)
                    :author (:username author)
                    :date   (format-date createdAt)
                    :heart  favoritesCount
                    :favorited favorited
                    :slug slug}]
     [:a.preview-link {:href "#"}
      [:h1 title]
      [:p description]
      [:span "Read more..."]
      [tags-list tagList]]]))


