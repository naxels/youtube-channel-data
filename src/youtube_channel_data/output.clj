(ns youtube-channel-data.output
  (:require   [clojure.java.io :as io]))

(set! *warn-on-reflection* true)

(def supported-formats ["csv"])

(defn location
  "Output to output folder if exists, else to current location"
  []
  (if (.exists (io/file "output"))
    (str "output" (java.io.File/separator))
    (str "")))

(defn filename
  [video-title-filter channel-title]
  (if video-title-filter
    (str channel-title "-" video-title-filter)
    channel-title))

(defn extension
  [output-format]
  (condp = output-format
    "csv" "csv"
    "json" "json"
    "csv"))

; Output map closure
(defn output-map-builder
  "Turn playlist item to map, adding video-duration through lookup"
  [video-durations-map]
  (fn [{v :snippet}]
    (let [video-id (get-in v [:resourceId :videoId])]
      {:video-id    video-id
       :video-url   (str "https://youtu.be/" video-id)
       :title       (:title v)
       :thumbnail   (get-in v [:thumbnails :default :url])
       :uploaded-at (.format (java.time.format.DateTimeFormatter/ISO_LOCAL_DATE) (:publishedAt v)) ; in yyyy-MM-dd string
       :duration    (video-durations-map video-id)}))) ; lookup duration from video-durations map
