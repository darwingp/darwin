(ns darwin.problems.pathfindingtests.machine
  (:require [darwin.graphics.environment :as display])
(:gen-class))

;starting attributes
(def start-loc {:x 10 :y 10 :angle 45 :crash 0 :color 0 :moves-made 0 :speed 20 :path '()})           ;x y angle crash total
(def target-loc '(750 600))  ;location of target
(def max-speed 20)
(def vehicle-width 2)  ;not used as an exact radius
(def window-max-x 900) ;based on graphical window bounds
(def window-max-y 700)
(def draw-to-window? (atom false))  ;plug graphical system into machine
(def field-of-view (Math/toRadians 15)) ;angle change for checking for obstacles

;Note: Obstacle list is formatted in the following way:
; {:x 0 :y 0 :width 5 :height 5}
;where x,y is top left

(defn intersects?
  "takes point and obstacle and checks for interesection"
  [x y obstacle]
  (let [obs-ulx (:x obstacle)
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
           (< (- y vehicle-width) 0)))
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
          current-path (:path location)
          angle (Math/toRadians (:angle location))
          crashes (:crash location)
          color (:color location)
          speed (:speed location)
          addmove (+ (:moves-made location) 1)
          new-x (+ x (* speed (Math/cos angle)))
          new-y (+ y (* speed (Math/sin angle)))]
    (if (move-possible? new-x new-y obs)
      (let [new-state
      { :x new-x
        :y new-y
        :angle angle
        :crash crashes
        :color color
        :moves-made addmove
        :speed speed
        :path (cons (list new-x new-y) current-path)
       }]
        ;if graphical viewing enabled, draw to state first
        (if (deref draw-to-window?)
          (display/draw-vehicle new-state x y vehicle-width)  ;draw state (returns vehicle state)
          new-state))
      (assoc location :crash (+ crashes 1))))))

(def change-attrib
  ;change a state attribute
  (fn [loc-map attrib val]
    (assoc loc-map attrib (if (and (= attrib :speed) (> val max-speed)) max-speed val))))

(def distance
  "calculate distance between points"
  (fn [x1 y1 x2 y2]
    (let [xdif (- x2 x1) ydif (- y2 y1)]
    (Math/sqrt (+ (* xdif xdif) (* ydif ydif))))))

(defn no-obstacles-in-range
  "checks if an obstacle is in a specific range from vehicle location"
  [loc range obs] ;px py range
  (let [x (:x loc)
        y (:y loc)
        speed (:speed loc)
        range2 (/ (max speed range) 2)]
    ;check if the range to an obstacle is less than provided range
    (and (move-possible? (+ x range2) (- y range2) obs) (move-possible? (+ x range2) (+ y range2) obs)
         (move-possible? (- x range2) (+ y range2) obs) (move-possible? (- x range2) (- y range2) obs))))

