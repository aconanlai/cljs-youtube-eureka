(ns youtuber.core
    (:require [reagent.core :as reagent]
      [cljs.pprint :refer [cl-format]]))

(enable-console-print!)

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

;; state

(def app-state (reagent/atom {:time 0 :form-open false :annotations {2 "hua" 3 "doda" 7 "uhh" 10 "wheeee"}}))

(defn within-range
  [middle key v]
  (let [k (int (first key))]
    (<= (- k 5) middle (+ k 5))))
  
(def shown-annotations (reagent.ratom/reaction (reverse (filter (partial within-range (@app-state :time)) (@app-state :annotations)))))

;; funcs

(defn convert-timestamp
  [seconds]
  (str (int (/ seconds 60)) ":" (cl-format nil "~2,'0d" (mod seconds 60))))

(defn toggle-form
  []
  (swap! app-state update-in [:form-open] #(not %)))

;; components

(defn annotation-panel [[time comment]]
  [:div.annotation {:key time}
   [:span.time (convert-timestamp time) ":"] 
   [:span.comment comment]])

(defn add-button []
  (if (not (@app-state :form-open))
    [:button {:on-click toggle-form} "Add Annotation"]))

(defn add-form []
  (if (@app-state :form-open)
    [:span "hello"]))

(defn app []
  [:div.wrapper
   [:h1.title (@app-state :time)]
   [:div#video]
   [add-form]
   [add-button]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @shown-annotations)]]])

(reagent/render-component [app]
                      (. js/document (getElementById "app")))

;; youtube component and app timer

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

