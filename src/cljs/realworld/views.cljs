(ns realworld.views
  (:require [re-frame.core :as re-frame]
            [realworld.subs :as subs]
            [realworld.pages.core :as pages]
            [realworld.pages.home :refer [home-page]]
            [realworld.pages.sign_in :refer [sign-in-page]]
            [realworld.pages.sign_up :refer [sign-up-page]]
            [realworld.pages.profile :refer [profile-page]]
            [realworld.pages.settings :refer [settings-page]]
            [realworld.pages.article-form :refer [article-form]]
            [realworld.pages.article :refer [article-page]]
            [statecharts.re-frame :as scr]
            [realworld.machines :as machines]
            [statecharts.path :as path]
            [realworld.fx :as fx]))


(def authenticated-pages
  (machines/page-machine
    {:init        :page/home
     :enter       [(scr/ctx-assoc-db-in [:user-status] :authenticated)]
     :exit        [(scr/ctx-dissoc-db-in [:user])]
     :states      {:page/home         realworld.pages.home/statechart
                   :page/profile      realworld.pages.profile/statechart
                   :page/settings     realworld.pages.settings/statechart
                   :page/article-form realworld.pages.article-form/statechart
                   :page/article      realworld.pages.article/statechart}
     :transitions [{:event  :logout
                    :target (path/sibling :anonymous)}
                   {:event   :toggle-favorite-article
                    :execute [(fn [ctx [_ slug value]]
                                (fx/toggle-favorite-article ctx {:slug       slug
                                                                 :value      value
                                                                 :on-success [:reload-article slug]}))]}]}))

(def anonymous-pages
  (machines/page-machine
    {:enter       [(scr/ctx-assoc-db-in [:user-status] :anonymous)]
     :init        :initialize-from-url
     :states      {:initialize-from-url {:enter [(fn [ctx _]

                                                   (-> ctx
                                                       (scr/push-event  [:goto/page :page/home])
                                                       ;(scr/dispatch  [:goto/page :page/home])
                                                       ))]}
                   :page/home           realworld.pages.home/statechart
                   :page/sign-in        realworld.pages.sign_in/statechart
                   :page/sign-up        realworld.pages.sign_up/statechart}
     :transitions [{:event  :toggle-favorite-article
                    :target :page/sign-in}]}))

(re-frame/reg-sub
  :current-page
  (fn [db _]
    (get-in db [:current-page :page])))

(re-frame/reg-sub
  :user-status
  (fn [db _]
    (:user-status db)))

(defn nav-item [page label]
  [:li.nav-item
   [:a.nav-link {:href     "#"
                 :on-click (fn [e]
                             (re-frame/dispatch [:goto/page page])
                             (.preventDefault e))}
    label]])

(defn top-navbar []
  (let [user-status @(re-frame/subscribe [:user-status])]
    [:nav.navbar.navbar-light
     [:div.container
      [:a.navbar-brand {:href "index.html"} "conduit"]

      (case user-status
        :authenticated
        [:ul.nav.navbar-nav.pull-xs-right
         [nav-item :page/home "Home"]
         [nav-item :page/new-post [:i.ion-compose " New Post"]]
         [nav-item :page/settings [:i.ion-gear-a " Settings"]]]

        [:ul.nav.navbar-nav.pull-xs-right
         [nav-item :page/home "Home"]

         [nav-item :page/sign-in " Sign in"]
         [nav-item :page/sign-up " Sign up"]])]]))


(defn main-panel []
  (let [page @(re-frame/subscribe [:current-page])]
    [:div
     [top-navbar]
     [:div (pr-str page)]
     [pages/page-content page {}]]))