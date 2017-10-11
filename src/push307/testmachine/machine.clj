(ns push307.testmachine.machine (:gen-class))

(def standard-start '(0 0))

(def move-dxdy (fn [x y dx dy] '((+ x dx) (+ y dy))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes pair: current-loc and instruction
  (fn [loc instr]
    ;return new loc based on instruction
    ))

(defn test-instructions
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (let [result {:dist-to-target 500 :num-crash 0 :instr-rem (count instructionlist)}]
    (reduce (new-move obstaclelist) standard-start instructionlist)))
