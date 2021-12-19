(ns youtube-channel-data.youtube-api-test
  (:require [clojure.test :refer [deftest is]]
            [youtube-channel-data.youtube-api :as yt]))

; tests done in other deftests
(deftest test-config)

(deftest test-api-key
  (is (= {:key (or yt/config "")} yt/api-key)))

; tests done in other deftests
(deftest test-base-url)
(deftest test-sub-path)

(deftest test-api
  (is (= true (fn? (yt/api "videos")))))

; yt/config outputs nil in case the API key is not found.
; the test-query-params->query-string will return nothing as value when no val for key found in map, so this works for testing.
(deftest test-videos
  (is (= (str yt/base-url yt/sub-path "videos?part=snippet&key=" yt/config) (yt/videos {:part "snippet"}))))

(deftest test-channels
  (is (= (str yt/base-url yt/sub-path "channels?part=contentDetails&key=" yt/config) (yt/channels {:part "contentDetails"}))))

(deftest test-playlist-items
  (is (= (str yt/base-url yt/sub-path "playlistItems?part=snippet&maxResults=50&key=" yt/config) (yt/playlist-items {:part "snippet" :maxResults "50"}))))
