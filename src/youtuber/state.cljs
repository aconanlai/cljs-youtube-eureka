(ns state
  (:require [reagent.core :as reagent]
    [utils]))

(def store (reagent/atom {:id ""
                              :time 0
                              :duration 0
                              :form-open false
                              :comment ""
                              :video-width 0
                              :annotations []
                              :related []}))
                                  
(defn within-range
  [vect middle]
  (->> vect
    (drop-while #(< (:time %) (- middle 5)))
    (take-while #(< (:time %) (+ middle 5)))))

(def shown-annotations
  (reagent.ratom/reaction (within-range (@store :annotations) (@store :time))))

(def all-annotations
  (reagent.ratom/reaction (@store :annotations)))
