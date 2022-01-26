(ns youtube-channel-data.youtube.url-test
  (:require [clojure.test :refer [deftest is]]
            [youtube-channel-data.youtube.url :as yt-url]
            [youtube-channel-data.youtube.config :as ytc]))

; tests done in other deftests
(deftest test-base-url)
(deftest test-sub-path)

(deftest test-api
  (is (= true (fn? (yt-url/api "videos")))))

; ytc/read-key outputs nil in case the API key is not found.
; the test-query-params->query-string will return nothing as value when no val for key found in map, so this works for testing.
(deftest test-videos
  (is (= (str yt-url/base-url yt-url/sub-path "videos?part=snippet&key=" (ytc/read-key)) (yt-url/videos {:part "snippet"}))))

(deftest test-channels
  (is (= (str yt-url/base-url yt-url/sub-path "channels?part=contentDetails&key=" (ytc/read-key)) (yt-url/channels {:part "contentDetails"}))))

(deftest test-playlist-items
  (is (= (str yt-url/base-url yt-url/sub-path "playlistItems?part=snippet&maxResults=50&key=" (ytc/read-key)) (yt-url/playlist-items {:part "snippet" :maxResults "50"}))))
