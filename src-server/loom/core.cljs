(ns loom.core
  (:require [cljs.nodejs :as node]
            [cljs.reader :as reader]
            ; [loom.db :as db]
            [cljs.core.async :refer [put! chan <! >! timeout pub sub pipe]])
  (:require-macros  [cljs.core.async.macros :as am :refer [go go-loop]]
                    [purnam.core :refer [obj arr ? ! ?> !>]]))

(enable-console-print!)
(def express (node/require "express"))
(def gaAnalytics (node/require "ga-analytics"))
(def path (node/require "path"))
(def url (node/require "url"))
(def urlencode (node/require "urlencode"))

(def moment (node/require "moment"))
(def app (express))
(def server (.Server (node/require "http") app))
(def IO (node/require "socket.io"))
(def io (IO server))

(def r (node/require "rethinkdb"))


(defn DBconnect []
  (let [ch (chan)]
  (.connect r #js{:host "localhost" :port 28015} (fn [err conn]
    (if (true? err) (println err))
    (put! ch conn)))
    ch))

(def db-chan (DBconnect))

(go (let [conn (<! db-chan)]
  (->
    (.db r "test")
    (.tableCreate "groups")
    (.run conn (fn [err res]
      (if (true? err) (println err)
      (.log js/console res)))))


))

(defn parseQuery [uri]
  (let [urlObj (.parse url uri true)]
    (aget urlObj "query")))

; paths
(def keyPath (.join path js/__dirname "../key.pem"))
(def staticPath (.join path js/__dirname "../build"))
(def appPath (.join path js/__dirname "../build/app.html"))

(defn setMonth [year month]
  (moment (str year "-" month) "YYYY-MM"))

(defn parseMonthString [string]
  (let [m (moment string "YYYY-MM")]
    [(.year m) (+ (.month m) 1)]))

(defn setFilters [groupId]
  (str "ga:customVarValue4==" groupId ";ga:customVarValue1!=undefined"))

(defn getOptions []
  (let [options #js {
    :clientId "1010918958229-d8r7khmhlne80hbtjmrpivjkjnesou2u.apps.googleusercontent.com"
    :serviceEmail "1010918958229-d8r7khmhlne80hbtjmrpivjkjnesou2u@developer.gserviceaccount.com"
    :key keyPath
    :metrics "ga:users"
    :ids "ga:58892342"
    :dimensions "ga:customVarValue1,ga:year,ga:month"
    }] options))

(defn setOptions [yearMonth groupId]
  (let [m (.startOf (moment yearMonth) "month") end (.endOf (moment yearMonth) "month") options (getOptions)]
    (println (.format m "YYYY-MM-DD"))
    (aset options "startDate" (.format m "YYYY-MM-DD"))
    (aset options "endDate" (.format end "YYYY-MM-DD"))
    (aset options "filter" (setFilters groupId))
    options))

(defn handle-error [options err ch]
  (println err options)
  (put! ch {
    :month (aget options "startDate")
    :activeUsers nil
    :filter (aget options "filter")
    :error: "analytics returned undefined results"}))

(defn fetchDatum [options ch]
  (gaAnalytics options (fn [err, res]
    (println (aget res "query" "start-date"))
    (if (or (true? err) (nil? res)) (handle-error options err ch)
      (let [result {:month (aget options "startDate") :activeUsers (aget res "totalResults") :filter (aget options "filter")}]
        (println "result" result)
        (go (>! ch result)))))))

(defn fetchData [optionsSeq]
  (let [ch (chan 1)]
    (go-loop [i 0]
      (fetchDatum (nth optionsSeq i) ch)
      (<! (timeout 1001))
      (println (str "iteration" i))
      (if (< i (- (count optionsSeq) 1)) (recur (inc i))))
    ch))

(defn getMonthAgoString [monthAgo]
  (.format (.subtract (moment) monthAgo "months") "YYYY-MM"))

(defn getMonthSeq [startMonth n]
  (map #(.add (moment startMonth "YYYY-MM") % "months") (range 0 n)))

(defn setPeriod [query]
  (let [
    startMonth (or (aget query "start") (getMonthAgoString 12) )
    endMonth (or (aget query "end") (getMonthAgoString 1))
    start (moment startMonth "YYYY-MM")
    end (moment endMonth "YYYY-MM")
    diff (.diff end start "months")]
    (println startMonth diff)
    [startMonth diff]))

(defn getOptionsSeq [groupId query]
  (let [ period (setPeriod query) monthSeq (apply getMonthSeq period)]
    (map #(setOptions % groupId) monthSeq)))

(defn pipe-to-client [query res emitter]
  (if-let [groupId (aget query "group")]
    ((let [optionsSeq (getOptionsSeq groupId query) ch (fetchData optionsSeq)]
      (pipe ch emitter))
      ; (go (while true
      ;     (put! emitter (<! ch)))
      (.json res #js{:group groupId})))
    (.json res #js{:error "supply group id -> ?group=[groupId]"}))

(defn serve [req res emitter]
  (let [query (parseQuery (aget req "url")) response (aget query "res")]
    (cond
      (= response "queue") (pipe-to-client query res emitter)
      (= response "reply") (.json res #js{:group "test"})
      :else (.sendFile res appPath))))

(defn connect [server io emitter]
  (println "establishing connection")

  (.listen server 8080)
  (.use app (.static express staticPath))

  (.on io "connection" (fn [socket]
    (println "connection established")
    (go (while true (let [d (<! emitter)]
      (println "d" d)
      (.emit socket "data" (clj->js d))))))))

(defn -main []
  (let [emitter (chan)]
    (connect server io emitter)
    (.get app "/" (fn [req res] (serve req res emitter)))))

(set! *main-cli-fn* -main)
