(ns realworld.pages.article
  (:require
    [realworld.components :as components]
    [statecharts.re-frame :as scr]))

(def statechart
  {})

(defn article-page []
  [:div.article-page
   [:div.banner
    [:div.container
     [:h1 "How to build webapps that scale"]


     [components/article-meta-other
      {:avatar      "http://i.imgur.com/Qr71crq.jpg"
       :author      "Eric Simons"
       :date        "January 20th"
       :favorite 29
       :follow 10
       }]]]



   [:div.container.page
    [:div.row.article-content
     [:div.col-md-12
      [:p "Web development technologies have evolved at an incredible clip over the past few years."]
      [:h2#introducing-ionic "Introducing RealdWorld"]
      [:p "It's a great solution for learning how other frameworks work."]]]

    [:hr]

    [:div.article-actions
     [:div.banner
      [:div.container
       [:h1 "How to build webapps that scale"]


       [components/article-meta-other
        {:avatar      "http://i.imgur.com/Qr71crq.jpg"
         :author      "Eric Simons"
         :date        "January 20th"
         :favorite 29
         :follow 10
         }]]]]

    [:div.row
     [:div.col-xs-12.col-md-8.offset-md-2
      [:form.card.comment-form
       [:div.card-block
        [:textarea.form-control
         {:placeholder "Write a comment..."
          :rows 3}]]
       [:div.card-footer
        [:img.comment-author-img {:src "http://i.imgur.com/Qr71crq.jpg"}]
        [:button.btn.btn-sm.btn-primary
         "Post Comment"]
        ]]

      [:div.card
       [:div.card-block
        [:p.card-text "With supporting text below as a natural lead-in to additional content."]]
       [:div.card-footer
        [:a.comment-author {:href "#"}
         [:img.comment-author-img {:src "http://i.imgur.com/Qr71crq.jpg"}]]
        [:a.comment-author {:href "#"}
         "Jacob Schmidt"]
        [:span.date-posted "Dec 29th"]]]

      [:div.card
       [:div.card-block
        [:p.card-text "With supporting text below as a natural lead-in to additional content."]]
       [:div.card-footer
        [:a.comment-author {:href "#"}
         [:img.comment-author-img {:src "http://i.imgur.com/Qr71crq.jpg"}]]
        [:a.comment-author {:href "#"}
         "Jacob Schmidt"]
        [:span.date-posted "Dec 29th"]
        [:span.mod-options
         [:i.ion-edit]
         [:i.ion-trash-a]]]]]]]])