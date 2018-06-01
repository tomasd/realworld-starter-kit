(ns realworld.pages.profile
  (:require [realworld.components :as components]
            [statecharts.re-frame :as scr]
            [realworld.pages.core :as pages]))

(def statechart
  {})

(defn nav-item [page label]
  [:li.nav-item
   [:a.nav-link {:href "#"}
    label]])

(defn profile-page []
  [:div.profile-page
   [:div.user-info
    [:div.container
     [:div.row
      [:div.col-xs-12.col-md-10.offset-md-1
       [:img.user-img {:src "http://i.imgur.com/Qr71crq.jpg"}]
       [:h4 "Eric Simons"]
       [:p "Cofounder @GoThinkster, lived in Aol's HQ for a few months, kinda looks like Peeta from the Hunger Games"]
       [:button.btn.btn-sm.btn-outline-secondary.action-btn
        [:i.ion-plus-round]
        "Follow Eric Simons"]]]]]

   [:div.container
    [:div.row
     [:div.col-xs-12.col-md-10.offset-md-1
      [:div.articles-toggle
       [:ul.nav.nav-pills.outline-active
        [nav-item :my-articles "My Articles"]
        [nav-item :favorite-articles "Favorited Articles"]]]

      [components/article {:avatar      "http://i.imgur.com/Qr71crq.jpg"
                           :author      "Eric Simons"
                           :date        "January 20th"
                           :heart       29
                           :title       "How to build webapps that scale"
                           :description "This is the description for the post."}]]]]])

(defmethod pages/page-content :page/profile [page args]
  [profile-page])