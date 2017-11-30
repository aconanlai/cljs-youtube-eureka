(ns youtube
    (:require
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

;; TODO get duration and set timer here to handle video change
(defn handle-state-change
  [state]
  (utils/log state))

;; TODO: use loadVideoById - split this into two functions for initial load and subsequent loads
(defn init
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