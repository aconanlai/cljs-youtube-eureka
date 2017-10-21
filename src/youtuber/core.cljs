(ns youtuber.core
    (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "This text is printed from src/youtuber/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:annotations {2 "hua"}}))

(defn app []
  [:div#video])

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(defn state-change
  [state]
  (println (aget state "data")))

(def youtube
  (let [Player (.-Player js/YT)]
      (Player. "video"
          (-> {:videoId "TDs-OPZsbUE"}
              ;  :events {:onReady #(println "loaded") :onStateChange state-change} 
           clj->js))))

; (println ((@app-state :annotations) 10))

; (println (filter (fn [k v] (<= (- k 5) k (+ k 5))) (@app-state :annotations)))

; (println (@app-state :annotations))

(println (map (fn [key value] (let [k (int key)] (<= (- k 5) k (+ k 5)))) (@app-state :annotations)))

(defn find-annotations
  []
  (let [time (int (.getCurrentTime youtube))]
   (println (filter (fn [k v] (<= (- k 5) k (+ k 5))) (@app-state :annotations)))))

; (find-annotations)

; (js/setInterval #(println (int (.getCurrentTime youtube))) 1000)

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

