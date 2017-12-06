(ns youtube
    (:require
      [components.loader]
      [utils]
      [state]))

(defn get-time
  []
  (let [current-time (int (.getCurrentTime js/player))]
   (swap! state/store assoc-in [:time] current-time)))

(defn get-duration
  []
  (let [duration (int (.getDuration js/player))]
   (swap! state/store assoc-in [:duration] duration)))

(defn play-video
  []
  (.playVideo js/player))

(defn handle-ready
  [state]
  (do
    (get-duration)
    (set! js/timer (js/setInterval get-time 100))))

(defn init
  [id]
  (set! js/player
    (let [Player (.-Player js/YT)]
        (Player. "video"
          (-> {
               :videoId id
               :playerVars {:rel 0}
               :events {:onReady handle-ready}}
            clj->js)))))

(defn remove-interval
  "tear down old interval when loading new video"
  []
  (if (exists? js/timer)
   (js/clearInterval js/timer)))

(defn change-video
  [id]
  (do
   (remove-interval)
   (js/setTimeout #(init id) 100)))

