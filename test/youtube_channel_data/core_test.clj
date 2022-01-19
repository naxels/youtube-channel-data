(ns youtube-channel-data.core-test
  (:require [clojure.test :refer :all]
            [youtube-channel-data.core :refer [video-id->channel-id]]))

(deftest test-yt-id-pull
  (is (= (video-id->channel-id "uUU3jW7Y9Ak") "UCFhXFikryT4aFcLkLw2LBLA")))