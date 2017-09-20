(defproject push307 "0.1.0"
  :description "PushGP, as implemented by Hamilton's CS 307 class."
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/test.generative "0.5.2"]]
  :main ^:skip-aot push307.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
