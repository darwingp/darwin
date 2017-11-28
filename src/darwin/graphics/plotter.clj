(ns darwin.graphics.plotter
  (:gen-class))

; -----------------------------------
; GP Plotting system using Java Swing
; -- Creates 4 windows to graph to --
; -----------------------------------

;Java import
(import '(java.awt Color Dimension Graphics Point))
(import '(javax.swing JPanel JFrame JLabel))

;Java window components
(def frame (JFrame. "GP Plotter"))
(def panel (JPanel.))

;graphical setup
(def frame-width 750)
(def frame-height 500)
;(def frame-width 1000)
;(def frame-height 700)
(def window-buffer 10)
(def horiz-lines 10)
(def sub-window-height (- frame-height (* 2 window-buffer)))
(def sub-window-width (- frame-width (* 2 window-buffer)))
(def line-rule-increment (int (/ sub-window-height horiz-lines)))

;base legend size on window (this will resize interior components
(def legend-width (int (/ sub-window-width 6)))
(def legend-height (int (/ sub-window-height 5)))

;colors
(def window-color (Color. 10 53 63))
(def background-color  (Color. 69 106 114))
(def horiz-rule-color (Color. 57 88 94))
(def line-color-1 (Color. 255 112 13))
(def line-color-2 (Color. 255 170 0))
(def line-color-3 (Color. 243 206 7))
(def line-color-4 (Color. 232 62 0))
(def all-line-colors (list line-color-1 line-color-2 line-color-3 line-color-4))

;specific sub-window (0, 0) reference pts
(def w-zero (list window-buffer (+ window-buffer sub-window-height)))

;generational params
(def generations 500)
(def gen-increment (/ sub-window-width generations))
(def data-fields (list "Fitness" "Behavior diversity" "Average error" "Best prog size"))

;note: use requires deref
(def max-vals (atom {}))
(swap! max-vals assoc :points-fit 700)
(swap! max-vals assoc :points-behavior 700)
(swap! max-vals assoc :average-fitness 700)
(swap! max-vals assoc :best-size 700)

;note: using this map requires deref
(def previous-values (atom {}))
;add values to prev
(swap! previous-values assoc :generation 0)
(swap! previous-values assoc :points-fit 700)
(swap! previous-values assoc :points-behavior 700)
(swap! previous-values assoc :average-fitness 700)  ;current total
(swap! previous-values assoc :best-size 700)

;set panel size
(.setPreferredSize panel (Dimension. frame-width frame-height))

(defn line-from-points
  "lambda used with update-points reduce to create lines between points"
  [gr color]
  (fn [p1 p2]
    (.setColor gr color)
    (.drawLine gr
       (first p1)
       (second p1)
       (first p2)
       (second p2))
    p2 ;return for reduce
))

(defn normalize-to-graph
  "take point and map to graph based on zero pt"
  [zero-pt max-val]
  (fn [input-pt]
    (list
       (+' (first zero-pt) (*' gen-increment (first input-pt)))
       (-' (second zero-pt) (bigint (/ (*' sub-window-height (second input-pt)) max-val)))
    )
))

;method for updating graph from main
;example use: (add-pt current-state :points-fit line-color1)

;; state looks like this:
;; { :points-fit 3
;;   :points-behavior 4
;;   :average-fitness 5
;;   :lowest-size  3
;;   :generation 3)

(defn add-pt
  "takes pt, previous pt and norm-function format: (prev-pt pt)"
  [state data-type color]
  ;set current max (maybe working?)
  (if (= (:generation state) 0) (do
                                  (swap! max-vals assoc data-type (data-type state))
                                  (swap! previous-values assoc data-type (data-type state)) ) "not gen 1")
    (let [gen (:generation state)
          current-pt (
                      (normalize-to-graph w-zero (data-type (deref max-vals)))
                      (if (= data-type :average-fitness)
                        (list gen
                          (/ (+ (data-type state) (data-type (deref previous-values))) (+ gen 2)))
                        (list gen (data-type state))))
          previous-pt (
                       (normalize-to-graph w-zero (data-type (deref max-vals)))
                       (if (= data-type :average-fitness)
                        (list (:generation (deref previous-values))
                        (/ (data-type (deref previous-values)) (+ gen 1)))
                        (list (:generation (deref previous-values)) (data-type (deref previous-values)))))]

        ((line-from-points (.getGraphics panel) color) current-pt previous-pt)
        ;set previous values
        (if (= data-type :average-fitness)
          (swap! previous-values assoc data-type (+ (data-type state) (data-type (deref previous-values))))
          (swap! previous-values assoc data-type (data-type state)))
        (swap! previous-values assoc :generation gen)
        ))

(defn init-sub-window
  "create a new sub window in the jframe"
  [x y width height color]
    (let [gr (.getGraphics panel)]  ;get graphics object
      (.setColor gr color)
      (.fillRect gr x y width height)))

(def add-sub
  "lambda to simplify generation of subwindows"
  (fn [x y] (init-sub-window x y sub-window-width sub-window-height window-color)))


(defn sub-increment
  "modify in x,y to draw horizontal lines"
  [pt]
  (list (first pt) (- (second pt) line-rule-increment )))

(defn add-windows-lines
  "create 1 subwindow based on buffer and horizontal reference lines"
  []
  ;add sub-window
  (add-sub window-buffer window-buffer)

  ;add horizontal lines to window
  (loop [remaining horiz-lines
         loc w-zero
         endloc (list (+ window-buffer sub-window-width) (+ sub-window-height window-buffer))]
    (if (= remaining 0) "added horizontal rules"
        (do
          ((line-from-points (.getGraphics panel) horiz-rule-color) loc endloc)
            (recur (- remaining 1) (sub-increment loc) (sub-increment endloc))))
))

(defn add-label
  "add a label and line"
  [text color x y]
  (let [gr (.getGraphics panel) line-length 10]
    (.setColor gr color)
    (.drawString gr text (+ x line-length 5) y)
    (.drawLine gr x (- y 4) (+ x line-length) (- y 4))
  )
)

(defn add-legend
 "add color-coded legend"
 [num-lines labels color-list]
 (let [
       upper-left-x (- frame-width (* 2 window-buffer) legend-width)
       upper-left-y (* 2 window-buffer)
       gr  (.getGraphics panel)
       line-increment (int (/ legend-height (+ num-lines 1)))
       ]
   ;add window
   (.setColor gr background-color)
   (.fillRect gr upper-left-x upper-left-y legend-width legend-height)

   ;add labels
   (loop [remaining-labels num-lines
          x (+ upper-left-x 5)
          y (+ upper-left-y 10 line-increment)
          colors color-list
          text labels
          ]
     (if (= remaining-labels 0) "added legend labels"
         (do
           (add-label (first text)  (first colors) x y)
           (recur (- remaining-labels 1) x (+ y line-increment) (rest colors) (rest text))
          )
      ))
   )
)

(defn init-window
  "get jpanel in jframe, setup prefs"
  []
  (doto frame
    (.setSize frame-width frame-height)   ;set frame size from preset
    (.setResizable false)
    (.add panel)
    (.pack)
    ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE) ; (repl problem)
    (.setVisible true)
  )
)

(defn start-plotter
  "initialize plotter window and build graphical elements"
  []
  (init-window)       ;build up window
  (Thread/sleep 1000)  ;needs a slight delay (can dial this back for optimization)
  (init-sub-window  0 0 frame-width frame-height background-color) ;add bg color
  (add-windows-lines)  ;add sub-windows
  (add-legend (count all-line-colors) data-fields all-line-colors)
  "UI initialized...")
