(ns darwin.problems.pathfindingtests.machine
  (:require [darwin.graphics.environment :refer :all])
(:gen-class))

;starting attributes
(def start-loc {:x 10 :y 10 :angle 45 :crash 0 :color 0})           ;x y angle crash total
(def target-loc '(200 50))  ;location of target
(def vehicle-width 2)  ;not used as an exact radius
(def window-max-x 900) ;based on graphical window bounds
(def window-max-y 700)
(def draw-to-window? true)  ;plug graphical system into machine
(def vehicle-speed 15)  ;default tick speed
(def field-of-view 10) ;angle change for checking for obstacles

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
          color (:color location)
          new-x (+ x (* vehicle-speed (Math/cos angle)))
          new-y (+ y (* vehicle-speed (Math/sin angle)))
          ]
    (if (move-possible? new-x new-y obs)
      (let [new-state
      { :x new-x
        :y new-y
        :angle angle
        :crash crashes
        :color color
       }]
        ;if graphical viewing enabled, draw to state first
        (if draw-to-window?
          (draw-vehicle new-state x y vehicle-width)  ;draw state (returns vehicle state)
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

(defn no-obstacles-in-range
  "checks if an obstacle is in a specific range from vehicle location"
  [loc range obs] ;px py range
  (let [x (:x loc)
        y (:y loc)
        range-x1 (+ x (* range (Math/cos (:angle loc))))
        range-y1 (+ y (* vehicle-speed (Math/sin (:angle loc))))
        range-x2 (+ x (* range (Math/cos (- (:angle loc) field-of-view))))
        range-y2 (+ y (* vehicle-speed (Math/sin (- (:angle loc) field-of-view))))
        range-x3 (+ x (* range (Math/cos (+ (:angle loc) field-of-view))))
        range-y3 (+ y (* vehicle-speed (Math/sin (+ (:angle loc) field-of-view))))]
    ;check if the range to an obstacle is less than provided range
    (and (move-possible? range-x1 range-y1 obs) (move-possible? range-x2 range-y2 obs) 
             (move-possible? range-x3 range-y3 obs))))

(defn new-move
  "take obstacle-list, vehicle loc, new move, returns new loc based on move/obstacles and angle"
  [obstacles]
  ;lambda takes: current-loc (x y angle speed crash) and instruction
  (fn [loc instr]
    (cond (= (first instr) "angle") (move (change-attrib loc :angle (second instr)) obstacles)
          (= (first instr) "if-obs-range") 
              (if (no-obstacles-in-range loc (second instr) obstacles)
                (reduce (new-move obstacles) loc (first (rest (rest instr)))) loc))))

(defn write-instructions-to-file
  [instr-list filename]
  ;file needs to exist
  (spit filename
  (reduce (fn [total instr] (str total "\n" instr)) instr-list )))

;MAIN TESTING ENTRY PT.
; ------- Takes list of lists of moves for all indivs in gen and obs list -------
(defn test-instructions-list
  "takes in a list of vehicle instructions, a list of obstacles
  outputs a map of fitness (can be used for behavioral tracking too)"
  [instructionlist obstaclelist]
    (let [obs (if draw-to-window? (draw-obstacles obstaclelist) obstaclelist)
          final-loc (reduce (new-move obs) start-loc instructionlist)]
      (println final-loc)
     {:dist-to-target (distance (:x final-loc) (:y final-loc) (first target-loc) (second target-loc))
      :end-loc (list (:x final-loc) (:y final-loc))
      :num-crash (:crash final-loc)
      :instr-total (count instructionlist)}))

;file for testing system
(def testfile "data/pathfiles/condtest.txt")
(def testobsfile "data/obsfiles/cleartest.txt")

;FILE OPERATIONS
;---------------
(defn load-instruction-list
  "load instructions from file"
  [location-file]
  (let [load-lines (map (fn [line] (clojure.string/split line #" "))
                    (clojure.string/split-lines (slurp location-file)))
        parse-angle (fn [lst] (list (first lst) (Integer. (second lst))))
        parse-cond (fn [lst] (list (first lst) (Integer. (second lst)) 
                                   (map parse-angle (partition 2 (rest (rest lst))))))]
    (map (fn [instr]
            (cond (= (first instr) "angle") (parse-angle instr)
                  (= (first instr) "if-obs-range") (parse-cond instr)))  load-lines)))

(defn load-obstacle-list
  "load obstacles from file"
  [obs-file]
  (map #(read-string %) (clojure.string/split-lines (slurp obs-file))))

(defn test-instructions-file
  "loads instructions from file and obstacles from a file and executes list function"
  [location-file obs-file]
  ;this is a file wrapper for test-instructions-list
  (if draw-to-window? (start-environment))
  (test-instructions-list ;call list-based with parsed instruction file and parsed obs file
   (load-instruction-list location-file)
     (map #(read-string %) (clojure.string/split-lines (slurp obs-file)))))

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
         (fn [ind] (partition diversity-frame 1 ind))
         generation-instructions)]
    ;check total against distinct
    (- (count all-frames) (count (distinct all-frames)))))
