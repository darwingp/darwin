(defproject darwin "0.1.0"
  :description "GP system developed for Hamilton College's CS 307 class."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.generators "0.1.2"]]
  :main ^:skip-aot darwin.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
