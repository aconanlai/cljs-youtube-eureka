(ns actions
  (:require
    [ajax.core :refer [GET POST]]
    [utils]
    [youtube]
    [state]))

(defn get-handler [response]
  (swap! state/store assoc :annotations (map utils/keywordize (:comments (utils/keywordize (js->clj response))))))

(defn post-handler [response]
  (do
    (youtube/play-video)
    (swap! state/store assoc-in [:form-open] false)
    (swap! state/store assoc-in [:comment] "")))

(defn error-handler [{:keys [status status-text]}]
  (utils/log (str "something bad happened: " status " " status-text)))

(defn get-comments [id]
  (GET (str "http://localhost:3000/video/" id)
   {:handler get-handler
    :keywords? true
    :error-handler error-handler}))

(defn send-comment
  []
  (let [payload {:id (@state/store :id)
                   :time (@state/store :time)
                   :comment (@state/store :comment)}]
   (do
    (swap! state/store update-in [:annotations] #(sort-by :time (into % [payload])))
    (POST "http://localhost:3000/video"
          {:params payload
           :format :json
           :handler post-handler
           :error-handler error-handler}))))