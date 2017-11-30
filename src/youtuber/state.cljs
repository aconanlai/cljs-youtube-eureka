(ns state
  (:require [reagent.core :as reagent]
    [utils]))

(def store (reagent/atom {:id ""
                              :time 0
                              :duration 0
                              :form-open false
                              :comment ""
                              :annotations []}))

(def shown-annotations
  (reagent.ratom/reaction (reverse (filter (partial utils/within-range (@store :time)) (@store :annotations)))))
