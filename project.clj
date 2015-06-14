(defproject loom "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2311"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [kioo "0.4.0"]
                 [om "0.7.1"]]

  :node-dependencies [[source-map-support "0.2.8"]
                      [ga-analytics "0.0.7"]
                      [express "4.0.0"]
                      [moment ""]]

  :plugins [[lein-cljsbuild "1.0.3"]
            [lein-npm "0.5.0"]]

  :source-paths ["src-server" "src-client"]

  :resource-paths ["resources"]

  :cljsbuild {
    :builds [{:id "loom"
              :source-paths ["src-server"]
              :notify-command ["node" "run.js"] ;; << ADD THIS
              :compiler {
                :output-to "out/loom.js"
                :output-dir "out"
                :target :nodejs
                :optimizations :none
                :source-map true}}
            { :id "client"
              :source-paths ["src-client"]
              :compiler {
                :verbose true
                :output-to "out/build/app.js"
                :pretty-print true
                :optimizations :simple
                :preamble ["react/react.js"]
                :externs ["react/externs/react.js"]
                }}]})
