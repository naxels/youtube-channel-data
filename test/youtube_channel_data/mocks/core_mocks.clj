(ns youtube-channel-data.mocks.core-mocks
  (:require [clojure.test :refer :all]
            [youtube-channel-data.youtube.url :as yt-url]))

;; Telepurte video
(def tp-vid-id-url (yt-url/videos {:part "contentDetails,snippet" :id "1DQ0j_9Pq-g"}))
;; NASA video
(def nasa-vid-id-url (yt-url/videos {:part "contentDetails,snippet" :id "ntkpK9gwEU4"}))
;; Telepurte channel
(def tp-chan-id-url (yt-url/channels {:part "contentDetails,brandingSettings" :id "UCkDtCKtPKlsg-gJO_m5D0mQ"}))
;; NASA channel
(def nasa-chan-id-url (yt-url/channels {:part "contentDetails,brandingSettings" :id "UCLA_DiR1FfKNvjuUpBHmylQ"}))

(defn local-slurp
  [url]
  (cond
    (= url tp-vid-id-url) (slurp "./test/youtube_channel_data/mocks/tp_vid_id_result.json")
    (= url nasa-vid-id-url) (slurp "./test/youtube_channel_data/mocks/nasa_vid_id_result.json")
    (= url tp-chan-id-url) (slurp "./test/youtube_channel_data/mocks/tp_chan_id_result.json")
    (= url nasa-chan-id-url) (slurp "./test/youtube_channel_data/mocks/nasa_chan_id_result.json")
    :else "{}"))


