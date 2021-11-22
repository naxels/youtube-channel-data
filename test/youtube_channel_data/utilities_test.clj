(ns youtube-channel-data.utilities-test
  (:require [clojure.test :refer [deftest is]]
            [youtube-channel-data.utilities :as u]))

(deftest test-json-value-reader
  (is (= :testval (u/json-value-reader :test :testval)))
  (let [zoned-date-time-val (u/json-value-reader :publishedAt "2021-11-22T14:34:20Z")]
    (is (= 2021 (.getYear zoned-date-time-val)))
    (is (= 11 (.getMonthValue zoned-date-time-val)))
    (is (= 22 (.getDayOfMonth zoned-date-time-val)))))

(deftest test-json-video-value-reader
  (is (= :testval (u/json-video-value-reader :test :testval)))
  (let [duration-val (u/json-video-value-reader :duration "PT1H51M36S")]
    (is (= 6696 (.getSeconds duration-val)))
    ; Since JDK 9
    (is (= 6696 (.toSeconds duration-val)))))

(deftest test-str->json
  (is (= true (fn? (u/str->json u/json-value-reader)))))

(deftest test-channel->json
  (is (= {:a "b"} (u/channel->json "{\"a\": \"b\"}"))))

; tests done in other deftests
(deftest test-playlist->json)
(deftest test-video->json)

(deftest test-parse-input
  ; using fake video id "monkey"
  (is (= "monkey" (u/parse-input "monkey")))
  (is (= "monkey" (u/parse-input "https://www.youtube.com/watch?v=monkey")))
  (is (= "monkey" (u/parse-input "https://www.youtube.com/watch?v=monkey&t=7")))
  (is (= "monkey" (u/parse-input "https://youtu.be/monkey")))
  (is (= "monkey" (u/parse-input "https://youtu.be/monkey?t=7"))))

(deftest test-seconds->minutes
  (is (= 1 (u/seconds->minutes 61)))
  (is (= 1 (u/seconds->minutes 89)))
  (is (= 2 (u/seconds->minutes 90))))