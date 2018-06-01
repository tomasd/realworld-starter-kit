(ns realworld.pages.sign_in
  (:require
    [realworld.form :as form :refer [input]]
    [statecharts.re-frame :as scr]
    [re-frame.core :as re-frame]
    [realworld.fx :as fx]
    [realworld.pages.core :as pages]))

(def statechart
  (let [form-path [:sign-in-form]]
    (form/form-machine
      {:path        form-path
       :reset       (constantly {:email    ""
                                 :password ""})
       :on-submit   (fn [ctx value]
                      (scr/fx ctx :fx/sign-in
                              {:credentials value
                               :on-success  [:sign-in.success form-path]
                               :on-failure  [:sign-in.failure form-path]}))
       :transitions [{:event   :sign-in.success
                      :execute [(fn [ctx [_ _ {:keys [user]}]]
                                  (scr/assoc-db-in ctx [:user] user))]
                      :target  [:auth :authenticated]}
                     {:event   :sign-in.failure
                      :execute [(fx/ctx-assoc-errors (conj form-path :server-errors))]}]})))

(re-frame/reg-sub
  :sign-in/server-errors
  (fn [db _]
    (get-in db [:sign-in-form :server-errors])))

(defn sign-in-page []
  (let [form [:sign-in-form]]
    [:div.auth-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Sign up"]
        [:p.text-xs-center
         [:a {:href "#"}
          "Have an account?"]]

        (if-let [errors @(re-frame/subscribe [:sign-in/server-errors])]
          (when (seq errors)
            (into [:ul.error-messages]
                  (for [message errors]
                    [:li message]))))

        [:form {:on-submit (form/handle-submit form)}
         [input form {:type        :text
                      :path        [:email]
                      :placeholder "Email"}]
         [input form {:type        :password
                      :path        [:password]
                      :placeholder "Password"}]
         [:button.btn.btn-lg.btn-primary.pull-xs-right
          {:type :submit}
          "Sign up"]]]]]]))

(defmethod pages/page-content :page/sign-in [page args]
  [sign-in-page])