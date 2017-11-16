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

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

;; state

(def app-state (reagent/atom {:id ""
                              :time 0
                              :form-open false
                              :comment ""
                              :annotations [{:time 2 :comment "this ones bretty good"}
                                            {:time 7 :comment "hi hello"}
                                            {:time 10 :comment "whoowapap"}
                                            {:time 15 :comment "dodah"}
                                            {:time 18 :comment "this is my favorite part"}]}))


(defn within-range
  [middle annotation]
  (let [time (:time annotation)]
      (<= (- time 5) middle (+ time 5))))
  
(def shown-annotations (reagent.ratom/reaction (reverse (filter (partial within-range (@app-state :time)) (@app-state :annotations)))))

;; funcs

(defn convert-timestamp
  [seconds]
  (str (int (/ seconds 60)) ":" (cl-format nil "~2,'0d" (mod seconds 60))))

(defn toggle-form
  []
  (swap! app-state update-in [:form-open] #(not %)))

;; components

(defn annotation-panel [annotation]
  (let [{time :time comment :comment} annotation]
   [:div.annotation {:key (str time comment)}
    [:span.time (convert-timestamp time) ":"] 
    [:span.comment comment]]))

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
     (map annotation-panel @shown-annotations)
     ]]])


(defn home-page []
  [:h1 "hua"])

(defn about-page []
  [:h1 "about"])

;; http
(defn keywordize
  [hashmap]
  (into {} 
    (for [[k v] hashmap] 
      [(keyword k) v])))

(defn handler [response]
  (swap! app-state assoc :annotations (map keywordize (:comments (keywordize (js->clj response :keywordize-keys true))))))

(defn error-handler [{:keys [status status-text]}]
  (.log js/console (str "something bad happened: " status " " status-text)))

(defn get-comments [id]
  (GET (str "http://localhost:3000/video/" id)
   {:handler handler
    :keywords? true
    :error-handler error-handler}))

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
        (secretary/dispatch! (.-token event)))))
   (.setEnabled true)))

(hook-browser-navigation!)

(reagent/render-component [current-page] (.getElementById js/document "app"))

(defn on-js-reload [])
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)

(println (map #(:comment %) @shown-annotations))