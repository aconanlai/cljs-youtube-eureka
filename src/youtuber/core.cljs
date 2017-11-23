(ns youtuber.core
    (:require [reagent.core :as reagent]
      [reagent.session :as session]
      [secretary.core :as secretary :include-macros true]
      [goog.events :as events]
      [goog.history.EventType :as EventType]
      [cljs.pprint :refer [cl-format]]
      [cljs.core.async :refer [<!]]
      [ajax.core :refer [GET POST]])
    (:require-macros [cljs.core.async.macros :refer [go]])
    (:import goog.History))

(enable-console-print!)

;; for debug
(defn log
  [obj]
  (.log js/console obj))

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

;; state

(def app-state (reagent/atom {:id ""
                              :time 0
                              :duration 0
                              :form-open false
                              :comment ""
                              :annotations []}))


(defn within-range
  [middle annotation]
  (let [time (:time annotation)]
      (<= (- time 5) middle (+ time 5))))
  
(def shown-annotations
  (reagent.ratom/reaction (reverse (filter (partial within-range (@app-state :time)) (@app-state :annotations)))))

(defn get-timeline-position
  "returns pixel position of marker based on duration of video"
  [time]
  (let [duration (@app-state :duration)]
    (if (= duration 0)
     0
     (* 640 (/ time duration)))))

;; http
(defn keywordize
  [hashmap]
  (into {} 
    (for [[k v] hashmap] 
      [(keyword k) v])))

(defn get-handler [response]
  (swap! app-state assoc :annotations (map keywordize (:comments (keywordize (js->clj response))))))

(defn post-handler [response]
  (log response))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-comments [id]
  (GET (str "http://localhost:3000/video/" id)
   {:handler get-handler
    :keywords? true
    :error-handler error-handler}))

(defn send-comment
  []
  (POST "http://localhost:3000/video"
        {:params {:id (@app-state :id)
                  :time (@app-state :time)
                  :comment (@app-state :comment)}
         :format :json
        ;  :headers {:content-type "application/x-www-form-urlencoded"}
         :handler post-handler
         :error-handler error-handler}))

;; funcs

(defn convert-timestamp
  [seconds]
  (str (int (/ seconds 60)) ":" (cl-format nil "~2,'0d" (mod seconds 60))))

(defn open-form
  []
  (do
    (.pauseVideo js/player)
    (swap! app-state update-in [:form-open] #(not %))))

;; components

(defn annotation-panel [annotation]
  (let [{time :time comment :comment} annotation]
   [:div.annotation {:key (str time comment)}
    [:span.time (convert-timestamp time) ":"] 
    [:span.comment comment]]))

(defn add-button []
  (if (not (@app-state :form-open))
    [:button {:on-click open-form} "Add Annotation"]))

(defn add-form []
  (if (@app-state :form-open)
      [:form
       [:div
        [:span (convert-timestamp (@app-state :time))]]
       [:textarea {:id "comment" :name "comment" :value (@app-state :comment)
                   :on-change #(swap! app-state assoc-in [:comment] (-> % .-target .-value))}]
       [:div
        [:button {:type "button" :on-click send-comment} "Submit"]]]))

(defn annotation-marker
  [{:keys [time comment]}]
  [:div.annotation-marker
   {:key (str time comment)
    :style {:left (str (get-timeline-position time) "px")}}])

;; TODO: change time of video
(defn on-click-timeline
  [e]
  (log (.-offsetX (.-nativeEvent e))))

(defn timeline []
  [:div.timeline
    {:on-click on-click-timeline}
   [:div.current-time
    {:style {:left (get-timeline-position (@app-state :time))}}]
   (doall (map annotation-marker (@app-state :annotations)))])

;; youtube component and app timer

(defn get-time
  []
  (let [current-time (int (.getCurrentTime js/player))]
   (swap! app-state assoc-in [:time] current-time)))

(defn get-duration
  []
  (let [duration (int (.getDuration js/player))]
   (swap! app-state assoc-in [:duration] duration)))

;; TODO get duration and set timer here to handle video change
(defn handle-state-change
  [state]
  (log state))

;; TODO: use loadVideoById - split this into two functions for initial load and subsequent loads
(defn youtube
  [id]
  (set! js/player
    (let [Player (.-Player js/YT)]
        (Player. "video"
          (-> {
               :videoId id
               :playerVars {:rel 0}
               :events {:onReady get-duration
                        :onStateChange handle-state-change}}
            clj->js)))))
        
(defn video-page []
  [:div.wrapper
   [:h1.title (@app-state :time)]
   [:h2 (@app-state :duration)]
   [:div#video]
   [timeline]
   [add-form]
   [add-button]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @shown-annotations)]]])
     
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
       (let [id (.substring (.-token event) 1)]
        (js/setTimeout #(youtube id) 0)
        (set! js/timer (js/setInterval get-time 1000))
        (get-comments id)
        (swap! app-state assoc-in [:id] id)
        (secretary/dispatch! (.-token event)))))
   (.setEnabled true)))

(hook-browser-navigation!)

(reagent/render-component [current-page] (.getElementById js/document "app"))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
