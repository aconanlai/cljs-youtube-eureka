(ns pages.video
  (:require [reagent.core :as reagent]
    [components.loader]
    [utils]
    [actions]
    [state]))

(def css-transition-group
  (reagent/adapt-react-class js/React.addons.CSSTransitionGroup))

(defn seconds->timeline
  "returns pixel position of marker based on duration of video"
  [time]
  (let [{:keys [duration video-width]} @state/store]
  
    (if (= duration 0)
     0
     (* video-width (/ time duration)))))
  
(defn open-form
  []
  (do
    (.pauseVideo js/player)
    (swap! state/store update-in [:form-open] #(not %))))

(defn annotation-panel [annotation]
  (let [{time :time comment :comment} annotation]
   [:div.annotations-container__annotation {:key (str time comment)}
    [:span.annotations-container__time (utils/convert-timestamp time) ":"] 
    [:span.annotations-container__comment comment]]))

(defn add-button []
  (if (not (@state/store :form-open))
    [:button {:on-click open-form} "Add Annotation"]))

(defn add-form []
  (if (@state/store :form-open)
      [:form
       [:textarea {:id "comment" :name "comment" :value (@state/store :comment)
                   :on-change #(swap! state/store assoc-in [:comment] (-> % .-target .-value))}]
       [:div
        [:button {
                  :type "button"
                  :on-click actions/send-comment
                  :disabled (= (@state/store :comment) "")}
                 "Submit"]]]))

(defn timeline->seconds
  "returns time in seconds from pixel position of timeline"
  [px]
  (let [{:keys [duration video-width]} @state/store]
   (* duration (/ px video-width))))

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
   (doall (map annotation-marker @state/all-annotations))])

(defn related-container []
  (if (< 0 (count (@state/store :related)))
      [:div.related-videos
       [:h2.related-videos__title "Related Videos"]
       [:h2 (:title (:snippet (get (@state/store :related) 1)))]
       [:div.related-videos__columns
        [:div.related-videos__video]
        [:div.related-videos__video]]]))

(defn video-page []
  [:div.wrapper
   [components.loader/loader-form]
   [:div#video]
   [timeline]
   [:h2 (utils/convert-timestamp (@state/store :time))]
   [add-form]
   [add-button]
   [:div.annotations-container
    [css-transition-group {:transition-name "annotation"}
     (map annotation-panel @state/shown-annotations)]]
   [related-container]])
 
