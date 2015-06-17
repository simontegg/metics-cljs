(ns client.core
  (:require [cljsjs.react :as react]
            [reagent.core :as reagent :refer [atom]]))

; websockets
(def socket (.connect (.io js/window) "http://localhost"))
(.on socket "data" (fn [data]
  (.log js/console data)))

(defonce group-number (atom 0))

(defn atom-input [value]
  [:input {
    :type "text"
    :id "search"
    :placeholder "Enter group number"
    :value @value
    :on-change #(reset! value (-> % .-target .-value))}])

(defn search-form []
  (let [val (atom "")]
    (fn []
      [:div.box
        [:div.btn "Submit"]
        [:div.container-1
          [atom-input val]]])))



(defn by-id [id]
  (.getElementById js/document id))


(reagent/render [search-form]
                (by-id "app"))
