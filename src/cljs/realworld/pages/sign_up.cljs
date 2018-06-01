(ns realworld.pages.sign_up
  (:require
    [realworld.form :refer [input]]
    [realworld.fx :as fx]
    [statecharts.re-frame :as scr]
    [realworld.form :as form]
    [re-frame.core :as re-frame]
    [realworld.pages.core :as pages]))

(def statechart
  (let [form-path [:sign-up]]
    (form/form-machine
      {:path        form-path
       :reset       (constantly {:email    ""
                                 :username ""
                                 :password ""})
       :on-submit   (fn [ctx value]
                      (scr/fx ctx :fx/sign-up {:registration value
                                               :on-success   [:sign-up.success form-path]
                                               :on-failure   [:sign-up.error form-path]}))
       :transitions [{:event :sign-up.success
                      :target [:auth :authenticated]}
                     {:event   :sign-up.error
                      :execute [(fx/ctx-assoc-errors (conj form-path :server-errors))]}]})))

(re-frame/reg-sub
  :sign-up/server-errors
  (fn [db _]
    (get-in db [:sign-up :server-errors])))

(defn sign-up-page []
  (let [form [:sign-up]]
    [:div.auth-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Sign up"]
        [:p.text-xs-center
         [:a {:href "#"}
          "Have an account?"]]

        (if-let [errors @(re-frame/subscribe [:sign-up/server-errors])]
          (when (seq errors)
            (into [:ul.error-messages]
                  (for [message errors]
                    [:li message]))))

        [:form {:on-submit (form/handle-submit form)}
         [input form {:type        :text
                      :path        [:username]
                      :placeholder "Your name"}]
         [input form {:type        :text
                      :path        [:email]
                      :placeholder "Email"}]
         [input form {:type        :password
                      :path        [:password]
                      :placeholder "Password"}]
         [:button.btn.btn-lg.btn-primary.pull-xs-right
          {:type :submit}
          "Sign up"]]]]]]))

(defmethod pages/page-content :page/sign-up [page args]
  [sign-up-page])