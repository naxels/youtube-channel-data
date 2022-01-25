(ns youtube-channel-data.youtube.url
  "Build Youtube Data API URL's"
  (:require [youtube-channel-data.utils :as u]
            [youtube-channel-data.youtube.config :as ytc]))

; Define url paths
(def base-url "https://www.googleapis.com")

; Use v3
(def sub-path "/youtube/v3/")

; Main API string builder closure
(defn api
  "Returns fn with route filled in.
   Ultimately Returns https string encoded ready for slurp"
  [route]
  (fn
    [query-params]
    (let [query-string (u/query-params->query-string (conj query-params ytc/api-key))]
      (str base-url sub-path route "?" query-string))))

; Caller helper methods
;; https://developers.google.com/youtube/v3/docs/videos/list
(def videos (api "videos"))

;; https://developers.google.com/youtube/v3/docs/channels/list
(def channels (api "channels"))

;; https://developers.google.com/youtube/v3/docs/playlistItems/list
(def playlist-items (api "playlistItems"))
