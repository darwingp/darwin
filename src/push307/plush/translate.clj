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
  (println sequence)
  (cond (not (seq? sequence)) sequence
        (empty? sequence) ()
        :else (let [opens (count (filter #(= :open %) sequence))
                    closes (count (filter #(= :close %) sequence))]
                (assert (= opens closes)
                        (str "open-close sequence must have equal numbers of :open and :close; this one does not:\n" sequence))

                ;; FIXME: There has to be a better way to do this.
                ;;        Allow the ignorance of trailing close parens.
                (let [s (str sequence)
                      l (read-string (string/replace (string/replace s ":open" " ( ") ":close" " ) "))]
                  ;; There will be an extra ( ) around l,
                  ;; which we keep if the number of read things is >1
                  (if (= (count l) 1) (first l) l)))))

(defn translate-plush-genome-to-push-program
  "Takes as input a Plush genome as either a list or a vector.
   and translates it to the correct Push program with balanced parens. The
   linear Plush genome is made up of a list of gene maps, each including an
   :instruction key as well as other epigenetic marker keys."
  [genome]
    (loop [prog [] ; The Push program incrementally being built
           gn genome ; The linear Plush genome, where items will be popped off the front. Each item is a map
                     ; containing at least the key :instruction, and unless the program is flat, also :close
           num-parens-here 0 ; The number of parens that still need to be added at this location.
           paren-stack '()] ; Whenever an instruction requires paren grouping, it will push either :close or
                            ; :close-open on this stack. This will indicate what to insert in the program the
                            ; next time a close paren is indicated by the :close key in the instruction map.
      (cond
        ;; Check if need to add close parens here
        (< 0 num-parens-here) (recur (cond
                                       (= (first paren-stack) :close) (conj prog :close)
                                       (= (first paren-stack) :open) (conj prog :open)
                                       :else prog) ; If paren-stack is empty, we won't put any parens in even
                                                   ; though the :close epigenetic marker indicated to do so
                                     gn
                                     (dec num-parens-here)
                                     (rest paren-stack))

        ;; Check if at end of program but still need to add parens
        (and (empty? gn)
             (not (empty? paren-stack))) (recur prog
                                                gn
                                                (count paren-stack)
                                                paren-stack)

        ;; Check if done
        (empty? gn) (open-close-sequence-to-list (apply list (filter #(not (nil? %)) prog)))

        ;; Check for silenced instruction
        (get (first gn) :silent false) (recur prog
                                              (rest gn)
                                              num-parens-here
                                              paren-stack)

        ;; Otherwise, ready for next instruction
        :else (let [number-close-parens (get (first gn) :close 0) ; The number of close parens to put after this instruction.
                    number-open-parens (get (first gn) :open 0) ; The number of open parens to put before this instruction.
                    is-noop (get (first gn) :no-op false)
                    new-paren-stack (concat (repeat number-close-parens :close) paren-stack) ;; TODO: add parens at end to close out :open's
                    prog-with-opens (vec (concat prog (repeat number-open-parens :open)))
                    instruction (when (not is-noop) (:instruction (first gn)))]
                 (recur (conj prog-with-opens instruction)
                        (rest gn)
                        number-close-parens
                        new-paren-stack))))))
