(ns realworld.pages.settings
  (:require
    [realworld.form :refer [input]]
    [statecharts.re-frame :as scr]
    [realworld.pages.core :as pages]))

(def statechart
  {})

(defn settings-page []
  (let [form [:settings-form]]
    [:div.container.page
     [:div.row

      [:div.col-md-6.offset-md-3.col-xs-12
       [:h1.text-xs-center "Your Settings"]

       [:form
        [:fieldset
         [input form {:type        :text
                      :path        [:avatar]
                      :placeholder "Your URL of profile picture"}]
         [input form {:type        :text
                      :path        [:name]
                      :placeholder "Your name"}]
         [input form {:type        :textarea
                      :path        [:bio]
                      :rows        8
                      :placeholder "Short bio about you"}]
         [input form {:type        :email
                      :path        [:email]
                      :placeholder "Email"}]
         [input form {:type        :password
                      :path        :password
                      :placeholder "Password"}]
         [:button.btn.btn-lg.btn-primary.pull-xs-right
          "Update Settings"]]]]]]))

(defmethod pages/page-content :page/settings [page args]
  [settings-page])