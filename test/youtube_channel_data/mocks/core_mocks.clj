(ns youtube-channel-data.mocks.core-mocks
  (:require [youtube-channel-data.youtube.url :as yt-url]))

;; Telepurte video
(def tp-vid-id-url (yt-url/videos {:part "contentDetails,snippet" :id "1DQ0j_9Pq-g"}))
;; NASA video
(def nasa-vid-id-url (yt-url/videos {:part "contentDetails,snippet" :id "ntkpK9gwEU4"}))
;; Telepurte channel
(def tp-chan-id-url (yt-url/channels {:part "contentDetails,brandingSettings" :id "UCkDtCKtPKlsg-gJO_m5D0mQ"}))
;; NASA channel
(def nasa-chan-id-url (yt-url/channels {:part "contentDetails,brandingSettings" :id "UCLA_DiR1FfKNvjuUpBHmylQ"}))
;; Telepurte playlist
(def tp-pl-id-url (yt-url/playlist-items {:part "snippet" :maxResults "50" :playlistId "UUkDtCKtPKlsg-gJO_m5D0mQ"}))
#_(def tp-pl-id-np-url (yt-url/playlist-items {:part "snippet" :maxResults "50" :playlistId "UUkDtCKtPKlsg-gJO_m5D0mQ"
                                               :nextPageToken "EAAaBlBUOkNESQ"}))
(def tp-pl-id-np-url "https://www.googleapis.com/youtube/v3/playlistItems?part=snippet&maxResults=50&playlistId=UUkDtCKtPKlsg-gJO_m5D0mQ&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w&pageToken=EAAaBlBUOkNESQ")
(def tp-vid-ids-from-pl-items (yt-url/videos {:part "contentDetails,snippet" :id "rZ5ZjG6-crQ,zNZZJQZchWc"}))

(def url-map {tp-vid-id-url (slurp "./test/youtube_channel_data/mocks/tp_vid_id_result.json")
              nasa-vid-id-url (slurp "./test/youtube_channel_data/mocks/nasa_vid_id_result.json")
              tp-chan-id-url (slurp "./test/youtube_channel_data/mocks/tp_chan_id_result.json")
              nasa-chan-id-url (slurp "./test/youtube_channel_data/mocks/nasa_chan_id_result.json")
              tp-pl-id-url (slurp "./test/youtube_channel_data/mocks/tp_pl_items_result.json")
              tp-pl-id-np-url (slurp "./test/youtube_channel_data/mocks/tp_pl_items_result_2.json")
              tp-vid-ids-from-pl-items (slurp "./test/youtube_channel_data/mocks/tp_vid_ids_from_pl_items_result.json")})


(defn local-slurp
  [url]
  (get url-map url "{}"))






