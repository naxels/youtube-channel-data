(ns youtube-channel-data.youtube.config
  (:require [clojure.edn :as edn]))

(set! *warn-on-reflection* true)

; Read API key from env or config.edn, return nil if none found
(def read-key (try
                (or
                 (System/getenv "GOOGLE_API_KEY")
                 (-> "resources/config.edn"
                     (slurp)
                     (edn/read-string)
                     (:API-Key)))
                (catch java.io.FileNotFoundException _e
                  (println "No API key found"))))

; Turn API key to map, using "" when nil found
(def api-key {:key (or read-key
                       "")})
