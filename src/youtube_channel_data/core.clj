(ns youtube-channel-data.core
  (:gen-class)
  (:require   [clojure.string :as str]
              [clojure.tools.cli :refer [parse-opts]]
              [youtube-channel-data.output :as output]
              [youtube-channel-data.youtube-api :as yt]
              [youtube-channel-data.utilities :as u]))

(set! *warn-on-reflection* true)

(declare consume-playlist-pages)

; Video key
(defn video-key
  "Turn video to vec of id, duration"
  [vid]
  [(:id vid) (u/seconds->minutes (.getSeconds ^java.time.Duration (get-in vid [:contentDetails :duration])))]) ; in minutes, rounded

(defn video-id->channel-id
  [video-id]
  (->> (yt/videos {:part "snippet" :id video-id})
       (slurp)
       (u/video->json)
       (:items)
       (first)
       (:snippet)
       (:channelId)))

(defn channel-id->playlist-id+title
  "Returns vec with [playlist-id, title]"
  [channel-id]
  (let [channel-item (->> (yt/channels {:part "contentDetails,brandingSettings" :id channel-id})
       (slurp)
       (u/channel->json)
       (:items)
               (first))]
    [(get-in channel-item [:contentDetails :relatedPlaylists :uploads])
     (get-in channel-item [:brandingSettings :channel :title])]))

(defn playlist-id->playlist-items
  [playlist-id]
  (->> (yt/playlist-items {:part "snippet" :maxResults "50" :playlistId playlist-id})
       (consume-playlist-pages)
       ; Note: (apply concat) is faster than using conj / into while consuming
       (apply concat)))

; Get all playlists by using all :nextPageToken until no more to fetch all json's
; Has to be consumed sequentially since we need the nextPageToken from the result
(defn consume-playlist-pages
  ([url]
   (let [converted (-> url
                       (slurp)
                       (u/playlist->json))]
     (consume-playlist-pages (conj [] (:items converted)) url (:nextPageToken converted))))
  ([items-coll base-url page-token]
   (if page-token
     (let [converted (-> (str base-url "&" (u/query-params->query-string {:pageToken page-token}))
                         (slurp)
                         (u/playlist->json))]
       (recur (conj items-coll (:items converted)) base-url (:nextPageToken converted)))
     items-coll)))

; Get all videos for the id's
(defn consume-video-lists
  [base-url ids]
  (-> (str base-url "&" (u/query-params->query-string {:id (str/join "," ids)}))
      (slurp)
      (u/video->json)
      (:items)))

(defn transform-playlist-items
  "Get duration from videos, parallelly grab and transform to output map"
  [playlist-items]
  (let [videos-data (->> playlist-items
                         (map (comp :videoId :resourceId :snippet))
                         (partition-all 50) ; partition per 50 id's
                         (pmap
                          #(consume-video-lists (yt/videos {:part "contentDetails"}) %)) ; get all video data for id's
                         (apply concat))
        ; turn into lookup map
        ; {video-id, java Duration parsed}
        video-durations (into {} (map video-key videos-data))
        output-map (output/output-map-builder video-durations)]
    (shutdown-agents) ; close futures thread pool used by pmap
    (map output-map playlist-items)))

(defn add-video-title-filter
  [{{filter-option :filter} :options :as data}]
  (assoc data :video-title-filter (and filter-option
                                       (str/lower-case filter-option))))

(defn add-video-id
  [{:keys [id-or-url] :as data}]
  (assoc data :video-id (u/parse-input id-or-url)))

(defn add-channel-id
  [{:keys [video-id] :as data}]
  (assoc data :channel-id (video-id->channel-id video-id)))

(defn add-playlist-id+channel-title
  [{:keys [channel-id] :as data}]
  (let [[playlist-id channel-title] (channel-id->playlist-id+title channel-id)]
    (assoc data
           :playlist-id playlist-id
           :channel-title channel-title)))

(defn add-output-data
  [{:keys [video-title-filter channel-title] :as data}]
  (let [output-map {:location (output/location)
                    :filename (output/filename video-title-filter channel-title)
                    :separator \.
                    :extension (output/extension (get-in data [:options :output]))}
        output (assoc output-map :file (apply str (vals output-map)))]
    (assoc data :output output)))

(defn add-playlist-items
  [{:keys [playlist-id] :as data}]
  (assoc data :playlist-items (playlist-id->playlist-items playlist-id)))

(defn add-transformed-playlist-items
  [{:keys [video-title-filter playlist-items] :as data}]
  (let [title-match? (u/title-match-builder video-title-filter)]
    (assoc data :playlist-items-transformed (cond->> playlist-items
                                               video-title-filter (filter title-match?)
                                              true (transform-playlist-items)))))

(defn output-to-file
  [{:keys [output playlist-items-transformed] :as data}]
  (let [{:keys [file extension]} output]
    (output/writer file extension playlist-items-transformed))
  data)

(defn notify
  "2 arity: print and return data
   3 arity: print after applying f to data"
  ([data msg]
   (println msg)
   data)
  ([data msg f]
   (notify data (str msg " " (f data)))))

(defn notify-if
  [data msg f conditionalf]
  (if (conditionalf data)
    (notify data msg f)
    data))

(defn pull-yt-channel-data
  [data]
  (println "Reading Youtube using API-Key:" yt/config)
  (-> data
      (add-video-title-filter)
      (add-video-id)
      (notify "Video id found:" :video-id)
      (notify-if "Filtering titles on:" :video-title-filter :video-title-filter)
      (add-channel-id)
      (notify "Channel Id:" :channel-id)
      (add-playlist-id+channel-title)
      (notify "Playlist Id:" :playlist-id)
      (notify "Channel title:" :channel-title)
      (add-output-data)
      (notify "Getting all playlist items.....")
      (add-playlist-items)
      (notify "Playlist items found:" #(count (:playlist-items %)))
      (notify "Getting all videos for duration data.....")
      (add-transformed-playlist-items)
      (notify-if "Playlist items left after filtering:" #(count (:playlist-items-transformed %)) :video-title-filter)
      (output-to-file)
      (notify "Data saved to:" #(get-in % [:output :file]))))

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
   ["-o" "--output FORMAT" (format "Output formats supported: %s" (str/join ", " output/supported-formats))
    :default "csv"]])

(defn -main
  [& args]
  (let [{:keys [options arguments _errors summary]} (parse-opts args cli-options)]
    (if (empty? arguments)
      (println (usage summary))
      (do
        (pull-yt-channel-data {:id-or-url (first arguments) :options options})
        (println "All done, exiting")))))
