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
(def moment (node/require "moment"))


(defn parseQuery [uri]
  (let [urlObj (.parse url uri true)]
    (aget urlObj "query")))


(def keyPath (.join path js/__dirname "/key.pem"))
(def staticPath (.join path js/__dirname "../build"))
(println staticPath)
(defn setMonth [year month]
  (moment (str year "-" month) "YYYY-MM"))

(defn parseMonthString [string]
  (let [m (moment string "YYYY-MM")]
    [(.year m) (+ (.month m) 1)]))

(defn setFilters [groupId]
  (str "ga:customVarValue4==" groupId ";ga:customVarValue1!=undefined"))

(def options #js {
  :clientId "1010918958229-d8r7khmhlne80hbtjmrpivjkjnesou2u.apps.googleusercontent.com"
  :serviceEmail "1010918958229-d8r7khmhlne80hbtjmrpivjkjnesou2u@developer.gserviceaccount.com"
  :key keyPath
  :metrics "ga:users"
  :ids "ga:58892342"
  :dimensions "ga:customVarValue1,ga:year,ga:month"
  })

(defn setOptions [options year month groupId]
  (let [m (moment (str year "-" month) "YYYY-MM") end (.endOf (moment m) "month")]
    (aset options "start-date" (.format m "YYYY-MM-DD"))
    (aset options "end-date" (.format end "YYYY-MM-DD"))
    (aset options "filters" (setFilters groupId))
    options))

(defn fetchData []
  (print keyPath)
  (print (setOptions options 2015 1 4))
  ; (gaAnalytics options (fn [err, res] (print err res)))

)

(defn getMonthAgoString [monthAgo]
  (.format (.subtract (moment) monthAgo "months") "YYYY-MM"))

(defn getMonthAhead [])

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

(defn serveQuery [req res]
  (let [query (parseQuery (aget req "url")) period (setPeriod query)]
    (println (apply getMonthSeq period))
    (.send res "build/index.html")))

(def app (express))

(defn -main []
  (.use app (.static express staticPath))
  ; (println (.get app))
  (.get app "/" serveQuery))
  (.listen app 3000 (fn []
    (println "Server litsning on port 3000")))
;
(set! *main-cli-fn* -main)
