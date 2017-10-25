(ns youtuber.core
    (:require [reagent.core :as reagent]))

(enable-console-print!)

(println "This text is printed from src/youtuber/core.cljs. Go ahead and edit it and see reloading in action.")

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(def app-state (reagent/atom {:time 0 :annotations {2 "hua" 3 "doda" 7 "uhh" 10 "wheeee"}}))

(defn within-range
  [middle key v]
  (let [k (int (first key))]
    (<= (- k 5) middle (+ k 5))))
  
(def shown-annotations (reagent.ratom/reaction (reverse (filter (partial within-range (@app-state :time)) (@app-state :annotations)))))

(defn annotation-panel [[time comment]]
  [:div.annotation {:key time}
   [:span.time time ":"] 
   [:span.comment comment]])
  
(defn app []
  [:div.wrapper
   [:h1.title (@app-state :time)]
   [:div#video]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @shown-annotations)]]])

(reagent/render-component [app]
                      (. js/document (getElementById "app")))

(def youtube
  (let [Player (.-Player js/YT)]
      (Player. "video"
          (-> {:videoId "TDs-OPZsbUE"}
           clj->js))))

(defn get-time
  []
  (let [current-time (int (.getCurrentTime youtube))]
   (swap! app-state assoc-in [:time] current-time)))

(js/setInterval get-time 1000)

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

