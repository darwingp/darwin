(ns push307.testmachine.machine
  (:require [push307.graphics.environment :refer :all])
(:gen-class))


;starting attributes
(def start-loc {:x 10 :y 10 :angle 45 :crash 0})           ;x y angle crash total
(def target-loc '(200 50))  ;location of target
(def vehicle-width 2)  ;not used as an exact radius
(def window-max-x 900) ;based on graphical window bounds
(def window-max-y 700)
(def draw-to-window? true)  ;plug graphical system into machine
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
    (let [obs (if draw-to-window? (draw-obstacles obstaclelist) obstaclelist)
          final-loc (reduce (new-move obs) start-loc instructionlist)]
     {:dist-to-target (distance (:x final-loc) (:y final-loc) (first target-loc) (second target-loc))
      :end-loc final-loc
      :num-crash (:crash final-loc)
      :instr-total (count instructionlist)}))

;testing
(def test-objects
  '({:x 855 :y 182 :width 4 :height 49}
{:x 737 :y 622 :width 21 :height 78}
{:x 591 :y 633 :width 73 :height 37}
{:x 252 :y 347 :width 1 :height 38}
{:x 670 :y 471 :width 19 :height 58}
{:x 425 :y 16 :width 17 :height 33}
{:x 646 :y 573 :width 8 :height 73}
{:x 781 :y 106 :width 56 :height 37}
{:x 860 :y 181 :width 87 :height 10}
{:x 328 :y 53 :width 89 :height 26}
{:x 191 :y 403 :width 29 :height 20}
{:x 143 :y 434 :width 9 :height 75}
{:x 543 :y 350 :width 10 :height 41}
{:x 718 :y 438 :width 94 :height 85}
{:x 540 :y 89 :width 16 :height 30}
{:x 244 :y 617 :width 40 :height 18}
{:x 340 :y 126 :width 60 :height 24}
{:x 636 :y 528 :width 84 :height 42}
{:x 174 :y 480 :width 54 :height 4}
{:x 885 :y 293 :width 40 :height 10}
{:x 197 :y 364 :width 91 :height 55}
{:x 770 :y 123 :width 79 :height 43}
{:x 150 :y 434 :width 13 :height 58}
{:x 833 :y 595 :width 54 :height 31}
{:x 209 :y 421 :width 16 :height 20}
{:x 862 :y 227 :width 33 :height 58}
{:x 89 :y 511 :width 93 :height 35}
{:x 34 :y 113 :width 62 :height 7}
{:x 530 :y 52 :width 57 :height 73}
{:x 887 :y 43 :width 46 :height 26}
{:x 17 :y 48 :width 59 :height 42}
{:x 34 :y 506 :width 41 :height 98}
{:x 6 :y 635 :width 68 :height 49}
{:x 439 :y 397 :width 73 :height 64}
{:x 533 :y 408 :width 26 :height 17}
{:x 223 :y 409 :width 55 :height 92}
{:x 344 :y 367 :width 51 :height 97}
{:x 411 :y 157 :width 74 :height 6}
{:x 665 :y 114 :width 95 :height 20}
{:x 690 :y 664 :width 40 :height 97}
{:x 522 :y 262 :width 97 :height 64}
{:x 371 :y 458 :width 60 :height 10}
{:x 56 :y 361 :width 5 :height 6}
{:x 2 :y 553 :width 31 :height 30}
{:x 642 :y 486 :width 30 :height 18}
{:x 778 :y 418 :width 37 :height 24}
{:x 863 :y 581 :width 57 :height 83}
{:x 220 :y 306 :width 41 :height 30}
{:x 425 :y 60 :width 93 :height 64}
{:x 602 :y 644 :width 76 :height 76}
{:x 236 :y 509 :width 19 :height 64}
{:x 14 :y 698 :width 63 :height 94}
{:x 243 :y 177 :width 1 :height 71}
{:x 96 :y 331 :width 62 :height 17}
{:x 343 :y 180 :width 96 :height 28}
{:x 858 :y 215 :width 89 :height 35}
{:x 335 :y 660 :width 44 :height 100}
{:x 456 :y 666 :width 23 :height 50}
{:x 789 :y 430 :width 63 :height 4}
{:x 332 :y 135 :width 56 :height 98}
{:x 78 :y 664 :width 30 :height 90}
{:x 25 :y 503 :width 81 :height 24}
{:x 447 :y 230 :width 35 :height 66}
{:x 696 :y 298 :width 35 :height 64}
{:x 886 :y 301 :width 3 :height 6}
{:x 655 :y 462 :width 92 :height 36}
{:x 615 :y 177 :width 73 :height 15}
{:x 206 :y 463 :width 64 :height 33}
{:x 365 :y 113 :width 45 :height 38}
{:x 346 :y 54 :width 86 :height 19}))

;file for testing system
(def testfile "pathfiles/test1000.txt")

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
  (if draw-to-window? (start-environment))
  (test-instructions-list
  (map gen-instruction
    ;split line by space
    (map (fn [line] (clojure.string/split line #" "))
    (clojure.string/split-lines (slurp location)))) obs-list))
