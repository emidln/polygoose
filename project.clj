(defproject polygoose "0.1.0-SNAPSHOT"
  :description "Playing with cljs stuff"
  :url "http://example.com/FIXME"

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/clojurescript "0.0-2138"]
                 [org.clojure/core.async "0.1.267.0-0d7780-alpha"]
                 [cloact "0.1.0"]
                 [prismatic/dommy "0.1.2"]
                 [garden "1.1.4"]
                 [cljs-http "0.1.3-SNAPSHOT"]
                 ]

  :plugins [[lein-cljsbuild "1.0.1"]]

  :source-paths ["src"]
  :repl-options {:timeout 240000}

  :cljsbuild {
    :builds [{:id "polygoose"
              :source-paths ["src"]
              :compiler {
                :output-to "polygoose.js"
                :output-dir "out"
                :optimizations :none
                :source-map true}}]})
