(ns push307.pushgp.mutation
  (:gen-class))

(def event-percentage 5)
(def instructions '(:a :b :c :d)) ; placeholder

(defn keep?
  "returns false n% of time"
  [x]  ;used as filter, ignore input
  ;arbitrary value in range 0-100 (exclusive) has n% chance of returning false...
  (<= event-percentage (rand-int 100)))

(defn add?
 "returns true n% of time"
  []
  (>= event-percentage (rand-int 100)))

(defn add-to-end
  "takes in a program and instructions and has 5% chance of adding a rand instruction to end
  (which will actually be the beginning because of the recursive method"
  [instructions prog]
  (if (not keep?)
    (cons (rand-instruction instructions) prog)
    prog
  ))

(defn rand-instruction
  "returns a random instruction"
  [instructions]
  (nth instructions (rand-int (count instructions)))) ;use nth with the length of the given instruction list

;TODO: addition should be refactored
(defn uniform-addition
  "Randomly adds new instructions before every instruction (and at the end of
  the program) with some probability. Returns child program."
  [program]
  ;add to list based on probablistic predicate
  (add-to-end instructions      ;add element (n% chance) to end (beginning of list with recursion putting elements behind
  (loop [final [] orig program]    ;tail recursion on two lists (original and result
    (if (empty? orig) final         ;return new list
        (if (add?) 
                      ;will return true n% of the time
          (recur
           (conj
            (conj final (first orig))  ;add first element of original to final list
            (rand-instruction instructions) ;add a random instruction as well (to front once reversed)
           )
           (rest orig)                 ;second list in recursive call
           )
          (recur
            (conj final (first orig))  ;if not adding instruction, just transfer old head to new list
            (rest orig)
          )
        ))))
  )

(defn uniform-deletion
  "Randomly deletes instructions from program at some rate. Returns child program."
  [prog]
  (filter keep? prog)
  )
