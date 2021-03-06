(ns youtube-channel-data.utils-test
  (:require [clojure.test :refer [deftest is]]
            [youtube-channel-data.utils :as u]))

(deftest test-json-value-reader
  (is (= :testval (u/json-value-reader :test :testval)))
  (let [zoned-date-time-val (u/json-value-reader :publishedAt "2021-11-22T14:34:20Z")]
    (is (= 2021 (.getYear zoned-date-time-val)))
    (is (= 11 (.getMonthValue zoned-date-time-val)))
    (is (= 22 (.getDayOfMonth zoned-date-time-val))))
  (let [duration-val (u/json-value-reader :duration "PT1H51M36S")]
    (is (= 6696 (.getSeconds duration-val)))
    ; Since JDK 9
    (is (= 6696 (.toSeconds duration-val)))))

(deftest test-json->clj
  (is (= true (fn? (u/json->clj u/json-value-reader))))
  (is (= {:wow "wow" :hey {:yo "hi"}} ((u/json->clj (fn [_ v] v)) "{\"wow\": \"wow\", \"hey\": {\"yo\": \"hi\"}}"))))

(deftest test-channel->clj
  (is (= {:a "b"} (u/channel->clj "{\"a\": \"b\"}"))))

; tests done in other deftests
(deftest test-playlist->clj)
(deftest test-video->clj)

(deftest test-parse-input
  ; using fake video id "monkey"
  (is (= "monkey" (u/parse-input "monkey")))
  (is (= "monkey" (u/parse-input "https://www.youtube.com/watch?v=monkey")))
  (is (= "monkey" (u/parse-input "https://www.youtube.com/watch?v=monkey&t=7")))
  (is (= "monkey" (u/parse-input "https://youtu.be/monkey")))
  (is (= "monkey" (u/parse-input "https://youtu.be/monkey?t=7"))))

(deftest test-query-params->query-string
  (is (= "" (u/query-params->query-string {})))
  (is (= "a=" (u/query-params->query-string {:a ""})))
  (is (= "a=b" (u/query-params->query-string {:a "b"})))
  (is (= "a=b&c=d" (u/query-params->query-string {:a "b" :c "d"})))
  (is (= "a=&c=d" (u/query-params->query-string {:a "" :c "d"}))))

(deftest test-seconds->minutes
  (is (= 1 (u/seconds->minutes 61)))
  (is (= 1 (u/seconds->minutes 89)))
  (is (= 2 (u/seconds->minutes 90))))

(deftest test-title-match?
  (let [title-match? (partial u/title-match? "monkey")]
    (is (= true (title-match? {:snippet {:title "MoNkEy island"}})))
    (is (= false (title-match? {:snippet {:title "Human island"}})))))