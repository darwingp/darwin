(ns push307.plotter
  (:gen-class)
)
(import '(java.awt Color Dimension Graphics Point))
(import '(javax.swing JPanel JFrame JLabel))

(def frame (JFrame. "GP Plotter"))
(def panel (JPanel.))

;graphical setup
(def frame-width 1000)
(def frame-height 750)
(def window-buffer 10)
(def sub-window-width  (- (/ frame-width 2) (* 2 window-buffer)))
(def sub-window-height (- (/ frame-height 2) (* 2 window-buffer)))
(def window-color (Color. 66 66 66))
(def background-color  (Color. 188 188 188))
(def line-color (Color. 255 153 0))

;statistical params
(def generations 100)
(def gen-increment (/ sub-window-width generations))

(.setPreferredSize panel (Dimension. frame-width frame-height))


(def stateExample {
    :points '((1 15) (2 30) (3 50) (4 53) (5 20) (6 170) (7 200) (8 111)
              (9 150) (10 200) (11 250) (12 234) (13 275) (14 300) (15 295) (16 320)
              (17 330) (18 327) (19 335) (20 322) (21 332) (22 317) (23 325) (24 326)
              (25 328) (26 334) (27 350) (28 356) (29 354) (30 357) (31 359) (32 360)
              (33 352) (34 350) (35 358) (36 356) (37 360) (38 362) (39 365) (40 360)
              )
                   ; generation, value
})


(defn line-from-points
  "lambda used with update-points reduce to create lines between points"
  [gr]
  (fn [p1 p2]
    (.setColor gr line-color)
    (.drawLine gr
               (* (first p1) gen-increment )
               (second p1)
               (* (first p2) gen-increment)
               (second p2))
    p2 ;return for reduce
))



(defn update-points
  "reduce points to lines from given state"
  [state]
  (let [gr (.getGraphics panel)]
    (reduce (line-from-points gr)  (state :points))
))

(defn update-graph1
  [pt] ;(gen pt)

)

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

(defn init-plotter
  "get jpanel in jframe, setup prefs"
  []
  (doto frame
    (.setSize frame-width frame-height)   ;set frame size from preset
    (.add panel)
    (.pack)
    ;(.setDefaultCloseOperation JFrame/EXIT_ON_CLOSE) ; (repl problem)
    (.setVisible true)
  )
)


(defn start-plotter
  "test pts added"
  [& args]
  (init-plotter)       ;build up window
  (Thread/sleep 1000)  ;needs a slight delay
  (init-sub-window  0 0 frame-width frame-height background-color) ;add bg color
  (add-windows)  ;add sub-windows

  (update-points stateExample)   ;add test points

)
