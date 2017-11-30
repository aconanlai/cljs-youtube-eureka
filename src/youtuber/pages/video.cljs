(ns pages.video
  (:require [reagent.core :as reagent]
    [utils]
    [actions]
    [state]))

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn seconds->timeline
  "returns pixel position of marker based on duration of video"
  [time]
  (let [duration (@state/store :duration)]
    (if (= duration 0)
     0
     (* 640 (/ time duration)))))
  
(defn open-form
  []
  (do
    (.pauseVideo js/player)
    (swap! state/store update-in [:form-open] #(not %))))

(defn annotation-panel [annotation]
  (let [{time :time comment :comment} annotation]
   [:div.annotation {:key (str time comment)}
    [:span.time (utils/convert-timestamp time) ":"] 
    [:span.comment comment]]))

(defn add-button []
  (if (not (@state/store :form-open))
    [:button {:on-click open-form} "Add Annotation"]))

(defn add-form []
  (if (@state/store :form-open)
      [:form
       [:div
        [:span (utils/convert-timestamp (@state/store :time))]]
       [:textarea {:id "comment" :name "comment" :value (@state/store :comment)
                   :on-change #(swap! state/store assoc-in [:comment] (-> % .-target .-value))}]
       [:div
        [:button {:type "button" :on-click actions/send-comment} "Submit"]]]))

(defn timeline->seconds
  "returns time in seconds from pixel position of timeline"
  [px]
  (let [duration (@state/store :duration)]
   (* duration (/ px 640))))

(defn annotation-marker
  [{:keys [time comment]}]
  [:div.annotation-marker
   {:key (str time comment)
    :style {:left (str (seconds->timeline time) "px")}}])

(defn on-click-timeline
  [e]
  (let [px (.-offsetX (.-nativeEvent e))]
    (.seekTo js/player (timeline->seconds px))))

(defn timeline []
  [:div.timeline
    {:on-click on-click-timeline}
   [:div.current-time
    {:style {:left (seconds->timeline (@state/store :time))}}]
   (doall (map annotation-marker (@state/store :annotations)))])

(defn video-page []
  [:div.wrapper
   [:h1.title (@state/store :time)]
   [:h2 (@state/store :duration)]
   [:div#video]
   [timeline]
   [add-form]
   [add-button]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @state/shown-annotations)]]])