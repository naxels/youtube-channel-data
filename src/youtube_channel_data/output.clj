(ns youtube-channel-data.output
  (:require   [clojure.java.io :as io]))

(def supported-formats ["csv"])

(defn location
  "Output to output folder if exists, else to current location"
  [channel-title]
  (if (.exists (io/file "output"))
    (str "output" (java.io.File/separator) channel-title)
    (str channel-title)))

(defn extension
  [output-location output-format]
  (str output-location "." (condp = output-format
                             "csv" "csv"
                             "csv")))