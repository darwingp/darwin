(ns push307.testmachine.machine
  (:require [push307.graphics.environment :refer :all])
(:gen-class))


;starting attributes
(def start-loc {:x 0 :y 0 :angle 0 :crash 0})           ;x y angle crash total
(def target-loc '(200 50))  ;location of target
(def vehicle-width 5)  ;not used as an exact radius
(def window-max-x 500) ;based on graphical window bounds
(def window-max-y 450)
(def draw-to-window? false)  ;plug graphical system into machine
(def vehicle-speed 5)  ;default tick speed

;Note: Obstacle list is formatted in the following way:
; {:x 0 :y 0 :width 5 :height 5}
;where x,y is top left

(defn intersects?
  "takes point and obstacle and checks for interesection"
  [x y obstacle]
  (let [
        obs-ulx (:x obstacle)
        obs-uly (:y obstacle)
        obs-lrx (+ obs-ulx (:width obstacle))
        obs-lry (+ obs-uly (:height obstacle))]
    ;detect collision with objects and bounds
    (or
    (or
       (or
           (> (+ x vehicle-width) window-max-x)
           (> (+ y vehicle-width) window-max-y))
       (or
           (< (- x vehicle-width) 0)
           (< (- y vehicle-width) 0)
        ))
    (and
       (and
           (> (+ x vehicle-width) obs-ulx)
           (> (+ y vehicle-width) obs-uly))
       (and
           (< (- x vehicle-width) obs-lrx)
           (< (- y vehicle-width) obs-lry))))))

(defn move-possible?
  "takes potential location and all obstacles and
  checks for intersections, returning true if the
  vehicle can move without a collision"
  [x y obs-lst]
  (loop [rem-obs obs-lst]
    (if (= rem-obs '()) true
        ;check if the vehicle intersects with the current object
        (if (intersects? x y (first rem-obs)) false
            (recur (rest rem-obs))))))

(def move
  "move based on angle and x,y and no collision present"
  (fn [location obs]
    (let [x (:x location)
          y (:y location)
          angle (Math/toRadians (:angle location))
          crashes (:crash location)
          new-x (+ x (* vehicle-speed (Math/cos angle)))
          new-y (+ y (* vehicle-speed (Math/sin angle)))
          ]
    (if (move-possible? new-x new-y obs)
      (let [new-state
      { :x new-x
        :y new-y
        :angle angle
        :crash crashes
       }]
        ;if graphical viewing enabled, draw to state first
        (if draw-to-window?
          (draw-vehicle new-state vehicle-width)  ;draw state (returns vehicle state)
          new-state))
      (assoc location :crash (+ crashes 1))))))

(def change-attrib
  ;change a state attribute
  (fn [loc-map attrib val]
    (assoc loc-map attrib val)))

(def distance
  "calculate distance between points"
  (fn [x1 y1 x2 y2]
    (let [xdif (- x2 x1) ydif (- y2 y1)]
    (Math/sqrt (+ (* xdif xdif) (* ydif ydif))))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes: current-loc (x y angle speed crash) and instruction
  (fn [loc instr]
    (cond
      (= (first instr) "angle") (move (change-attrib loc :angle (second instr)) obstacles)
      :else (move loc obstacles)
    )))

(defn test-instructions-list
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (if draw-to-window?
    (let [final-loc (reduce (new-move obstaclelist) start-loc instructionlist)]
     {:dist-to-target (distance (:x final-loc) (:y final-loc) (first target-loc) (second target-loc))
      :end-loc final-loc
      :num-crash (:crash final-loc)
      :instr-total (count instructionlist)}))

;testing
(def test-objects
  '({:x 10 :y 10 :width 5 :height 5}
    {:x 50 :y 50 :width 10 :height 15}
    {:x 10 :y 5 :width 20 :height 30}))

;file for testing system
(def testfile "pathfiles/testpath.txt")

(def gen-instruction
  ;create an instruction based on string input from file
  (fn [lst] (list
             (first lst)
             ;if arg is no change to heading (-)
             (if (= "-" (first lst)) "-"
             (Integer. (second lst))))))

(defn test-instructions-file
  "loads instructions from file and executes list function"
  [location obs-list]
  ;this is a file wrapper for test-instructions-list
  (test-instructions-list
  (map gen-instruction
    ;split line by space
    (map (fn [line] (clojure.string/split line #" "))
    (clojure.string/split-lines (slurp location)))) obs-list))
