(ns client.core
  (:require [kioo.om :refer [content set-style set-attr do-> substitute listen]]
            [kioo.core :refer [handle-wrapper]]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true])
  (:require-macros [kioo.om :refer [defsnippet deftemplate]]))

; websockets
(def socket (.connect (.io js/window) "http://localhost"))
(.on socket "data" (fn [data]
  (.log js/console data)))

(defsnippet box "main.html" [:.box]
  [{:keys [test]}]
  {[:.container-1] (content test)})

(deftemplate my-page "main.html"
  [data]
  {[:body] (content box data)})

(defn init [data] (om/component (my-page data)))

(def app-state (atom {:test "TEST" }))

(om/root init app-state {:target  (.-body js/document)})
