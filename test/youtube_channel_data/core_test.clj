(ns youtube-channel-data.core-test
  (:require [clojure.test :refer :all]
            [youtube-channel-data.core :as ytcd-core]
            [youtube-channel-data.youtube.url :as yt-url]
            [youtube-channel-data.mocks.core-mocks :as cm]
            [youtube-channel-data.resources.core-test :refer [tp-playlist-item-augmented-data]]))



(deftest video-id->channel-id-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (is (= (ytcd-core/video-id->channel-id "1DQ0j_9Pq-g") "UCkDtCKtPKlsg-gJO_m5D0mQ")
      "Given a Telepurte video id, this should return his channel ID.")
    (is (= (ytcd-core/video-id->channel-id "ntkpK9gwEU4") "UCLA_DiR1FfKNvjuUpBHmylQ")
      "Given a NASA video id, this should return NASA's channel ID.")
    (is (nil? (ytcd-core/video-id->channel-id "invalid-string"))
      "Invalid strings should return nil (for now).")))

(deftest channel-id->playlist-id+title-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (is (= (ytcd-core/channel-id->playlist-id+title "UCkDtCKtPKlsg-gJO_m5D0mQ")
          ["UUkDtCKtPKlsg-gJO_m5D0mQ" "Telepurte"])
      "Given Telepurte's channel id, this should return a vector of TP's main playlist ID and channel name.")
    (is (= (ytcd-core/channel-id->playlist-id+title "UCLA_DiR1FfKNvjuUpBHmylQ")
          ["UULA_DiR1FfKNvjuUpBHmylQ" "NASA"])
      "Given NASA's channel id, this should return a vector of NASA's main playlist ID and channel name.")
    (is (= (ytcd-core/channel-id->playlist-id+title "invalid-string")
          [nil nil])
      "Invalid strings should return a vector of nils (for now :( )")))

(deftest add-video-title-filter-test
  (is (= (ytcd-core/add-video-title-filter {:id-or-url "1DQ0j_9Pq-g"
                                            :options   {:filter "twosday"}})
        {:id-or-url          "1DQ0j_9Pq-g"
         :options            {:filter "twosday"}
         :video-title-filter "twosday"})
    "Given a filter option, the same hashmap should return with a :video-title-filter key"))

(deftest add-video-id-test
  (is (= (ytcd-core/add-video-id {:id-or-url          "1DQ0j_9Pq-g"
                                  :options            {:filter "twosday"}
                                  :video-title-filter "twosday"})
        {:id-or-url          "1DQ0j_9Pq-g"
         :options            {:filter "twosday"}
         :video-title-filter "twosday"
         :video-id           "1DQ0j_9Pq-g"})
    "Given a video id, should return a copied hashmap with the id with a :video-id key")
  (is (= (ytcd-core/add-video-id {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
                                  :options            {:filter "twosday"}
                                  :video-title-filter "twosday"})
        {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
         :options            {:filter "twosday"}
         :video-title-filter "twosday"
         :video-id           "1DQ0j_9Pq-g"})
    "Given a video url, should return a copied hashmap with the id with a :video-id key"))

(deftest add-channel-id-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (is (= (ytcd-core/add-channel-id {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
                                      :options            {:filter "twosday"}
                                      :video-title-filter "twosday"
                                      :video-id           "1DQ0j_9Pq-g"})
          {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
           :options            {:filter "twosday"}
           :video-title-filter "twosday"
           :video-id           "1DQ0j_9Pq-g"
           :channel-id         "UCkDtCKtPKlsg-gJO_m5D0mQ"}))))

;TODO: Double-check add-playlist-id+channel-title-test
(deftest add-playlist-id+channel-title-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (is (= (ytcd-core/add-playlist-id+channel-title {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
                                                     :options            {:filter "twosday"}
                                                     :video-title-filter "twosday"
                                                     :video-id           "1DQ0j_9Pq-g"
                                                     :channel-id         "UCkDtCKtPKlsg-gJO_m5D0mQ"})
          {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
           :options            {:filter "twosday"}
           :video-title-filter "twosday"
           :video-id           "1DQ0j_9Pq-g"
           :channel-id         "UCkDtCKtPKlsg-gJO_m5D0mQ"
           :playlist-id        "UUkDtCKtPKlsg-gJO_m5D0mQ"
           :channel-title      "Telepurte"}))))

