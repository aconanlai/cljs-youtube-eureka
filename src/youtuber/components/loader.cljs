(ns components.loader
  (:require [reagent.core :as reagent]
    [utils]
    [youtube]
    [state]))

(defn navigate-to-video
  [id]
  (set! (-> js/window .-location .-hash) (str "#/" id)))

; (defn navigate-to-video
;   [id]
;   (.loadVideoById js/player id))
    
(defn loader-form []
  [:form
    [:input {:id "loader-form" :name "loader-form" :value (@state/store :loader-value)
                :placeholder "Video ID"
                :on-change #(swap! state/store assoc-in [:loader-value] (-> % .-target .-value))}]
    [:button {
              :type "button"
              :on-click #(navigate-to-video (@state/store :loader-value))
              :disabled (= (@state/store :loader-value) "")}
             "Load Video"]]))


