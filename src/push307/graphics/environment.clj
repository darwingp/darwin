(ns push307.graphics.environment
  (:gen-class))

(import '(java.awt Color Dimension Graphics Point))
(import '(javax.swing JPanel JFrame JLabel))

;graphical setup
(def frame-width 900)
(def frame-height 700)
(def window-buffer 10)

;Java window components
(def frame (JFrame. "GP Plotter"))
(def panel (JPanel.))
(.setPreferredSize panel (Dimension. frame-width frame-height))

(def env-color (Color. 168 155 145))
(def window-color  (Color. 65 57 51))

(def obstacle-color (Color. 220 204 189))
(def vehicle-color (Color. 250 192 35))

(defn draw-vehicle
  "takes machine state, draws vehicle, returns state"
  [vehicle-state]
  vehicle-state
  )

(defn init-sub-window
  "create a new sub window in the jframe"
  [x y width height color]
    (let [gr (.getGraphics panel)]  ;get graphics object
      (.setColor gr color)
      (.fillRect gr x y width height)))

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

(defn start-environment
  "initialize plotter window and build graphical elements"
  []
  (init-window)       ;build up window
  (Thread/sleep 1000)  ;needs a slight delay (can dial this back for optimization)
  (init-sub-window  0 0 frame-width frame-height env-color) ;add bg color
  "Environment initialized..."
)
