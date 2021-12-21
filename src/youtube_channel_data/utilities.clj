(ns youtube-channel-data.utilities
  (:require [clojure.data.json :as json]
            [clojure.string :as str]))

(set! *warn-on-reflection* true)

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

(def channel->json (str->json (fn [_k v] v)))

(def playlist->json (str->json json-value-reader))

(def video->json (str->json json-video-value-reader))

(defn data->json
  [data]
  (json/write-str data))

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
              (str (name k) "="  (java.net.URLEncoder/encode v "UTF-8")))))

(defn seconds->minutes
  "Turn to minutes, rounded up or down based on seconds left"
  [seconds]
  (Math/round (/ seconds 60.0)))

(defn title-match-builder
  [video-title-filter]
  (fn [playlist-item] (-> playlist-item
                          (get-in [:snippet :title] "")
                          (str/lower-case)
                          (str/includes? video-title-filter))))
