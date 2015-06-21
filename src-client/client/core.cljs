(ns client.core
  (:require [cljsjs.react :as react]
            [ajax.core :refer [GET]]
            [reagent.core :as reagent :refer [atom]])
  (:require-macros  [purnam.core :refer [obj arr ? ! ?> !>]]))


; websockets
(def socket (.connect (.io js/window "http://localhost:8080" #js{:reconnectionDelay 500 :reconnectionDelayMax 500})))
(.log js/console socket)

(.log js/console socket)
(.on socket "data" (fn [data]
  (.log js/console data)))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn validate [s]
  (true? (integer? (js/parseInt s))))

(defn not-red [s]
  (or (validate s) (or (nil? s) (= s ""))))

(defonce group-number (atom nil))

(defn submit []
  (if (validate @group-number)
    (GET (str "/?res=queue&group=" @group-number) {
      :handler handler
      :error-handler: error-handler})))

(defn handle-key-press [e]
  (if (= (aget e "charCode") 13) (submit)))



(defn atom-input []
  [:input {
    :type "text"
    :style {:border (if (not-red @group-number) nil "solid red 1px")}
    :id "search"
    :placeholder "Enter group number"
    :value @group-number
    :on-key-press handle-key-press
    :on-change #(reset! group-number (-> % .-target .-value))}])

(defn search-form []
  [:div.box
    [:button.btn {
      :on-click submit
      } (if (not-red @group-number) "View Group Activity" "Numbers only!")]
    [:div.container-1
      [atom-input]]])


(defn by-id [id]
  (.getElementById js/document id))

(reagent/render [search-form]
                (by-id "app"))
