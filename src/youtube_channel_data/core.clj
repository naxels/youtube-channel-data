(ns youtube-channel-data.core
  (:gen-class)
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [youtube-channel-data.output :as output]
            [youtube-channel-data.utils :as u]
            [youtube-channel-data.youtube.request :as yt-request]))

(set! *warn-on-reflection* true)

(def ^:dynamic *slurp* slurp)

(defn video-id->channel-id
  "Takes youtube video id string and returns the channelId from youtube"
  [video-id]
  (->> (yt-request/video video-id *slurp*)
    (first)
    (:snippet)
    (:channelId)))

(defn channel-id->playlist-id+title
  "Takes channel id string and returns vec with [playlist-id, title]"
  [channel-id]
  (let [channel-item (->> (yt-request/channel channel-id *slurp*)
                       (first))]
    [(get-in channel-item [:contentDetails :relatedPlaylists :uploads])
     (get-in channel-item [:brandingSettings :channel :title])]))

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
  (let [output-map {:location  (output/location)
                    :filename  (output/filename video-title-filter channel-title)
                    :separator \.
                    :extension (output/extension (get-in data [:options :output]))}
        output (assoc output-map :file (apply str (vals output-map)))]
    (assoc data :output output)))

(defn add-playlist-items
  [{:keys [video-title-filter playlist-id] :as data}]
  (assoc data :playlist-items (cond->> (yt-request/playlist-items playlist-id *slurp*) ;enable mocking with dynamically bound *slurp*
                                video-title-filter (filter (partial u/title-match? video-title-filter)))))

(defn add-videos-data
  [{:keys [playlist-items] :as data}]
  (assoc data :videos (yt-request/playlist-items->videos playlist-items *slurp*)))

(defn playlist-items+videos->output-maps
  "Transform playlist-items & videos to output map"
  [playlist-items videos]
  (map output/output-map playlist-items videos))

(defn add-transformed-playlist-items
  [{:keys [playlist-items videos] :as data}]
  (assoc data :playlist-items-transformed (playlist-items+videos->output-maps playlist-items videos)))

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
    (add-videos-data)
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
  "Start the app. Key examples:

   options: {:output csv}, {:output csv, :filter \"twosday\"}
   arguments: [\"1DQ0j_9Pq-g\"]
   summary: \"The system cannot find the path specified...\""
  [& args]
  (let [{:keys [options arguments _errors summary]} (parse-opts args cli-options)]
    (if (empty? arguments)
      (println (usage summary))
      (do
        (pull-yt-channel-data {:id-or-url (first arguments) :options options})
        (shutdown-agents)                                   ; close futures thread pool used by pmap
        (println "All done, exiting")))))
