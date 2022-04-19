(ns youtube-channel-data.youtube.request
  "Make requests to Youtube Data API"
  (:require [clojure.string :as str]
            [youtube-channel-data.utils :as u]
            [youtube-channel-data.youtube.url :as yt-url]))

(declare consume-playlist-pages)

(defn video
  "Request videos API from Youtube using video-id(s) and import JSON
  we require an explicit *slurp* to assist in mocking from other functions"
  [video-id *slurp*]
  (-> (yt-url/videos {:part "contentDetails,snippet" :id video-id})
      (*slurp*)
      (u/video->clj)
      (:items)))

(defn channel
  "Request channels API from Youtube using channel-id and import JSON"
  [channel-id *slurp*]
  (-> (yt-url/channels {:part "contentDetails,brandingSettings" :id channel-id})
      (*slurp*)
      (u/channel->clj)
      (:items)))

(defn playlist-items
  "Request playlist-items API from Youtube using playlist-id and import JSON
   NOTE: API returns the playlist items based on position in the playlist"
  [playlist-id *slurp*]
  (as-> (yt-url/playlist-items {:part "snippet" :maxResults "50" :playlistId playlist-id}) pli
       (consume-playlist-pages pli *slurp*)
       (sequence cat pli)))

(defn playlist-items->videos
  "Parallel request of videos API from Youtube per 50 id's
   NOTE: API returns the videos based on the order of the id's

   Explicit slurp function to assist mocking."
  [playlist-items *slurp*]
  (->> playlist-items
       (map (comp :videoId :resourceId :snippet))
       (partition-all 50) ; partition per 50 id's
       (pmap
        (fn [ids] (video (str/join "," ids) *slurp*)))
       (sequence cat)))

(defn consume-playlist-pages
  "Get all playlist-items by using :nextPageToken until no more and import all JSON's.
  This will return a sequence of 50-item sequences. To enable parallel data processing,
  we don't concat until a later function."
  [playlist-url *slurp*]
  (iteration
    ;; Pull playlist page. Append any tokens to the playlist params, request it, then clj-ify it.
    (fn [page-token]
      (cond-> playlist-url
        page-token (str "&" (u/query-params->query-string {:pageToken page-token})) ; nil turns into ""; nothing will be appended if no page token.
        true (*slurp*)
        true (u/playlist->clj)))
    :kf :nextPageToken
    :vf :items))
