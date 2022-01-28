(ns youtube-channel-data.utils
  (:require [clojure.data.json :as json]
            [clojure.string :as str])
  (:import [java.time Duration ZonedDateTime]
           [java.net URLEncoder]))

(set! *warn-on-reflection* true)

; JSON
(defn json-value-reader
  [key value]
  (case key
    :duration (Duration/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html
    :publishedAt (ZonedDateTime/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/ZonedDateTime.html
    value))

(defn json->clj
  [value-fn-func]
  (fn
    [json-str]
    (json/read-str json-str
                   :value-fn value-fn-func ; each key/value will go through this function
                   :key-fn keyword)))

(def channel->clj (json->clj (fn [_k v] v)))

(def playlist->clj (json->clj json-value-reader))

(def video->clj (json->clj json-value-reader))

; Others
(defn parse-input
  "Use RegEx to try to get the video-id from the input or return input"
  [cli-arg]
  ;; This regex cuts out both before and what's after the video id, e.g. "?t=5".

  ;; re-find returns [whole-match group-1 group-2 ... group-n]. The video id is
  ;; in group-1, "([^\?&]+)" (read as "not a query delimiter"). There's only one group,
  ;; so the vector looks like [url video-id], so we destructure it.

  ;; This all fails if the user passes in a raw id, so check if the re finds
  ;; anything. If not, return the input.
  (let [[_ video-id :as url-match] (re-find #"(?:be\/|\?v=)([^\?&]+)[\?&]*" cli-arg)]
    (if url-match
      video-id
      cli-arg)))

; from https://stackoverflow.com/a/9745663
(defn query-params->query-string [m]
  (str/join "&"
            (for [[k ^String v] m]
              (str (name k) "="  (URLEncoder/encode v "UTF-8")))))

(defn seconds->minutes
  "Turn to minutes, rounded up or down based on seconds left"
  [seconds]
  (Math/round (/ seconds 60.0)))

; Partial
(defn title-match?
  [video-title-filter playlist-item]
  (-> playlist-item
      (get-in [:snippet :title] "")
      (str/lower-case)
      (str/includes? video-title-filter)))
