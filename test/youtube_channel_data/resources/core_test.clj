(ns youtube-channel-data.resources.core-test)

; (-> data (add-playlist-items) (stringify-published-at))

(def tp-playlist-item-augmented-data
  {:playlist-id "UUkDtCKtPKlsg-gJO_m5D0mQ",
   :channel-title "Telepurte",
   :video-id "1DQ0j_9Pq-g",
   :output {:location "output\\",
            :filename "Telepurte-twosday",
            :separator \.,
            :extension "csv",
            :file "output\\Telepurte-twosday.csv"},
   :video-title-filter "twosday",
   :channel-id "UCkDtCKtPKlsg-gJO_m5D0mQ",
   :options {:filter "twosday"},
   :playlist-items '({:kind "youtube#playlistItem",
                      :etag "y61JlbipF2OVrgCIndft-VmGKNM",
                      :id "VVVrRHRDS3RQS2xzZy1nSk9fbTVEMG1RLnJaNVpqRzYtY3JR",
                      :snippet {:videoOwnerChannelTitle "Telepurte",
                                :description "Song by Hechizeros Band: https://youtu.be/x47NYUbtYb0

                                           Follow me here:
                                           https://twitter.com/Telepeturtle
                                           https://www.tiktok.com/@telepurte
                                           Support me here:
                                           https://www.patreon.com/Telepurte",
                                :publishedAt "2022-02-23T01:06:37Z",
                                :channelId "UCkDtCKtPKlsg-gJO_m5D0mQ",
                                :thumbnails {:default {:url "https://i.ytimg.com/vi/rZ5ZjG6-crQ/default.jpg",
                                                       :width 120,
                                                       :height 90},
                                             :medium {:url "https://i.ytimg.com/vi/rZ5ZjG6-crQ/mqdefault.jpg",
                                                      :width 320,
                                                      :height 180},
                                             :high {:url "https://i.ytimg.com/vi/rZ5ZjG6-crQ/hqdefault.jpg",
                                                    :width 480,
                                                    :height 360},
                                             :standard {:url "https://i.ytimg.com/vi/rZ5ZjG6-crQ/sddefault.jpg",
                                                        :width 640,
                                                        :height 480}},
                                :title "53 | now THIS is twosday",
                                :resourceId {:kind "youtube#video", :videoId "rZ5ZjG6-crQ"},
                                :videoOwnerChannelId "UCkDtCKtPKlsg-gJO_m5D0mQ",
                                :position 6,
                                :channelTitle "Telepurte",
                                :playlistId "UUkDtCKtPKlsg-gJO_m5D0mQ"}}
                     {:kind "youtube#playlistItem",
                      :etag "wnIeJKvjlaET51eBIK5u82omFV8",
                      :id "VVVrRHRDS3RQS2xzZy1nSk9fbTVEMG1RLnpOWlpKUVpjaFdj",
                      :snippet {:videoOwnerChannelTitle "Telepurte",
                                :description "Follow me here:
                                           https://twitter.com/Telepeturtle
                                           https://www.tiktok.com/@telepurte
                                           Support me here:
                                           https://www.patreon.com/Telepurte",
                                :publishedAt "2022-02-03T05:33:57Z",
                                :channelId "UCkDtCKtPKlsg-gJO_m5D0mQ",
                                :thumbnails {:default {:url "https://i.ytimg.com/vi/zNZZJQZchWc/default.jpg",
                                                       :width 120,
                                                       :height 90},
                                             :medium {:url "https://i.ytimg.com/vi/zNZZJQZchWc/mqdefault.jpg",
                                                      :width 320,
                                                      :height 180},
                                             :high {:url "https://i.ytimg.com/vi/zNZZJQZchWc/hqdefault.jpg",
                                                    :width 480,
                                                    :height 360},
                                             :standard {:url "https://i.ytimg.com/vi/zNZZJQZchWc/sddefault.jpg",
                                                        :width 640,
                                                        :height 480},
                                             :maxres {:url "https://i.ytimg.com/vi/zNZZJQZchWc/maxresdefault.jpg",
                                                      :width 1280,
                                                      :height 720}},
                                :title "33 | not quite twosday",
                                :resourceId {:kind "youtube#video", :videoId "zNZZJQZchWc"},
                                :videoOwnerChannelId "UCkDtCKtPKlsg-gJO_m5D0mQ",
                                :position 26,
                                :channelTitle "Telepurte",
                                :playlistId "UUkDtCKtPKlsg-gJO_m5D0mQ"}}),
   :id-or-url "youtube.com/watch?v=1DQ0j_9Pq-g"})

