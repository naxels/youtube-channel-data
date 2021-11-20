(ns youtube-channel-data.utilities
  (:require [clojure.data.json :as json]))

; JSON
(defn json-value-reader
  [key value]
  (if (= key :publishedAt)
    (java.time.ZonedDateTime/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/ZonedDateTime.html
    value))

(defn json-video-value-reader
  [key value]
  (if (= key :duration)
    (java.time.Duration/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html
    value))

(defn str->json
  [value-fn-func]
  (fn
    [json-str]
    (json/read-str json-str
                   :value-fn value-fn-func ; each key/value will go through this function
                   :key-fn keyword)))

(def channel->json (str->json json-value-reader))

(def playlist->json (str->json json-value-reader))

(def video->json (str->json json-video-value-reader))

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

(defn seconds->minutes
  "Turn to minutes, rounded up or down based on seconds left"
  [seconds]
  (Math/round (/ seconds 60.0)))