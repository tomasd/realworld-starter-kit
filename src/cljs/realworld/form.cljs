(ns realworld.form
  (:require [statecharts.re-frame :as scr]
            [re-frame.core :as re-frame]
            [realworld.machines :as machines]))


(defn form-machine [{:keys [path reset on-submit transitions]}]
  (let [form-path  path
        value-path (conj form-path :value)]
    (-> {:enter       [(fn [ctx event]
                         (scr/assoc-db-in ctx value-path (reset ctx event)))]
         :exit        [(scr/ctx-dissoc-db-in form-path)]
         :transitions (-> [{:event   :form/set-value
                            :execute [(fn [ctx [_ _ field-path value]]
                                        (scr/assoc-db-in ctx (into value-path field-path) value))]}
                           {:event   :form/submit
                            :execute [(fn [ctx _]
                                        (on-submit ctx (scr/get-db-in ctx value-path)))]}]
                          (into transitions))}
        (machines/parameterize-transitions form-path))))

(re-frame/reg-sub
  :form/root
  (fn [db [_ form-path]]
    (get-in db form-path)))

(re-frame/reg-sub
  :form/value
  (fn [[_ form-path]]
    (re-frame/subscribe [:form/root form-path]))
  (fn [db _]
    (:value db)))

(re-frame/reg-sub
  :form/errors
  (fn [[_ form-path]]
    (re-frame/subscribe [:form/root form-path]))
  (fn [form _]
    (:errors form)))

(re-frame/reg-sub
  :form/errors?
  (fn [[_ form-path]]
    (re-frame/subscribe [:form/root form-path]))
  (fn [form _]
    (seq (:errors form))))

(re-frame/reg-sub
  :form/field-value
  (fn [[_ form-path]]
    (re-frame/subscribe [:form/value form-path]))
  (fn [form [_ _ field-path]]
    (get-in form field-path)))

(defmulti input (fn [form-path attributes]
                  (:type attributes)))

(defmethod input :default
  [form-path {:keys [type placeholder path]
              :or   {type :text}}]
  [:fieldset.form-group
   [:input.form-control.form-control-lg
    {:type        type
     :on-change   #(re-frame/dispatch [:form/set-value form-path path (-> % .-target .-value)])
     :value       @(re-frame/subscribe [:form/field-value form-path path])
     :placeholder placeholder}]])

(defn handle-submit [form-path]
  (fn [e]
    (js/console.log "xxx" [:form/submit form-path])
    (re-frame/dispatch [:form/submit form-path])
    (.preventDefault e)))