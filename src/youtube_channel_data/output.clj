(ns youtube-channel-data.output
  (:require [clojure.data.json :as json]
            [clojure.java.io :as io]
            [youtube-channel-data.utilities :as u]
            [csv-exporter.core :as csv]))

(set! *warn-on-reflection* true)

(def supported-formats ["csv", "edn", "json"])

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
    ; edn: need to print to string first
    "edn" (with-open [writer (io/writer file)]
            (.write writer (pr-str to-write)))
    "json" (with-open [writer (io/writer file)]
             (json/write to-write writer))))

;; ; Output map closure
;; (defn output-map-builder
;;   [video-durations-map]
;;   (fn [{v :snippet}]
;;     (let [video-id (get-in v [:resourceId :videoId])]
;;       {})))

; Output map
(defn output-map
  [{playlist-item :snippet} {vid-cd :contentDetails vid-s :snippet}]
  (let [video-id (get-in playlist-item [:resourceId :videoId])]
    {; Playlist-item
     :video-id     video-id
     :video-url    (str "https://youtu.be/" video-id)
     ; Playlist-item
     :title        (:title playlist-item)
     ; Playlist-item
     :thumbnail    (get-in playlist-item [:thumbnails :default :url])
     ; Playlist-item
     :uploaded-at  (.format (java.time.format.DateTimeFormatter/ISO_LOCAL_DATE) (:publishedAt playlist-item))
     ; Video
     :published-at (.format (java.time.format.DateTimeFormatter/ISO_LOCAL_DATE) (:publishedAt vid-s))
     ; Video
     :duration
     (->> :duration ^java.time.Duration (vid-cd) (.getSeconds) (u/seconds->minutes))}))

(defn to-file
  [{:keys [output playlist-items-transformed] :as data}]
  (let [{:keys [file extension]} output]
    (writer file extension playlist-items-transformed))
  data)