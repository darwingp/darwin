(ns darwin.core
  (:require [darwin.gp :refer :all])
  (:require [darwin.problems.symbolicregression :as symreg])
  (:gen-class))

(defn -main
  "Runs push-gp, giving it a map of arguments."
  [& args]
  (run-gp symreg/configuration))
