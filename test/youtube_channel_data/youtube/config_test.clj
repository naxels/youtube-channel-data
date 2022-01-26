(ns youtube-channel-data.youtube.config-test
  (:require [clojure.test :refer [deftest is]]
            [youtube-channel-data.youtube.config :as ytc]))

; tests done in other deftests
(deftest test-read-key)

(deftest test-api-key
  (is (= {:key (or (ytc/read-key) "")} (ytc/api-key))))