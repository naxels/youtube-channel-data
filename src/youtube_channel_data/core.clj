(ns youtube-channel-data.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [youtube-channel-data.youtube-api :as yt]
            [csv-exporter.core :as csv]
            [clojure.java.io :as io]))

(declare seconds->minutes)
(declare consume-playlist-pages)

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

; Video key
(defn video-key
  "Turn video to vec of id, duration"
  [vid]
  [(:id vid) (seconds->minutes (.toSeconds (get-in vid [:contentDetails :duration])))]) ; in minutes, rounded

(defn json-value-reader
  [key value]
  (if (= key :publishedAt)
    (java.time.ZonedDateTime/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/ZonedDateTime.html
    value))
; Example of how to read: (.getYear (:publishedAt (playlist->json)))

(defn json-video-value-reader
  [key value]
  (if (= key :duration)
    (java.time.Duration/parse value) ; https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/time/Duration.html
    value))
; Example of how to read: (.toMinutes (:duration (video->json)))

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

(defn video-id->channel-id
  [video-id]
  (->> (yt/videos {:part "snippet" :id video-id})
       (slurp)
       (video->json)
       (:items)
       (first)
       (:snippet)
       (:channelId)))

(defn channel-id->playlist-id
  [channel-id]
  (->> (yt/channels {:part "contentDetails" :id channel-id})
       (slurp)
       (channel->json)
       (:items)
       (first)
       (:contentDetails)
       (:relatedPlaylists)
       (:uploads)))

(defn channel-id->title
  [channel-id]
  (->> (yt/channels {:part "brandingSettings" :id channel-id})
       (slurp)
       (channel->json)
       (:items)
       (first)
       (:brandingSettings)
       (:channel)
       (:title)))

(defn playlist-id->playlist-items
  [playlist-id]
  (->> (yt/playlist-items {:part "snippet" :maxResults "50" :playlistId playlist-id})
       (consume-playlist-pages)
       (flatten)))

; Utility
(defn seconds->minutes
  "Turn to minutes, rounded up or down based on seconds left"
  [seconds]
  (Math/round (/ seconds 60.0)))

(defn output-location
  "Output to output folder if exists, else to current location"
  [channel-title]
  (if (.exists (io/file "output"))
    (str "output" (java.io.File/separator) channel-title)
    (str channel-title)))

; Get all playlists by using all :nextPageToken until no more to fetch all json's
(defn consume-playlist-pages
  ([url]
   (let [api-str (slurp url)
         converted (playlist->json api-str)]
     (consume-playlist-pages (conj [] (:items converted)) url (:nextPageToken converted))))
  ([items-coll base-url page-token]
   (if page-token
     (let [api-str (slurp (str base-url "&pageToken=" page-token))
           converted (playlist->json api-str)]
       (recur (conj items-coll (:items converted)) base-url (:nextPageToken converted))) ; learned that the function call can be replaced with recur
     items-coll)))

; Get all videos for the id's
(defn consume-video-lists
  [base-url ids]
  (let [api-str (slurp (str base-url "&id=" (str/join "," ids)))
        converted (video->json api-str)]
    (:items converted)))

(defn transform-playlist-items
  "Get duration from videos, transform to output map"
  [playlist-items]
  (let [videos-data (->> playlist-items
                         (map (comp :videoId :resourceId :snippet))
                         (partition-all 50) ; partition per 50 id's
                         (map
                          #(consume-video-lists (yt/videos {:part "contentDetails"}) %)) ; get all video data for id's
                         (flatten))
        ; turn into lookup map
        ; {video-id, java Duration parsed}
        video-durations (into {} (map video-key videos-data))
        output-map (output-map-builder video-durations)]
    (map output-map playlist-items)))

(defn pull-yt-channel-data
  [id-or-url]
  (println "Usage: Input Video id or Full youtube URL")

  (println "Reading Youtube using API-Key:" yt/config)

  ; get video id from args
  (let [video-id (parse-input id-or-url)]
    (println "Video id found:" video-id)

    ; call video api with part=snippet and get channel id
    (let [channel-id (video-id->channel-id video-id)]
      (println "Channel Id:" channel-id)

      ; get playlist id from channel api & title from channel api
      (let [playlist-id (channel-id->playlist-id channel-id)
            channel-title (channel-id->title channel-id)
            output-location (output-location channel-title)
            ; output-location-edn (str output-location ".edn")
            output-location-csv (str output-location ".csv")]
        (println "Playlist Id:" playlist-id)
        (println "Channel title:" channel-title)

        (println "Getting all playlist items.....")
        ; get all playlistitems from playlistitems api
        (let [playlist-items (playlist-id->playlist-items playlist-id)]
          (println "Playlist items found:" (count playlist-items))

          (println "Getting all videos for duration data.....")
          (let [playlist-items-transformed (transform-playlist-items playlist-items)]
            ; (spit output-location-edn (prn-str playlist-items-transformed))
            (csv/write-csv-from-maps output-location-csv playlist-items-transformed)
            (println "Data saved to" output-location-csv)))))))

(defn -main
  ([]
   (do (println "Please enter a video id or YouTube video URL")
       (System/exit 0)))
  ([id-or-url]
   (pull-yt-channel-data id-or-url)))