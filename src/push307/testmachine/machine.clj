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
(def vehicle-speed 10)  ;default tick speed

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
  '({:x 62 :y 120 :width 62 :height 100}
{:x 778 :y 413 :width 33 :height 46}
{:x 858 :y 674 :width 52 :height 97}
{:x 11 :y 68 :width 62 :height 23}
{:x 839 :y 336 :width 97 :height 59}
{:x 264 :y 382 :width 68 :height 52}
{:x 453 :y 314 :width 37 :height 97}
{:x 534 :y 651 :width 26 :height 80}
{:x 89 :y 197 :width 57 :height 51}
{:x 634 :y 31 :width 74 :height 42}
{:x 208 :y 620 :width 15 :height 61}
{:x 765 :y 409 :width 72 :height 44}
{:x 421 :y 544 :width 53 :height 29}
{:x 61 :y 384 :width 84 :height 87}
{:x 669 :y 171 :width 17 :height 56}
{:x 324 :y 60 :width 96 :height 11}
{:x 729 :y 247 :width 3 :height 89}
{:x 465 :y 166 :width 59 :height 95}
{:x 579 :y 339 :width 45 :height 90}
{:x 695 :y 388 :width 70 :height 25}
{:x 287 :y 108 :width 86 :height 71}
{:x 789 :y 469 :width 80 :height 44}
{:x 897 :y 584 :width 3 :height 25}
{:x 829 :y 25 :width 81 :height 90}
{:x 817 :y 447 :width 95 :height 39}
{:x 387 :y 15 :width 73 :height 99}
{:x 665 :y 161 :width 17 :height 84}
{:x 456 :y 191 :width 90 :height 63}
{:x 617 :y 540 :width 31 :height 34}
{:x 281 :y 519 :width 44 :height 32}
{:x 44 :y 311 :width 7 :height 15}
{:x 688 :y 333 :width 37 :height 51}
{:x 235 :y 664 :width 35 :height 19}
{:x 789 :y 172 :width 90 :height 48}
{:x 6 :y 342 :width 40 :height 66}
{:x 255 :y 556 :width 64 :height 11}
{:x 223 :y 149 :width 60 :height 50}
{:x 525 :y 197 :width 72 :height 69}
{:x 327 :y 217 :width 60 :height 75}
{:x 766 :y 603 :width 23 :height 65}
{:x 453 :y 493 :width 14 :height 71}
{:x 655 :y 198 :width 27 :height 54}
{:x 572 :y 218 :width 11 :height 89}
{:x 352 :y 230 :width 99 :height 16}
{:x 601 :y 573 :width 88 :height 78}
{:x 634 :y 226 :width 4 :height 79}
{:x 887 :y 246 :width 77 :height 51}
{:x 12 :y 675 :width 40 :height 62}
{:x 578 :y 191 :width 69 :height 2}
{:x 652 :y 303 :width 53 :height 22}
{:x 401 :y 572 :width 84 :height 39}
{:x 506 :y 207 :width 34 :height 37}
{:x 698 :y 618 :width 99 :height 19}
{:x 294 :y 546 :width 11 :height 38}
{:x 124 :y 499 :width 49 :height 15}
{:x 561 :y 668 :width 77 :height 68}
{:x 576 :y 478 :width 99 :height 41}
{:x 23 :y 503 :width 41 :height 13}
{:x 559 :y 74 :width 71 :height 16}
{:x 50 :y 336 :width 14 :height 100}
{:x 207 :y 524 :width 86 :height 79}
{:x 541 :y 77 :width 37 :height 83}
{:x 439 :y 620 :width 56 :height 26}
{:x 695 :y 258 :width 48 :height 56}
{:x 144 :y 417 :width 32 :height 86}
{:x 151 :y 24 :width 18 :height 40}
{:x 361 :y 154 :width 28 :height 9}
{:x 36 :y 282 :width 17 :height 15}
{:x 559 :y 269 :width 32 :height 65}
{:x 94 :y 538 :width 18 :height 71}
{:x 771 :y 601 :width 77 :height 77}
{:x 863 :y 148 :width 77 :height 26}
{:x 612 :y 110 :width 23 :height 57}
{:x 579 :y 555 :width 85 :height 78}
{:x 82 :y 344 :width 1 :height 40}
{:x 559 :y 365 :width 92 :height 26}
{:x 847 :y 679 :width 95 :height 15}
{:x 341 :y 581 :width 58 :height 68}
{:x 727 :y 383 :width 39 :height 22}
{:x 490 :y 373 :width 70 :height 48}
{:x 280 :y 655 :width 19 :height 92}
{:x 159 :y 693 :width 93 :height 100}
{:x 6 :y 300 :width 37 :height 83}
{:x 362 :y 396 :width 48 :height 9}
{:x 101 :y 470 :width 11 :height 42}
{:x 356 :y 256 :width 61 :height 88}
{:x 893 :y 547 :width 37 :height 87}
{:x 237 :y 323 :width 54 :height 63}
{:x 864 :y 7 :width 66 :height 57}
{:x 43 :y 80 :width 53 :height 12}))

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
