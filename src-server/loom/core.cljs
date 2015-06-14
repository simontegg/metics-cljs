(ns loom.core
  (:require [cljs.nodejs :as node]
            [cljs.reader :as reader]
            [cljs.core.async :refer [put! chan <! >!]])
  (:require-macros  [cljs.core.async.macros :as am :refer [go]]))

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


(defn parseQuery [uri]
  (let [urlObj (.parse url uri true)]
    (aget urlObj "query")))

; paths
(def keyPath (.join path js/__dirname "../key.pem"))
(def staticPath (.join path js/__dirname "../build"))
(def appPath (.join path js/__dirname "../build/app.html"))


(def c (chan))

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
    (println (.format m "YYYY-MM-DD")) (println (.format end "YYYY-MM-DD"))
    (aset options "startDate" (.format m "YYYY-MM-DD"))
    (aset options "endDate" (.format end "YYYY-MM-DD"))
    (aset options "filter" (setFilters groupId))
    (println options)
    options))

    ; put! c (aget res "rows" "length")
    ; let [n (aget res "query")] (println n)
(defn fetchData [options]
  (gaAnalytics options (fn [err, res]
    (if (true? err) (println "err" err)
      (put! c (aget res "totalResults"))))))

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

(defn serve [req res]
  (let [query (parseQuery (aget req "url"))]
  (println query )
    (if-let [groupId (aget query "group")]
      (let [
        period (setPeriod query)
        monthSeq (apply getMonthSeq period)
        optionsSeq (map #(setOptions % groupId) monthSeq)]
        (fetchData (nth optionsSeq 1))
        (.json res #js{:test groupId}))
      (.sendFile res appPath))))



(defn -main []
  (go (while true (let [res (<! c)] (println res))))

  (.use app (.static express staticPath))
  (.get app "/" serve))
  (.listen app 3000 (fn []
    (println "Server listening on port 3000")))

  (.listen server 8080)

  (.on io "connection" (fn [socket]
    (println "connection established")
    (.emit socket "data" "test")))


;
(set! *main-cli-fn* -main)
