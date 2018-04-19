(ns realworld.pages.article-form
  (:require
    [realworld.form :refer [input]]
    [statecharts.re-frame :as scr]))

(def statechart
  {})

(defn article-form []
  (let [form [:article-form]]
    [:div.container.page
     [:div.row

      [:div.col-md-10.offset-md-1.col-xs-12
       [:form
        [:fieldset
         [input form {:type        :text
                      :path        [:title]
                      :placeholder "Article Title"}]
         [input form {:type        :text
                      :path        [:description]
                      :placeholder "Your What's this article about?"}]
         [input form {:type        :textarea
                      :path        [:text]
                      :rows        8
                      :placeholder "Write your article (in markdown)"}]
         [input form {:type        :text
                      :path        [:tags]
                      :placeholder "Enter tags"}]
         [:button.btn.btn-lg.btn-primary.pull-xs-right
          "Publish Article"]]]]]]))