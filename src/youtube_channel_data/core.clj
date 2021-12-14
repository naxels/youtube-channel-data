(ns youtube-channel-data.core
  (:gen-class)
  (:require   [clojure.java.io :as io]
              [clojure.string :as str]
              [clojure.tools.cli :refer [parse-opts]]
              [csv-exporter.core :as csv]
              [youtube-channel-data.youtube-api :as yt]
              [youtube-channel-data.utilities :as u]))

(declare consume-playlist-pages)

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
  [(:id vid) (u/seconds->minutes (.getSeconds (get-in vid [:contentDetails :duration])))]) ; in minutes, rounded

(defn video-id->channel-id
  [video-id]
  (->> (yt/videos {:part "snippet" :id video-id})
       (slurp)
       (u/video->json)
       (:items)
       (first)
       (:snippet)
       (:channelId)))

(defn channel-id->playlist-id
  [channel-id]
  (->> (yt/channels {:part "contentDetails" :id channel-id})
       (slurp)
       (u/channel->json)
       (:items)
       (first)
       (:contentDetails)
       (:relatedPlaylists)
       (:uploads)))

(defn channel-id->title
  [channel-id]
  (->> (yt/channels {:part "brandingSettings" :id channel-id})
       (slurp)
       (u/channel->json)
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

(defn output-location
  "Output to output folder if exists, else to current location"
  [channel-title]
  (if (.exists (io/file "output"))
    (str "output" (java.io.File/separator) channel-title)
    (str channel-title)))

; Get all playlists by using all :nextPageToken until no more to fetch all json's
; Has to be consumed sequentially since we need the nextPageToken from the result
(defn consume-playlist-pages
  ([url]
   (let [api-str (slurp url)
         converted (u/playlist->json api-str)]
     (consume-playlist-pages (conj [] (:items converted)) url (:nextPageToken converted))))
  ([items-coll base-url page-token]
   (if page-token
     (let [api-str (slurp (str base-url "&pageToken=" page-token))
           converted (u/playlist->json api-str)]
       (recur (conj items-coll (:items converted)) base-url (:nextPageToken converted))) ; learned that the function call can be replaced with recur
     items-coll)))

; Get all videos for the id's
(defn consume-video-lists
  [base-url ids]
  (let [api-str (slurp (str base-url "&id=" (str/join "," ids)))
        converted (u/video->json api-str)]
    (:items converted)))

(defn transform-playlist-items
  "Get duration from videos, parallelly grab and transform to output map"
  [playlist-items]
  (let [videos-data (->> playlist-items
                         (map (comp :videoId :resourceId :snippet))
                         (partition-all 50) ; partition per 50 id's
                         (pmap
                          #(consume-video-lists (yt/videos {:part "contentDetails"}) %)) ; get all video data for id's
                         (flatten))
        ; turn into lookup map
        ; {video-id, java Duration parsed}
        video-durations (into {} (map video-key videos-data))
        output-map (output-map-builder video-durations)]
    (shutdown-agents) ; close futures thread pool used by pmap
    (map output-map playlist-items)))

(defn pull-yt-channel-data
  [id-or-url options]
  (let [filter-option (:filter options)
        video-title-filter (if filter-option
                             (str/lower-case filter-option)
                             nil)]
    (println "Reading Youtube using API-Key:" yt/config)

    ; get video id from args
    (let [video-id (u/parse-input id-or-url)]
      (println "Video id found:" video-id)

      ; call video api with part=snippet and get channel id
      (let [channel-id (video-id->channel-id video-id)]
        (println "Channel Id:" channel-id)

        ; get playlist id from channel api & title from channel api
        (let [playlist-id (channel-id->playlist-id channel-id)
              channel-title (channel-id->title channel-id)
              ; add filter to output filename if set
              output-location (output-location (if video-title-filter
                                                 (str channel-title "-" video-title-filter)
                                                 channel-title))
              ; output-location-edn (str output-location ".edn")
              output-location-csv (str output-location ".csv")]
          (println "Playlist Id:" playlist-id)
          (println "Channel title:" channel-title)

          (println "Getting all playlist items.....")
          ; get all playlistitems from playlistitems api
          (let [playlist-items (playlist-id->playlist-items playlist-id)]
            (println "Playlist items found:" (count playlist-items))

            (when video-title-filter
              (println "Filtering titles on:" video-title-filter))

            (println "Getting all videos for duration data.....")
            ; filter the values on video-title-filter if truthy
            (let [title-match? (u/title-match-builder video-title-filter)
                  playlist-items-transformed (cond->> playlist-items
                                               video-title-filter (filter title-match?)
                                               true (transform-playlist-items))]
              ; (spit output-location-edn (prn-str playlist-items-transformed))
              (csv/write-csv-from-maps output-location-csv playlist-items-transformed)
              (println "Data saved to" output-location-csv))))))))

; CLI
(defn usage [options-summary]
  (->> ["Youtube channel data"
        ""
        "Usage: youtube-channel-data [options] video-id/url"
        ""
        "Options:"
        options-summary
        ""
        "Video id/url:"
        "Can be just the video id, full Youtube url or short Youtu.be url"]
       (str/join \newline)))

(def cli-options
  [["-f" "--filter SEARCHQUERY" "Search query to filter the channel video's on"]
   ["-o" "--output FORMAT" "Output formats supported: csv"
    :default "csv"]])

(defn -main
  [& args]
  (let [{:keys [options arguments _errors summary]} (parse-opts args cli-options)]
    (if (empty? arguments)
      (println (usage summary))
      (do
        (pull-yt-channel-data (first arguments) options)
        (println "All done, exiting")))))