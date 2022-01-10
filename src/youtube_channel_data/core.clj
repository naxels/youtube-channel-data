(ns youtube-channel-data.core
  (:gen-class)
  (:require   [clojure.string :as str]
              [clojure.tools.cli :refer [parse-opts]]
              [youtube-channel-data.add :as add]
              [youtube-channel-data.output :as output]
              [youtube-channel-data.youtube-api :as yt]
              [youtube-channel-data.utilities :refer [notify notify-if]]))


(set! *warn-on-reflection* true)

(defn pull-yt-channel-data
  [data]
  (println "Reading Youtube using API-Key:" yt/config)
  (-> data
      (add/video-title-filter)
      (add/video-id)
      (notify "Video id found:" :video-id)
      (notify-if "Filtering titles on:" :video-title-filter :video-title-filter)
      (add/channel-id)
      (notify "Channel Id:" :channel-id)
      (add/playlist-id+channel-title)
      (notify "Playlist Id:" :playlist-id)
      (notify "Channel title:" :channel-title)
      (add/output-data)
      (notify "Getting all playlist items.....")
      (add/playlist-items)
      (notify "Playlist items found:" #(count (:playlist-items %)))
      (notify "Getting all videos for duration data.....")
      (add/videos-data)
      (add/transformed-playlist-items)
      (notify-if "Playlist items left after filtering:" #(count (:playlist-items-transformed %)) :video-title-filter)
      (output/to-file)
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
        (shutdown-agents) ; close futures thread pool used by pmap
        (println "All done, exiting")))))
