(ns push307.testmachine.machine (:gen-class))

(def move-dxdy (fn [x y dx dy] '((+ x dx) (+ y dy))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles vehicle move angle]
  vehicle
)

(defn test-instructions
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (loop [vehicle '(0 0 0)  ;x y angle
           rem-instr instructionlist
           result-state {:dist-to-target 500 :num-crash 0 :instr-rem (count instructionlist)}]
      (if (= rem-instr '()) result-state
        (recur
            (new-move obstaclelist vehicle (first rem-instr))
            (rest rem-instr)
            result-state))))