(defn get-angle-to-target
  "finds the angle between the vehicle location and the target"
  [x y]
  (let [dx (- (first target-loc) x)
        dy (- (second target-loc) y)]
    (Math/toDegrees (Math/atan (/ dx dy)))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes: current-loc (x y angle speed crash) and instruction
  (fn [loc instr]
    ;(println "Eval: " instr)
    (cond (= (first instr) "angle") (move (change-attrib loc :angle (second instr)) obstacles)
          (= (first instr) "set-angle-target")
            (move (change-attrib loc :angle (get-angle-to-target (:x loc) (:y loc))) obstacles)
          (= (first instr) "set-speed")
            (move (change-attrib loc :speed (second instr)) obstacles)
          (=  (first instr) "loop")
              (reduce (new-move obstacles) loc
                      (take (* (second instr) (count (nth instr 2)))
                      (cycle (nth instr 2))))
          (= (first instr) "move-while")
              ;create lazy-seq of provided moves, reduce until obstacle in range, return reduced loc
              (reduce (fn [loc instruction]
                    ;check if the next move will result in a crash
                    (if (= (- (:crash loc) (:crash ((new-move obstacles) loc instruction))) 0)
                      ((new-move obstacles) loc instruction) (reduced loc)))
                    loc (cycle (nth instr 2)))
          (= (first instr) "if-obs-range")
              (if (no-obstacles-in-range loc (second instr) obstacles)
                (reduce (new-move obstacles) loc (nth instr 2)) loc))))

(defn write-instructions-to-file
  [instr-list filename]
  ;file needs to exist
  (spit filename
  (reduce (fn [total instr] (str total "\n" instr)) instr-list )))

;MAIN TESTING ENTRY PT.
; ------- Takes list of lists of moves for all indivs in gen and obs list -------

(defn prep-instructions
  "take instruction, prep for eval"
  [instr]
  (let [parse-angle (fn [lst] (list (first lst) (Integer. (second lst))))
        parse-cond (fn [lst] (list (first lst) (Integer. (second lst))))]
  (cond (= (first instr) "angle")
           (cons (parse-angle instr) (prep-instructions (rest (rest instr))))
        (= (first instr) "set-angle-target")
           (cons '("set-angle-target") (prep-instructions (rest instr)))
        (= (first instr) "set-speed")
           (cons (parse-angle instr) (prep-instructions (rest (rest instr))))
        (or
         (= (first instr) "loop")
         (= (first instr) "move-while")
         (= (first instr) "if-obs-range"))
           (list (concat (parse-cond instr) (list (prep-instructions (rest (rest instr)))))))))

(defn test-instructions-list
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist testcriteria]
    (let [prepped-instr (map first
                             (map prep-instructions
                                  (map (fn [line] (clojure.string/split line #" ")) instructionlist)))
          obs (if (deref draw-to-window?) (display/draw-obstacles obstaclelist) obstaclelist)
          ;draw-target (display/draw-pt (first target-loc) (second target-loc))
          final-loc (reduce (new-move obs) start-loc prepped-instr)
          dist (distance (:x final-loc) (:y final-loc) (first target-loc) (second target-loc))]
      ;(println (:x final-loc) " " (:y final-loc))
     {:dist-to-target dist
      :path (:path final-loc) ;original instruction list size
      :num-crash (:crash final-loc)
      :instr-total (:moves-made final-loc)
      :fitness (bigint (+' (*' (:distance-from-target testcriteria) dist)
                     (*' (:total-crashes testcriteria) (:crash final-loc))
                     (*' (:moves-made testcriteria) (count instructionlist))))}))

;file for testing system
(def testfile "data/pathfiles/solution.txt")
(def testobsfile "data/obsfiles/easytest.txt")
(def examplecriteria {:distance-from-target 1
   :total-crashes 1
   :moves-made 0.2})

;FILE OPERATIONS
;---------------

(defn load-instruction-list
  "load instructions from file"
  [location-file]
  (clojure.string/split-lines (slurp location-file)))

(defn load-obstacle-list
  "load obstacles from file"
  [obs-file]
  (map #(read-string %) (clojure.string/split-lines (slurp obs-file))))

(defn test-instructions-file
  "loads instructions from file and obstacles from a file and executes list function"
  [location-file obs-file testcriteria]
  ;this is a file wrapper for test-instructions-list
  (if (deref draw-to-window?) (display/start-environment))
  (test-instructions-list ;call list-based with parsed instruction file and parsed obs file
   (load-instruction-list location-file) (load-obstacle-list obs-file) testcriteria))

(defn display-obstacle-map
  "display the obstacle map"
  [locationfile]
    (display/start-environment)
    (display/draw-obstacles (load-obstacle-list locationfile)))

(defn final-display
  [instrs test-loc]
  (do
    (reset! draw-to-window? true)
    (display/start-environment)
    (test-instructions-list instrs (load-obstacle-list test-loc) examplecriteria)))

;POPULATION DIVERSITY
;--------------------
(defn calculate-behavior-div
  "this function takes in all the lists of instructions
  for a generation and determines a behavioral diversity value"
  [generation-instructions diversity-frame]
  ;note: generation-instructions should be a list of
  ;instruction lists for all individuals in generation
  ;note: this is going to be inefficient
  (let [all-frames
        ;mapcat to flatten results by 1 level (partition is list of lists)
        (mapcat
         ;get all partitions for each individual (step 1 for ALL)
         (fn [ind] (partition diversity-frame 1 (:error ind)))
         generation-instructions)]
    ;check total against distinct
    (- (count all-frames) (count (distinct all-frames)))))
