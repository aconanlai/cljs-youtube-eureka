(ns youtuber.core
    (:require [reagent.core :as reagent :refer [atom]]))

(enable-console-print!)

(println "This text is printed from src/youtuber/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:text "Hello world!"}))

(defn app []
  [:div#video])

(reagent/render-component [app]
                          (. js/document (getElementById "app")))

(def youtube
  (let [Player (.-Player js/YT)]
      (Player. "video"
          (-> {:videoId "TDs-OPZsbUE"
              :events {:onReady #(println "loaded") :onStateChange #(println "state change")}}
           clj->js))))

; (js/setTimeout #(println (.getPlayerState youtube)) 3000)

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

