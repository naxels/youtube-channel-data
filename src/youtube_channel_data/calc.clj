(ns youtube-channel-data.calc
  (:require [clojure.string :as str]
            [youtube-channel-data.output :as output]
            [youtube-channel-data.youtube-api :as yt]
            [youtube-channel-data.utilities :as u]))

(declare consume-playlist-pages)
(declare consume-video-lists)

(defn video-id->channel-id
  [video-id]
  (->> {:part "snippet" :id video-id}
       (yt/videos)
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
  "NOTE: API returns the playlist items based on position in the playlist"
  [playlist-id]
  (->> {:part "snippet" :maxResults "50" :playlistId playlist-id}
       (yt/playlist-items)
       (consume-playlist-pages)
       ; Note: (apply concat) is faster than using conj / into while consuming
       (apply concat)))

(defn playlist-items->videos
  "Parallel grab of videos
   NOTE: API returns the videos based on the order of the id's"
  [playlist-items]
  (->> playlist-items
       (map (comp :videoId :resourceId :snippet))
       (partition-all 50) ; partition per 50 id's
       (pmap
         #(consume-video-lists (yt/videos {:part "contentDetails,snippet"}) %)) ; get all video data for id's
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
  "Transform playlist-items & videos to output map"
  [playlist-items videos]
  (map output/output-map playlist-items videos))