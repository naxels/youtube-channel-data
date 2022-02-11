(ns youtube-channel-data.core-test
  (:require [clojure.test :refer :all]
            [youtube-channel-data.core :as ytcd-core]))

(deftest video-id->channel-id-test
  (is (= (ytcd-core/video-id->channel-id "1DQ0j_9Pq-g") "UCkDtCKtPKlsg-gJO_m5D0mQ"))
  (is (= (ytcd-core/video-id->channel-id "ntkpK9gwEU4") "UCLA_DiR1FfKNvjuUpBHmylQ"))
  (is (nil? (ytcd-core/video-id->channel-id "invalid-string"))))

(deftest channel-id->playlist-id+title-test
  (is (= (ytcd-core/channel-id->playlist-id+title "UCkDtCKtPKlsg-gJO_m5D0mQ")
        ["UUkDtCKtPKlsg-gJO_m5D0mQ" "Telepurte"]))
  (is (= (ytcd-core/channel-id->playlist-id+title "UCLA_DiR1FfKNvjuUpBHmylQ")
        ["UULA_DiR1FfKNvjuUpBHmylQ" "NASA"]))
  (is (= (ytcd-core/channel-id->playlist-id+title "invalid-string")
        [nil nil])))
