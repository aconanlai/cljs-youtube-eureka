(ns utils
  (:require
      [cljs.pprint :refer [cl-format]]))

(defn log
  [obj]
  (.log js/console obj))

(defn keywordize
  [hashmap]
  (into {} 
    (for [[k v] hashmap] 
      [(keyword k) v])))

(defn convert-timestamp
  [seconds]
  (str (int (/ seconds 60)) ":" (cl-format nil "~2,'0d" (mod seconds 60))))
