(ns realworld.pages.core)

(defmulti page-content (fn [page args] page))

(defmethod page-content :default [page args]
  [:div "Loading..."])