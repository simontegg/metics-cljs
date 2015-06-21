(ns loom.db
  (:require [cljs.nodejs :as node]
            [cljs.reader :as reader]
            [cljs.core.async :refer [put! chan <! >! timeout pub sub pipe]])
  (:require-macros  [cljs.core.async.macros :as am :refer [go go-loop]]
                    [purnam.core :refer [obj arr ? ! ?> !>]]))

(def REDIS_PASSWORD (aget js/process "env" "REDIS_PASSWORD"))
(def redis (node/require "redis"))
(def r (node/require "rethinkdb"))


(defn getClient []
  (let [
    client (.createClient
      redis
      10232
      "pub-redis-10232.us-east-1-4.ec2.garantiadata"
      #js{:no_ready_check true :auth_pass REDIS_PASSWORD}])))


      ; (defn connect []
      ;   (let [ch (chan)]
      ;   (.connect r #js{:host "localhost" :port 28015} (fn [err conn]
      ;     (if (true? err) (println err))
      ;     (-> (.db r "loom")
      ;       (.tableCreate ""))
      ;
      ;
      ;
      ;     (put! ch conn)))
      ;     ch))
