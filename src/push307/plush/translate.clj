(ns push307.plush.translate
  (:require [clojure.string :as string])
  (:gen-class))

;; CITE: Thomas Helmuth & Lee Spector (Clojush)
;; DESC: This logic was copied from Lee Spector's Clojush,
;;       modified by Thomas Helmuth, and finally modified by
;;       the authors of this program.

(defn open-close-sequence-to-list
  "Takes a sequence and replaces instances of :open with an open paren
   and instances of :close with a close paren."
  [sequence]
  (cond (not (seq? sequence)) sequence
        (empty? sequence) ()
        :else (let [opens (count (filter #(= :open %) sequence))
                    closes (count (filter #(= :close %) sequence))]
                (println (str sequence))
                (assert (= opens closes)
                        (str "open-close sequence must have equal numbers of :open and :close; this one does not:\n" sequence))

                ;; FIXME: There has to be a better way to do this.
                ;;        Allow the ignorance of trailing close parens.
                (let [s (str sequence)
                      l (read-string (string/replace (string/replace s ":open" " ( ") ":close" " ) "))]
                  ;; There will be an extra ( ) around l,
                  ;; which we keep if the number of read things is >1
                  (if (= (count l) 1) (first l) l)))))

(defn recursive-reverse
  "Reverses all elements in the input with "
  [coll]
  (reverse (map #(if (seq? %) (recursive-reverse %) %) coll)))

(defn translate-plush-genome-to-push-program
  "Takes as input a Plush genome and translates it to the correct Push program with
   balanced parens. As the linear Plush genome is traversed, each instruction that requires
   parens will push :close and/or :close-open onto the paren-stack, and will
   also put an open paren after it in the program. For example, an instruction
   that requires 3 paren groupings will push :close, then :close-open, then :close-open.
   When a positive number is encountered in the :close key of the
   instruction map, it is set to num-parens-here during the next recur. This
   indicates the number of parens to put here, if need is indicated on the
   paren-stack. If the top item of the paren-stack is :close, a close paren
   will be inserted. If the top item is :close-open, a close paren followed by
   an open paren will be inserted.
   If the end of the program is reached but parens are still needed (as indicated by
   the paren-stack), parens are added until the paren-stack is empty.
   Instruction maps that have :silence set to true will be ignored entirely."
  [genome]
  (loop [prog [] ; The Push program incrementally being built
         gn (apply list (reverse genome)) ; The linear Plush genome, where items will be popped off the front. Each item is a map containing at least the key :instruction, and unless the program is flat, also :close
         num-parens-here 0 ; The number of parens that still need to be added at this location.
         paren-stack '()] ; Whenever an instruction requires parens grouping, it will push either :close or :close-open on this stack. This will indicate what to insert in the program the next time a paren is indicated by the :close key in the instruction map.
    (cond
      ; Check if need to add close parens here
      (< 0 num-parens-here) (recur (cond
                                     (= (first paren-stack) :close) (conj prog :close)
                                     (= (first paren-stack) :close-open) (conj (conj prog :close) :open)
                                     :else prog) ; If paren-stack is empty, we won't put any parens in even though the :close epigenetic marker indicated to do so
                                   gn
                                   (dec num-parens-here)
                                   (rest paren-stack))

      ; Check if at end of program but still need to add parens
      (and (empty? gn)
           (not (empty? paren-stack))) (recur prog
                                              gn
                                              (count paren-stack)
                                              paren-stack)

      ; Check if done
      (empty? gn) (recursive-reverse (open-close-sequence-to-list (apply list (filter #(not (nil? %)) prog))))
      
      ; Check for silenced instruction
      (get (first gn) :silent false) (recur prog
                                            (rest gn)
                                            num-parens-here
                                            paren-stack)

      ; Otherwise, ready for next instruction
      :else (let [number-paren-groups (get (first gn) :arity 0)
                  is-noop (get (first gn) :no-op false)
                  instr (if is-noop nil (:instruction (first gn)))
                  new-paren-stack (if (>= 0 number-paren-groups)
                                    paren-stack
                                    (concat (repeat (dec number-paren-groups) :close-open)
                                            '(:close)
                                            paren-stack))]
              (recur (if (>= 0 number-paren-groups)
                       (conj prog instr)
                       (conj (conj prog instr) :open))
                     (rest gn)
                     (get (first gn) :close 0) ; The number of close parens to put after this instruction;
                     new-paren-stack)))))

