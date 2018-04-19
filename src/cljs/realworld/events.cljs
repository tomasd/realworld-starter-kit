(ns realworld.events
  (:require
    realworld.fx
    [re-frame.core :as re-frame]
    [realworld.db :as db]
    [statecharts.core :as sc]
    [statecharts.re-frame :as scr]
    [realworld.views :as views]))

(def auth-statechart
  {:type        :xor
   :init        :anonymous
   :states      {:authenticated views/authenticated-pages
                 :anonymous     views/anonymous-pages}})

(def statechart
  (sc/make {:type   :and
            :states {:auth auth-statechart}}))

(re-frame/reg-event-fx
  ::initialize-db
  (fn [_ _]
    (-> {:db db/default-db}
        (scr/initialize statechart))))