;TODO: Double-check add-output-data-test on Linux/MacOS.
(deftest add-output-data-test
  (is (= (ytcd-core/add-output-data {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g"
                                     :options            {:filter "twosday"}
                                     :video-title-filter "twosday"
                                     :video-id           "1DQ0j_9Pq-g"
                                     :channel-id         "UCkDtCKtPKlsg-gJO_m5D0mQ"
                                     :playlist-id        "UUkDtCKtPKlsg-gJO_m5D0mQ"
                                     :channel-title      "Telepurte"})
        {:id-or-url "youtube.com/watch?v=1DQ0j_9Pq-g",
         :options {:filter "twosday"},
         :video-title-filter "twosday",
         :video-id "1DQ0j_9Pq-g",
         :channel-id "UCkDtCKtPKlsg-gJO_m5D0mQ",
         :playlist-id "UUkDtCKtPKlsg-gJO_m5D0mQ",
         :channel-title "Telepurte",
         :output {:location "output\\",
                  :filename "Telepurte-twosday",
                  :separator \.,
                  :extension "csv",
                  :file "output\\Telepurte-twosday.csv"}})))

; Big Yoshi test here. How to mock it.
; #object[java.time.ZonedDateTime 0x3d3c8b50 "2022-02-23T01:06:37Z"] causing errors. I want to stringify it.

; add-playlist-items uses consume-pages which eventually creates urls with page tokens which differ every time.
; gonna mock consume-playlist-pages.
(deftest add-playlist-items-test
  (binding [ytcd-core/*slurp* cm/local-slurp]
    (let [tp-data {:id-or-url          "youtube.com/watch?v=1DQ0j_9Pq-g",
                   :options            {:filter "twosday"},
                   :video-title-filter "twosday",
                   :video-id           "1DQ0j_9Pq-g",
                   :channel-id         "UCkDtCKtPKlsg-gJO_m5D0mQ",
                   :playlist-id        "UUkDtCKtPKlsg-gJO_m5D0mQ",
                   :channel-title      "Telepurte",
                   :output             {:location  "output\\",
                                        :filename  "Telepurte-twosday",
                                        :separator \.,
                                        :extension "csv",
                                        :file      "output\\Telepurte-twosday.csv"}}
          ;; Long variable names, but hopefully conveys the point.
          ;stringify-published-at-in-snippet (fn [hm] (map #(update-in % [:snippet :publishedAt] (fn [d] (.toString d))) hm))
          ;stringify-published-at #(update % :playlist-items stringify-published-at-in-snippet)
          ;str-fied (-> (ytcd-core/add-playlist-items tp-data) (stringify-published-at))
          pli-data (ytcd-core/add-playlist-items tp-data)
          {:keys [act-playlist-items]} pli-data
          {:keys [exp-playlist-items]} tp-playlist-item-augmented-data]

      ; A request with a nextPageToken
      ; A request without it
      ; Should exist in the mock.
      ;(println (ytcd-core/add-playlist-items tp-data))
      ;(println "Hewwo")
      ;(let [str-fied (-> (ytcd-core/add-playlist-items tp-data) (stringify-published-at))
      ;      {:keys [playlist-items]} str-fied
      ;      [pli] playlist-items
      ;      {{publishedAt :publishedAt} :snippet} pli]
      ;  (println publishedAt))
      ;(let [{:keys [playlist-items]} tp-playlist-item-augmented-data
      ;      [pli]  playlist-items
      ;      {{publishedAt :publishedAt} :snippet} pli]
      ;  (println publishedAt))
      #_(is (= (-> (ytcd-core/add-playlist-items tp-data) (stringify-published-at))
              tp-playlist-item-augmented-data))
      ; this doesn't feel comprehensive.
      (is (= (count act-playlist-items)
            (count exp-playlist-items))))))
