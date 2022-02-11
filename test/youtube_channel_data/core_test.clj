(ns youtube-channel-data.core-test
  (:require [clojure.test :refer :all]
            [youtube-channel-data.core :as ytcd-core]))

(deftest video-id->channel-id-test
  (is (= (ytcd-core/video-id->channel-id "1DQ0j_9Pq-g") "UCkDtCKtPKlsg-gJO_m5D0mQ"))
  (is (= (ytcd-core/video-id->channel-id "ntkpK9gwEU4") "UCLA_DiR1FfKNvjuUpBHmylQ"))
  (is (nil? (ytcd-core/video-id->channel-id "invalid-string"))))
