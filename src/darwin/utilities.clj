(ns darwin.utilities
  (:gen-class))

(defn print-many-ln
  "Prints args to stdout in a coder-friendly way."
  [& args]
  (println (apply str (map print-str args))))

(defn prepeatedly
  "Like repeatedly, but parallel."
  [n fn]
  (apply pcalls (repeat n fn)))

(defmacro coalesce
  ([] nil)
  ([x] x)
  ([x & next]
     `(let [v# ~x]
         (if (not (nil? v#)) v# (coalesce ~@next)))))

(defn abs
  [n]
  (if (< n 0) (* -1 n) n))

(defn find-list
  "Finds the first element in l for which p is true"
  [p l]
  (first (drop-while #(not (p %)) l)))

(defn true-percent?
  "Returns true n percent of the time."
  [n]
  (<= (inc (rand-int 100)) n))

(defn flatten-maplist
  "Takes a list of maps, and returns a map where each key appears in maplist
   and the value is a list of all values for that key in maplist."
  [maplist]
  (loop [ms maplist
         res {}]
    (if
      (empty? ms)
      res
      (recur
       (rest ms)
       (reduce-kv
         #(assoc %1 %2
            (concat
              (get %1 %2 '())
              (list %3)))
         res
         (first ms))))))
