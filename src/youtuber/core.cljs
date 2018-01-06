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
        (youtube/change-video id)
        (actions/get-comments id)
        (actions/get-related id)
        (swap! state/store assoc-in [:id] id)
        (secretary/dispatch! (.-token event)))))
   (.setEnabled true)))

(defn set-video-width
  []
  (let [window-width (.-innerWidth js/window)
        width (min 700 (* (.-innerWidth js/window) 0.5))
        chosen-width (if (< window-width 700) window-width width)]
   (do
    (swap! state/store assoc-in [:video-width] chosen-width)
    (youtube/resize-video))))

(set-video-width)
(.addEventListener js/window "resize" set-video-width)

(hook-browser-navigation!)

(reagent/render-component [current-page] (.getElementById js/document "app"))
