(ns youtube-channel-data.output
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [csv-exporter.core :as csv]))

(set! *warn-on-reflection* true)

(def supported-formats ["csv", "json"])

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
  "Lookup the output option or return csv as default"
  [output-format]
  (or (some #{output-format} supported-formats)
      "csv"))

(defn writer
  [file extension to-write]
  (condp = extension
    "csv" (csv/write-csv-from-maps file to-write)
    "json" (with-open [writer (io/writer file)]
             (json/write to-write writer))))

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
