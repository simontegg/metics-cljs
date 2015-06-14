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


(defsnippet my-nav-item "main.html" [:.nav-item]
  [[caption func]]
  {#_#_[:a] (listen :onClick #(func caption))
   [:h1] (content caption)})

(defsnippet my-header "main.html" [:header]
  [{:keys [heading navigation]}]
  {[:h1] (content heading)
   #_#_ [:ul] (content (map my-nav-item navigation))})

(deftemplate my-page "main.html"
  [data]
  {[:header] (substitute (my-header data))
   [:div] (set-style :color "red")})

(defn init [data] (om/component (my-page data)))

(def app-state (atom {:heading "main"
                      :content    "Hello World"
                      :navigation [["home" #(js/alert %)]
                                   ["next" #(js/alert %)]]}))

(om/root init app-state {:target  (.-body js/document)})
