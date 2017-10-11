(ns push307.testmachine.machine (:gen-class))

(def start-loc '(0 0 0))   ;x y angle
(def target-loc '(200 50))
(def move-length 1)
(def vehicle-width 5)  ;not used as an exact radius

;Note: Obstacle list is formatted in the following way:
;(x y width length)
;where x,y is top left

(defn intersects? 
  "takes point and obstacle and checks for interesection"
  [x y obstacle]
  (let [
        width (nth obstacle 2)
        height (nth obstacle 3)
        obs-ulx (first obstacle)
        obs-uly (second obstacle)
        obs-lrx (+ obs-ulx width)
        obs-lry (+ obs-uly height)]
    (and
       (and 
           (> (+ x vehicle-width) obs-ulx) 
           (> (+ y vehicle-width) obs-uly))
       (and
           (< (- x vehicle-width) obs-lrx)
           (< (- y vehicle-width) obs-lry))
)))

(defn move-possible? 
  "takes potential location and all obstacles and
  checks for intersections"
  [x y obs-lst]
  (loop [rem-obs obs-lst]
    (if (= rem-obs '()) true
        (if (intersects? x y (first rem-obs)) false 
            (recur (rest rem-obs))))))

(def move
  "move based on angle and x,y"
  (fn [location obs]
    (let [x (first location) 
          y (second location) 
          angle (Math/toRadians (nth location 2))
          new-x (+ x (* move-length (Math/cos angle)))
          new-y (+ y (* move-length (Math/sin angle)))
          ]
    (if (move-possible? new-x new-y obs)
      (list
        new-x
        new-y
        angle)
      location ))))

(defn change-angle
  "takes instruction, if angle-based, modifies angle val"
  [loc instr]
    (concat (take 2 loc) [(second instr)]))

(def distance
  "calculate distance between points"
  (fn [x1 y1 x2 y2]
    (let [xdif (- x2 x1) ydif (- y2 y1)]
    (Math/sqrt (+ (* xdif xdif) (* ydif ydif))))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes pair: current-loc (x y angle) and instruction
  (fn [loc instr]
    (cond
      (= (first instr) "angle") (move (change-angle loc instr) obstacles)
      :else (move loc obstacles)
    )))

(defn test-instructions
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (let [final-loc (reduce (new-move obstaclelist) start-loc instructionlist)]
      (take 2 final-loc)
    ; {:dist-to-target (distance (first final-loc) (second final-loc) (first target-loc) (second target-loc))
    ;  :num-crash 0
    ;  :instr-rem (count instructionlist)}
))
