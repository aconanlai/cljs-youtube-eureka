(ns youtuber.core
    (:require [reagent.core :as reagent]
      [reagent.session :as session]
      [secretary.core :as secretary :include-macros true]
      [goog.events :as events]
      [goog.history.EventType :as EventType]
      [state]
      [utils]
      [actions]
      [youtube]
      [pages.home]
      [pages.video]
      [pages.about])
    (:import goog.History))

(enable-console-print!)
        
(defn current-page []
  [:div [(session/get :current-page)]])

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (session/put! :current-page #'pages.home/home-page))

(secretary/defroute "/about" []
  (session/put! :current-page #'pages.about/about-page))

(secretary/defroute "/:video-id" {:as params}
  (session/put! :current-page #'pages.video/video-page))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
     EventType/NAVIGATE
     (fn [event]
       (let [id (.substring (.-token event) 1)]
        (js/setTimeout #(youtube/init id) 0)
        (set! js/timer (js/setInterval youtube/get-time 100))
        (actions/get-comments id)
        (swap! state/store assoc-in [:id] id)
        (secretary/dispatch! (.-token event)))))
   (.setEnabled true)))

(hook-browser-navigation!)

(reagent/render-component [current-page] (.getElementById js/document "app"))
