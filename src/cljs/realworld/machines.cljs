(ns realworld.machines
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]))

(defn parameterize-transitions [machine path]
  (update machine :transitions (fn [transitions]
                                 (->> transitions
                                      (mapv #(assoc % :condition (fn [ctx [_ current-path]]
                                                                   (= path current-path))))))))
(defn page-machine [{:keys [init states enter exit transitions]}]
  {:type        :xor
   :init        init
   :states      (->> states
                     (mapv (fn [[state-id state]]
                             [state-id (update state :enter (fnil conj [])
                                               (fn [ctx [_ page params]]
                                                 (scr/assoc-db-in ctx [:current-page] {:page   state-id
                                                                                       :params params})))]))
                     (into {}))
   :enter       enter
   :exit        exit
   :transitions (-> []
                    (into transitions)
                    (into (->> states
                               (map (fn [[state-id state]]
                                      {:event     :goto/page
                                       :condition (fn [ctx [_ page]]
                                                    (= page state-id))
                                       :target    state-id})))))})






(defn list-machine [{:keys [reset-params
                            path
                            load-data
                            post-process
                            enter
                            exit
                            transitions
                            page-limit]
                     :or   {post-process (fn [payload] {:data payload})
                            reset-params (constantly {})}}]
  (let [params-path   (conj path :params)
        status-path   (conj path :status)
        ctx-load-data (fn [ctx event]
                        (-> ctx
                            (load-data {:params     (scr/get-db-in ctx params-path)
                                        :on-success [:list.load-data/success path]
                                        :on-failure [:list.load-data.failure path]})))]
    (-> {:type        :xor
         :enter       (-> []
                          (into enter)
                          (into [(fn [ctx event]
                                   (scr/assoc-db-in ctx params-path (reset-params ctx event)))
                                 ctx-load-data]))
         :exit        (-> []
                          (into [(scr/ctx-dissoc-db-in path)])
                          (into exit))
         :init        :loading
         :states      {:loading      {:enter [(scr/ctx-assoc-db-in status-path :loading)]}
                       :page-loading {:enter [(scr/ctx-assoc-db-in status-path :page-loading)]}
                       :error        {:enter [(scr/ctx-assoc-db-in status-path :error)]}
                       :loaded       {:enter [(scr/ctx-assoc-db-in status-path :loaded)]}}
         :transitions (-> [{:event    :list.load-data/success
                            :internal true
                            :execute  [(fn [ctx [_ _ payload]]
                                         (scr/update-db-in ctx path merge (post-process payload)))]
                            :target   :loaded}
                           {:event    :list.load-data.failure
                            :internal true
                            :target   :error}
                           {:event    :list/set-params
                            :execute  [(fn [ctx [_ _ params]]
                                         (scr/assoc-db-in ctx params-path params))]
                            :internal true
                            :target   :loading}]
                          (into transitions)
                          (cond-> (some? page-limit)
                                  (into [{:event    :list/next-page
                                          :internal true
                                          :execute  [(scr/ctx-update-db-in (into path [:params :offset]) (fnil + 0) page-limit)
                                                     ctx-load-data]
                                          :target   :page-loading}
                                         {:event    :list/prev-page
                                          :internal true
                                          :execute  [(scr/ctx-update-db-in (into path [:params :offset]) (fnil - 0) page-limit)
                                                     ctx-load-data]
                                          :target   :page-loading}
                                         {:event    :list/goto-page
                                          :internal true
                                          :execute  [(fn [ctx [_ _ page]]
                                                       (scr/assoc-db-in ctx (into path [:params :offset]) (* page page-limit)))
                                                     ctx-load-data]
                                          :target   :page-loading}])))}
        (parameterize-transitions path))))

(defn pagination [path paging]
  (let [{:keys [next-page? prev-page? pages current]} @paging]
    [:nav
     (into [:ul.pagination]
           (for [page pages]
             [:li.page-item
              {:class (when (= current page)
                        "active")}
              [:a.page-link {:href     "#"
                             :on-click #(do
                                          (re-frame/dispatch [:list/goto-page path page])
                                          (.preventDefault %))}
               (inc page)]]))]))


(defn pagination-sub [limit offset total]
  {:next-page? (< (+ offset limit) total)
   :prev-page? (not (zero? offset))
   :pages      (range (divide total limit))
   :current    (divide offset limit)})

(defn goto [transitions event-name target-states]
  (into transitions
        (map #(do {:event     event-name
                   :condition (fn [ctx [_ state]]
                                (= % state))
                   :target    %}) target-states)))

(defn goto-internal [transitions event-name target-states]
  (into transitions
        (map #(do {:event     event-name
                   :internal  true
                   :condition (fn [ctx [_ state]]
                                (= % state))
                   :target    %}) target-states)))