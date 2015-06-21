(defproject loom "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [im.chit/purnam "0.5.2"]
                 [cljs-ajax "0.3.13"]
                 [cljsjs/react "0.13.3-0"]
                 [reagent "0.5.0"]]

  :node-dependencies [[source-map-support "0.2.8"]
                      [ga-analytics "0.0.7"]
                      [express "4.8.0"]
                      [moment ""]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-npm "0.5.0"]]

  :source-paths ["src-server" "src-client"]

  :cljsbuild {
    :builds [{:id "loom"
              :source-paths ["src-server"]
              ; :notify-command ["node" "run.js"] ;; << ADD THIS
              :compiler {
                :output-to "out/loom.js"
                :output-dir "out"
                :target :nodejs
                :optimizations :none
                :source-map true}}
            { :id "client"
              :source-paths ["src-client"]
              :compiler {
                :closure-warnings {:non-standard-jsdoc :off}
                :foreign-libs []
                :verbose true
                :output-to "out/build/app.js"
                :pretty-print true
                :optimizations :simple
                }}]})
