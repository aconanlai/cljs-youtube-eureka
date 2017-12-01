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

;; TODO get duration and set timer here to handle video change
(defn handle-state-change
  [state]
  (if (= (aget state "data") -1)
   (do
     (utils/log "new video loaded")
     (get-duration)
     (set! js/timer (js/setInterval get-time 100)))))

;; TODO: use loadVideoById - split this into two functions for initial load and subsequent loads
(defn init
  [id]
  (set! js/player
    (let [Player (.-Player js/YT)]
      (do (println "initing video")
        (Player. "video"
          (-> {
               :videoId id
               :playerVars {:rel 0}
               :events {:onStateChange handle-state-change}}
            clj->js))))))

(defn load-video
  [id]
  (do
   (println "supposed to load new video...")
   (println "supposed to load new videosadasd...")
   (println id)))
   
  
(defn change-video
  [id]
  (if (exists? js/player)
   (load-video id)
   (js/setTimeout #(init id) 0)))

; (js/setTimeout #(youtube/init id) 0)
; (set! js/timer (js/setInterval youtube/get-time 100))
