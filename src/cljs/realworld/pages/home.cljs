(ns realworld.pages.home
  (:require
    [realworld.components :as components]
    [realworld.fx :as fx]
    [statecharts.re-frame :as scr]
    [cljs-time.coerce :refer [to-long]]
    [re-frame.core :as re-frame]
    [realworld.machines :as machines]))


(defn add-epoch [date coll]
  "Takes date identifier and adds :epoch (cljs-time.coerce/to-long) timestamp to coll"
  (map (fn [item] (assoc item :epoch (to-long (date item)))) coll))
(defn index-by [key coll]
  "Transform a coll to a map with a given key as a lookup value"
  (into {} (map (juxt key identity) (add-epoch :createdAt coll))))

(defn articles-machine [{:keys [reset-params
                                enter exit
                                load-data]}]
  (machines/list-machine
    {:reset-params reset-params
     :enter        enter
     :exit         exit
     :path         [:home :articles]
     :load-data    load-data
     :page-limit   10
     :post-process (fn [payload]
                     (let [{articles :articles, articles-count :articlesCount} payload]
                       {:count    articles-count
                        :id-order (mapv :slug articles)
                        :data     (index-by :slug articles)}))}))
(def statechart
  {:type   :and
   :states {:articles {:type        :xor
                       :init        :global
                       :states      {:feed   (articles-machine
                                               {:reset-params (constantly {:offset 0 :limit 10})
                                                :enter        [(scr/ctx-assoc-db-in [:home :mode] :feed)]
                                                :load-data    fx/get-feed-articles})
                                     :global (articles-machine
                                               {:reset-params (constantly {:offset 0 :limit 10})
                                                :load-data    fx/get-articles
                                                :enter        [(scr/ctx-assoc-db-in [:home :mode] :global)]})
                                     :tag    (articles-machine
                                               {:reset-params (fn [ctx _]
                                                                (merge (scr/get-db-in ctx [:home :articles :params])
                                                                       {:offset 0 :limit 10}))
                                                :load-data    fx/get-articles
                                                :enter        [(scr/ctx-assoc-db-in [:home :mode] :tag)]})}
                       :transitions (-> [{:event    :home/set-tag
                                          :execute  [(fn [ctx [_ tag]]
                                                       (scr/assoc-db-in ctx [:home :articles :params :tag] tag))]
                                          :internal true
                                          :target   :tag}
                                         {:event   :reload-article
                                          :execute [(fn [ctx [_ slug {:keys [article]}]]
                                                      (scr/assoc-db-in ctx [:home :articles :data slug] article))]}]
                                        (machines/goto-internal :goto/mode [:feed :global]))}
            :tags     (machines/list-machine
                        {:path         [:home :tags]
                         :load-data    fx/get-tags
                         :post-process identity})}})

(re-frame/reg-sub
  :home/tags
  (fn [db _]
    (get-in db [:home :tags :tags])))

(re-frame/reg-sub
  :home/articles-ids
  (fn [db _]
    (get-in db [:home :articles :id-order])))

(re-frame/reg-sub
  :home/articles-detail
  (fn [db [_ article-id]]
    (get-in db [:home :articles :data article-id])))

(re-frame/reg-sub
  :home/current-mode?
  (fn [db [_ mode]]
    (= mode (get-in db [:home :mode]))))

(re-frame/reg-sub
  :home/current-tag
  (fn [db _]
    (get-in db [:home :articles :params :tag])))

(re-frame/reg-sub
  :home/articles-status
  (fn [db _]
    (get-in db [:home :articles :status])))

(re-frame/reg-sub
  :home/tags-status
  (fn [db _]
    (get-in db [:home :tags :status])))

(re-frame/reg-sub
  :home/articles-paging
  (fn [db _]
    (let [{:keys [limit offset]} (get-in db [:home :articles :params])
          total (get-in db [:home :articles :count])]
      (machines/pagination-sub limit offset total))))


(defn nav-item [mode label]
  [:li.nav-item
   [:a.nav-link {:href     "#"
                 :class    (when @(re-frame/subscribe [:home/current-mode? mode])
                             :active)
                 :on-click #(do
                              (re-frame/dispatch [:goto/mode mode])
                              (.preventDefault %))}
    label]])

(defn articles-list []
  [:div.feed-toggle
   [:ul.nav.nav-pills.outline-active
    (when (= @(re-frame/subscribe [:user-status]) :authenticated)
      [nav-item :feed "Your feed"])
    [nav-item :global "Global feed"]
    (when @(re-frame/subscribe [:home/current-mode? :tag])
      [:li.nav-item
       [:a.nav-link.active (str "# " @(re-frame/subscribe [:home/current-tag]))]])]


   (let [status @(re-frame/subscribe [:home/articles-status])]
     (case status
       :loading
       [:div "Loading articles..."]

       (:loaded :page-loading)
       [:div
        (let [article-ids @(re-frame/subscribe [:home/articles-ids])]
          (if (seq article-ids)
            (doall
              (for [article-id @(re-frame/subscribe [:home/articles-ids])]
                ^{:key article-id}
                [components/article (re-frame/subscribe [:home/articles-detail article-id])]))
            [:div "No articles are here... yet."]
            ))
        (when (= status :page-loading)
          [:div "Loading articles..."])
        [machines/pagination [:home :articles] (re-frame/subscribe [:home/articles-paging])]]

       :error
       [:div "Error occured while loading articles..."]))])

(defn tags-list []
  (case @(re-frame/subscribe [:home/tags-status])
    :loading
    [:div "Loading tags..."]

    :loaded
    [:div.tag-list
     (doall
       (for [tag @(re-frame/subscribe [:home/tags])]
         [:a.tag-pill.tag-default
          {:href     "#"
           :key      tag
           :on-click #(do
                        (re-frame/dispatch [:home/set-tag tag])
                        (.preventDefault %))}
          tag]))]

    :error
    [:div "Error occured while loading tags..."]))

(defn home-page []
  [:div.home-page
   [:div.banner

    [:div.container
     [:h1.logo-font "conduit"]
     [:p "A place to share your knowledge."]]]

   [:div.container.page
    [:div.row
     [:div.col-md-9
      [articles-list]]

     [:div.col-md-3
      [:div.sidebar
       [:p "Popular Tags"]

       [tags-list]]]]]])