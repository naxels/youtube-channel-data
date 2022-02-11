(ns youtube-channel-data.core-test
  (:require [clojure.test :refer :all]
            [youtube-channel-data.core :as ytcd-core]
            [youtube-channel-data.mocks.core-mocks :as cm]))

(deftest video-id->channel-id-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (is (= (ytcd-core/video-id->channel-id "1DQ0j_9Pq-g") "UCkDtCKtPKlsg-gJO_m5D0mQ")
      "Given a Telepurte video id, this should return his channel ID.")
    (is (= (ytcd-core/video-id->channel-id "ntkpK9gwEU4") "UCLA_DiR1FfKNvjuUpBHmylQ")
      "Given a NASA video id, this should return NASA's channel ID.")
    (is (nil? (ytcd-core/video-id->channel-id "invalid-string"))
      "Invalid strings should return nil (for now).")))

(deftest channel-id->playlist-id+title-test
  (is (= (ytcd-core/channel-id->playlist-id+title "UCkDtCKtPKlsg-gJO_m5D0mQ")
        ["UUkDtCKtPKlsg-gJO_m5D0mQ" "Telepurte"])
    "Given Telepurte's channel id, this should return a vector of TP's main playlist ID and channel name.")
  (is (= (ytcd-core/channel-id->playlist-id+title "UCLA_DiR1FfKNvjuUpBHmylQ")
        ["UULA_DiR1FfKNvjuUpBHmylQ" "NASA"])
    "Given NASA's channel id, this should return a vector of NASA's main playlist ID and channel name.")
  (is (= (ytcd-core/channel-id->playlist-id+title "invalid-string")
        [nil nil])
    "Invalid strings should return a vector of nils (for now :( )"))
