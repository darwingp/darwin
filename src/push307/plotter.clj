(ns push307.plotter
  (:gen-class)
)
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
(def frame-width 1000)
(def frame-height 700)
(def window-buffer 10)
(def sub-window-width  (- (/ frame-width 2) (* 2 window-buffer)))
(def sub-window-height (- (/ frame-height 2) (* 2 window-buffer)))

;colors
(def window-color (Color. 66 66 66))
(def background-color  (Color. 188 188 188))
(def line-color (Color. 255 153 0))

;specific sub-window (0, 0) reference pts
(def w1-zero (list window-buffer (+ window-buffer sub-window-height)))
(def w2-zero (list (+ window-buffer sub-window-width) window-buffer))
(def w3-zero (list window-buffer (+ (* 2 window-buffer) (* 2 sub-window-height))))
(def w4-zero (list (+ (* 2 window-buffer) (* 2 sub-window-width)) (+ (* 2 window-buffer) (* 2 sub-window-height))))

;generational params
(def generations 100)
(def gen-increment (/ sub-window-width generations))

;set panel size
(.setPreferredSize panel (Dimension. frame-width frame-height))

;TESTING FUNC: generate random test-pts for display
(def test-pts
  (fn [length] 
    (loop [pts '() count length]
      (if (= count 0) pts
          (recur (cons (list count (rand-int 100)) pts) (- count 1))))

))

;TESTING MAP
(def stateExample {
    ;:points-fit '((0 10) (1 15) (2 30) (3 50) (4 53) (5 20) (6 60) (7 95)
     :points-fit (cons '(0 100) (test-pts generations))
              
                   ; generation, value (value out of 100)
})


(defn line-from-points
  "lambda used with update-points reduce to create lines between points"
  [gr]
  (fn [p1 p2]
    (.setColor gr line-color)
    (.drawLine gr
               (first p1)
               (second p1)
               (first p2)
               (second p2))
    p2 ;return for reduce
))

;method for updating graph from main
(defn add-pt
  "takes pt, previous pt and norm-function format: (prev-pt pt)"
  [pts norm]
  ((line-from-points (.getGraphics panel)) (map (normalize-to-graph norm) pts))
  ;returns pt for use in main (also for reduce when testing)
)
  
;TESTING FUNC
(defn update-points
  "reduce points to lines from given state"
  [state]
  (let [gr (.getGraphics panel)]
    (reduce (line-from-points gr)  (map (normalize-to-graph w1-zero) (state :points-fit)))
))


(defn normalize-to-graph
  "take point and map to graph based on zero pt"
  [zero-pt]
  (fn [input-pt] (list 
                    (+ (first zero-pt) (* gen-increment (first input-pt)))
                    (- (second zero-pt) (int (/ (* sub-window-height (second input-pt)) 100)))
                 )
))

(defn init-sub-window
  "create a new sub window in the jframe"
  [x y width height color]
    (let [gr (.getGraphics panel)]  ;get graphics object
      (.setColor gr color)
      (.fillRect gr x y width height)
))

(def add-sub
  "lambda to simplify generation of subwindows"
  (fn [x y] (init-sub-window x y sub-window-width sub-window-height window-color)))

(defn add-windows
  "create 4 subwindows based on buffer"
  []
                                        ;TODO: make this code not suck
  (add-sub window-buffer window-buffer)
  (add-sub (+ (/ frame-width 2) window-buffer) window-buffer)
  (add-sub window-buffer  (+ (/ frame-height 2) window-buffer))
  (add-sub (+ (/ frame-width 2) window-buffer)  (+ (/ frame-height 2) window-buffer))
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
  "test pts added"
  [& args]
  (init-window)       ;build up window
  (Thread/sleep 1000)  ;needs a slight delay
  (init-sub-window  0 0 frame-width frame-height background-color) ;add bg color
  (add-windows)  ;add sub-windows

  (update-points stateExample)   ;add test points
  "done"

)
