(ns youtube-channel-data.mocks.core-mocks
  (:require [clojure.test :refer :all]))

;; Telepurte, from (videos {:part "contentDetails,snippet" :id "1DQ0j_9Pq-g"})
(def tp-vid-id-url "https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=1DQ0j_9Pq-g&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")
;; NASA, from (videos {:part "contentDetails,snippet" :id "ntkpK9gwEU4"})
(def nasa-vid-id-url "https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=ntkpK9gwEU4&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")
;; Telepurte, from (channels {:part "contentDetails,brandingSettings" :id "UCkDtCKtPKlsg-gJO_m5D0mQ"})
(def tp-chan-id-url "https://www.googleapis.com/youtube/v3/channels?part=contentDetails%2CbrandingSettings&id=UCkDtCKtPKlsg-gJO_m5D0mQ&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")
;; NASA, from (channels {:part "contentDetails,brandingSettings" :id "UCLA_DiR1FfKNvjuUpBHmylQ"})
(def nasa-chan-id-url "https://www.googleapis.com/youtube/v3/channels?part=contentDetails%2CbrandingSettings&id=UCLA_DiR1FfKNvjuUpBHmylQ&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")

(defn local-slurp
  [url]
  (cond
    (= url tp-vid-id-url) (slurp "./test/youtube_channel_data/mocks/tp_vid_id_result.json")
    (= url nasa-vid-id-url) (slurp "./test/youtube_channel_data/mocks/nasa_vid_id_result.json")
    (= url tp-chan-id-url) (slurp "./test/youtube_channel_data/mocks/tp_chan_id_result.json")
    (= url nasa-chan-id-url) (slurp "./test/youtube_channel_data/mocks/nasa_chan_id_result.json")
    :else "{}"))


