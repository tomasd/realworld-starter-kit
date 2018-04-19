(ns realworld.fx
  (:require
    [re-frame.core :as re-frame]
    [day8.re-frame.http-fx :refer [http-effect]]
    [ajax.core :refer [json-request-format json-response-format]]
    [clojure.string :as str]
    [statecharts.re-frame :as scr]))

(def api-url "https://conduit.productionready.io/api")

(defn endpoint [& params]
  "Concat any params to api-url separated by /"
  (str/join "/" (concat [api-url] params)))

(re-frame/reg-fx
  :fx/sign-in
  (fn [{:keys [credentials on-success on-failure]}]
    (http-effect {:method          :post
                  :uri             (endpoint "users" "login") ;; evaluates to "api/users/login"
                  :params          {:user credentials}      ;; {:user {:email ... :password ...}}
                  :format          (json-request-format)    ;; make sure it's json
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success               ;; trigger login-success
                  :on-failure      on-failure})))
(re-frame/reg-fx
  :fx/sign-up
  (fn [{:keys [registration on-success on-failure]}]        ;; registration = {:username ... :email ... :password ...}
    (http-effect {:method          :post
                  :uri             (endpoint "users")       ;; evaluates to "api/users"
                  :params          {:user registration}     ;; {:user {:username ... :email ... :password ...}}
                  :format          (json-request-format)    ;; make sure it's json
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success               ;; trigger login-success
                  :on-failure      on-failure})))           ;; trigger api-request-error with :login-success


(defn format-errors [payload]
  (->> (get-in payload [:response :errors])
       (mapcat (fn [[key errors]]
                 (map #(str (name key) " " %) errors)))
       (into [])))

(defn ctx-assoc-errors [errors-path]
  (fn [ctx [_ _ payload]]
    (scr/assoc-db-in
      ctx
      errors-path
      (format-errors payload))))

(re-frame/reg-fx
  :fx/get-articles
  (fn [{:keys [params token on-success on-failure]}]        ;; params = {:offset 0 :limit 10}
    (http-effect {:method          :get
                  :uri             (endpoint "articles")    ;; evaluates to "api/articles/feed"
                  :params          params                   ;; include params in the request
                  :headers         (if (seq token)
                                     [:Authorization (str "Token " token)]) ;; get and pass user token obtained during login
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success               ;; trigger get-articles-success event
                  :on-failure      on-failure})))

(defn get-articles [ctx {:keys [params on-success on-failure]}]
  (scr/fx ctx :fx/get-articles {:params     params
                                :token      (scr/get-db-in ctx [:user :token])
                                :on-success on-success
                                :on-failure on-failure}))
(defn ctx-get-articles [{:keys [params-path on-success on-failure] :as x}]
  (fn [ctx _]
    (get-articles ctx x)))

(defn auth-header [token]
  (if (seq token)
    [:Authorization (str "Token " token)]))

(re-frame/reg-fx
  :fx/get-feed-articles
  (fn [{:keys [params token on-success on-failure]}]        ;; params = {:offset 0 :limit 10}
    (http-effect {:method          :get
                  :uri             (endpoint "articles" "feed") ;; evaluates to "api/articles/feed"
                  :params          params                   ;; include params in the request
                  :headers         (auth-header token)      ;; get and pass user token obtained during login
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success               ;; trigger get-articles-success event
                  :on-failure      on-failure})))           ;; This is the only one we need)

(defn get-feed-articles [ctx {:keys [params on-success on-failure]}]
  (scr/fx ctx :fx/get-feed-articles {:params     params
                                     :token      (scr/get-db-in ctx [:user :token])
                                     :on-success on-success
                                     :on-failure on-failure}))

(re-frame/reg-fx
  :fx/get-tags
  (fn [{:keys [on-success on-failure]}]
    (http-effect {:method          :get
                  :uri             (endpoint "tags")        ;; evaluates to "api/articles/feed"
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success               ;; trigger get-articles-success event
                  :on-failure      on-failure})))

(defn get-tags [ctx {:keys [on-success on-failure]}]
  (scr/fx ctx :fx/get-tags {:on-success on-success
                            :on-failure on-failure}))

(re-frame/reg-fx                                            ;; usage (dispatch [:toggle-favorite-article slug])
  :fx/toggle-favorite-article                               ;; triggered when user clicks favorite/unfavorite button on profile page
  (fn [{:keys [slug value token on-success on-failure]}]    ;; slug = :slug
    (http-effect {:method          (if value :post :delete ) ;; check if article is already favorite: yes DELETE, no POST
                  :uri             (endpoint "articles" slug "favorite") ;; evaluates to "api/articles/:slug/favorite"
                  :headers         (auth-header token)      ;; get and pass user token obtained during login
                  :format          (json-request-format)    ;; make sure it's json
                  :response-format (json-response-format {:keywords? true}) ;; json response and all keys to keywords
                  :on-success      on-success
                  :on-failure      on-failure})))

(defn toggle-favorite-article [ctx {:keys [slug value on-success on-failure]}]
  (scr/fx ctx :fx/toggle-favorite-article {:slug       slug
                                           :value      value
                                           :token      (scr/get-db-in ctx [:user :token])
                                           :on-success on-success
                                           :on-failure on-failure}))