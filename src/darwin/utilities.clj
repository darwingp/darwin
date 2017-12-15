(ns darwin.utilities
  (:gen-class))

;; TODO: Move general utilities here from 
; darwin.push.utilities
; darwin.gp.utilities
; other modules
;
;; There might be some duplication

(defmacro coalesce
  ([] nil)
  ([x] x)
  ([x & next]
     `(let [v# ~x]
         (if (not (nil? v#)) v# (coalesce ~@next)))))
