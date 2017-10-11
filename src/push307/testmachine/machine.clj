(ns push307.testmachine.machine (:gen-class))

(def start-loc '(0 0 0))   ;x y angle
(def target-loc '(200 50))
(def move-length 1)

(def move
  "move based on angle and x,y"
  (fn [location]
    (let [x (first location) y (second location) angle (nth location 2)]
      (list
        ;Note: currently expects radians
        (+ x (* move-length (Math/cos angle)))
        (+ y (* move-length (Math/sin angle)))
        ))))

(defn change-angle
  "takes instruction, if angle-based, modifies angle val"
  [loc instr]
  (if (= (first instr) :angle)
    (concat (take 2 loc) [(second instr)])
    loc
))

(def distance
  "calculate distance between points"
  (fn [x1 y1 x2 y2]
    (let [xdif (- x2 x1) ydif (- y2 y1)]
    (Math/sqrt (+ (* xdif xdif) (* ydif ydif)))
)))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes pair: current-loc (x y angle) and instruction
  (fn [loc instr]
    ;return new loc based on instruction
    (move (change-angle loc instr))
    ))

(defn test-instructions
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (let [final-loc (reduce (new-move obstaclelist) start-loc instructionlist)]
    {:dist-to-target (distance (first final-loc) (second final-loc) (first target-loc) (second target-loc))
     :num-crash 0
     :instr-rem (count instructionlist)}))
