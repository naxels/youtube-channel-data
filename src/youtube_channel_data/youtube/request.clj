(ns youtube-channel-data.youtube.request
  "Make requests to Youtube Data API"
  (:require [clojure.string :as str]
            [youtube-channel-data.utils :as u]
            [youtube-channel-data.youtube.url :as yt-url]))

(declare consume-playlist-pages)
(declare consume-video-lists)

(defn video
  "Request videos API from Youtube using video-id(s) and import JSON"
  [video-id]
  (-> (yt-url/videos {:part "contentDetails,snippet" :id video-id})
      (slurp)
      (u/video->clj)
      (:items)))

(defn channel
  "Request channels API from Youtube using channel-id and import JSON"
  [channel-id]
  (-> (yt-url/channels {:part "contentDetails,brandingSettings" :id channel-id})
      (slurp)
      (u/channel->clj)
      (:items)))

(defn playlist-items
  "Request playlist-items API from Youtube using playlist-id and import JSON
   NOTE: API returns the playlist items based on position in the playlist"
  [playlist-id]
  (->> (yt-url/playlist-items {:part "snippet" :maxResults "50" :playlistId playlist-id})
       (consume-playlist-pages)
       ; Note: (apply concat) is faster than using conj / into while consuming
       (apply concat)))

(defn playlist-items->videos
  "Parallel request of videos API from Youtube per 50 id's
   NOTE: API returns the videos based on the order of the id's"
  [playlist-items]
  (->> playlist-items
       (map (comp :videoId :resourceId :snippet))
       (partition-all 50) ; partition per 50 id's
       (pmap
        (fn [ids] (video (str/join "," ids))))
       (apply concat)))

(defn consume-playlist-pages
  "Get all playlist-items by using :nextPageToken until no more and import all JSON's
   Has to be consumed sequentially since we need the nextPageToken from the result"
  ([url]
   (let [converted (-> url
                       (slurp)
                       (u/playlist->clj))]
     (consume-playlist-pages (conj [] (:items converted)) url (:nextPageToken converted))))
  ([items-coll base-url page-token]
   (if page-token
     (let [converted (-> (str base-url "&" (u/query-params->query-string {:pageToken page-token}))
                         (slurp)
                         (u/playlist->clj))]
       (recur (conj items-coll (:items converted)) base-url (:nextPageToken converted)))
     items-coll)))
