(ns state
  (:require [reagent.core :as reagent]
    [utils]))

(def store (reagent/atom {:id ""
                              :time 0
                              :duration 0
                              :form-open false
                              :comment ""
                              :annotations []}))

; (defn within-range1
;   [middle annotation]
;   (let [time (:time annotation)]
;       (<= (- time 5) middle (+ time 5))))              

(defn within-range
  [vect middle]
  (->> vect
    (drop-while #(< (:time %) (- middle 5)))
    (take-while #(< (:time %) (+ middle 5)))))

                          
; (def shown-annotations
;   (reagent.ratom/reaction (reverse (filter (partial within-range1 (@store :time)) (@store :annotations)))))

(def shown-annotations
  (reagent.ratom/reaction (within-range (@store :annotations) (@store :time))))

(def all-annotations
  (reagent.ratom/reaction (@store :annotations)))
