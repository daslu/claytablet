(defproject clojure-ide "0.1.0-SNAPSHOT"
  :description "A simple Clojure-based web IDE"
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [ring "1.9.6"]
                 [compojure "1.6.2"]
                 [ring/ring-defaults "0.3.3"]
                 [cheshire "5.11.0"]
                 [nrepl "0.8.3"]
                 [cider/piggieback "0.5.3"]]
  :plugins [[lein-ring "0.12.6"]]
  :ring {:handler clojure-ide.core/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring/ring-mock "0.4.0"]]}}
  :main clojure-ide.core)
