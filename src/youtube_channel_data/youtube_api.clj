(ns youtube-channel-data.youtube-api
  (:require [clojure.edn :as edn]
            [clojure.string :as str]))

; Read API key
(def config (edn/read-string (slurp "resources/config.edn")))

(def api-key {:key (:API-Key config)})

; Define url paths
(def base-url "https://www.googleapis.com")

(def sub-path "/youtube/v3/")

; Helpers
; from https://stackoverflow.com/a/9745663
(defn query-params->query-string [m]
  (str/join "&"
            (for [[k v] m]
              (str (name k) "="  (java.net.URLEncoder/encode v "UTF-8"))))) 

; Main API string builder closure
(defn api
  "Returns fn with route filled in"
  [route]
  (fn
    [query-params]
    (let [query-string (query-params->query-string (conj query-params api-key))]
      (str base-url sub-path route "?" query-string))))

; Caller helper methods
;; https://developers.google.com/youtube/v3/docs/videos/list
(def videos (api "videos"))

;; https://developers.google.com/youtube/v3/docs/channels/list
(def channels (api "channels"))

;; https://developers.google.com/youtube/v3/docs/playlistItems/list
(def playlist-items (api "playlistItems"))
