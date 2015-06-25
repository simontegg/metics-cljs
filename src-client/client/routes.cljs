(ns client.routes
  (:require
    [secretary.core :as secretary :refer-macros [defroute]]
    [ajax.core :refer [GET]]
    [goog.events :as events]
    [goog.history.EventType :as EventType])
    (:import goog.history.Html5History
             goog.Uri))

(js/console.log "ROUTES")

(defn hook-browser-navigation! []
  (let [history (doto (Html5History.)
                  (events/listen
                    EventType/NAVIGATE
                    (fn [event]
                      (secretary/dispatch! (.-token event))))
                  (.setUseFragment false)
                  (.setPathPrefix "")
                  (.setEnabled true))]

    (events/listen js/document "click"
                   (fn [e]
                     (. e preventDefault)
                     (let [path (.getPath (.parse Uri (.-href (.-target e))))
                           title (.-title (.-target e))]
                       (when path
                         (. history (setToken path title))))))))

(defroute "/" {:as params}
  (js/console.log (str "group: " (:id params))))

(defn handler [response]
  (.log js/console (str response)))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defonce group-number (atom nil))

(defroute "/group/:id" {:as params}
  (reset! group-number (:id params))
  (js/console.log (str "group/" @group-number))
  (GET (str "/group/" @group-number) {
        :handler handler
        :error-handler: error-handler})
  (js/console.log (str "group: " @group-number)))

(hook-browser-navigation!)
