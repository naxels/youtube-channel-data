(ns youtube-channel-data.mocks.core-mocks
  (:require [clojure.test :refer :all]))

;; Big file incoming :(

;; Telepurte, from (slurp (videos {:part "contentDetails,snippet" :id "1DQ0j_9Pq-g"}))
(def tp-vid-id-url "https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=1DQ0j_9Pq-g&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")
;; NASA, from (slurp (videos {:part "contentDetails,snippet" :id "ntkpK9gwEU4"}))
(def nasa-vid-id-url "https://www.googleapis.com/youtube/v3/videos?part=contentDetails%2Csnippet&id=ntkpK9gwEU4&key=AIzaSyAvq_RD5Kl_ggdWDAN_Anmh5n-4F0qvZ8w")


(defn local-slurp
  [url]
  (cond
    (= url tp-vid-id-url) (slurp "./test/youtube_channel_data/mocks/tp_vid_id_result.json")
    (= url nasa-vid-id-url) (slurp "./test/youtube_channel_data/mocks/nasa_vid_id_result.json")
    :else "{}"))


