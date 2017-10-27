(ns youtuber.core
    (:require [reagent.core :as reagent]
      [reagent.session :as session]
      [secretary.core :as secretary :include-macros true]
      [goog.events :as events]
      [goog.history.EventType :as EventType]
      [cljs.pprint :refer [cl-format]])
    (:import goog.History))

(enable-console-print!)

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

;; state

(def app-state (reagent/atom {:time 0 :form-open false :comment "" :annotations {2 "this ones bretty good" 7 "hi hello" 10 "whoowapap" 15 "dodah" 18 "this is my favorite part"}}))

(defn within-range
  [middle key v]
  (let [k (int (first key))]
    (<= (- k 5) middle (+ k 5))))
  
(def shown-annotations (reagent.ratom/reaction (reverse (filter (partial within-range (@app-state :time)) (@app-state :annotations)))))
                                          
;; "TDs-OPZsbUE"

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
      [:form
       [:textarea {:id "comment" :name "comment" :value (@app-state :comment)
                   :on-change #(swap! app-state assoc-in [:comment] (-> % .-target .-value))}]]))

;; youtube component and app timer

(defn get-time
  []
  (let [current-time (int (.getCurrentTime js/player))]
   (swap! app-state assoc-in [:time] current-time)))

(defn youtube
  [id]
  (set! js/player
    (let [Player (.-Player js/YT)]
      (Player. "video"
        (-> {:videoId id :playerVars {:rel 0}}
          clj->js)))))

(defn video-page []
  [:div.wrapper
   [:h1.title (@app-state :time)]
   [:div#video]
   [add-form]
   [add-button]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @shown-annotations)]]])

; (reagent/render-component [app]
;                       (. js/document (getElementById "app")))

(defn home-page []
  [:h1 "hua"])

(defn about-page []
  [:h1 "about"])

;; routing

(defn current-page []
  [:div [(session/get :current-page)]])

;; routes
(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'about-page))

(secretary/defroute "/:video-id" {:as params}
  (session/put! :current-page #'video-page))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (js/setTimeout #(youtube (.substring (.-token event) 1)) 0)
       (set! js/timer (js/setInterval get-time 1000))
      (secretary/dispatch! (.-token event))))
   (.setEnabled true)))

(hook-browser-navigation!)

(reagent/render-component [current-page] (.getElementById js/document "app"))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

