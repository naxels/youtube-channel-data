(ns youtube-channel-data.exporter.csv
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

; from: https://stackoverflow.com/a/48244002

(defn write-csv
  "Takes a file (path, name and extension) and 
   csv-data (vector of vectors with all values) and
   writes csv file."
  [file csv-data]
  (with-open [writer (io/writer file)]
    (csv/write-csv writer csv-data)))

(defn maps->csv-data
  "Takes a collection of maps and returns csv-data 
   (vector of vectors with all values)."
  [maps]
  (let [columns (-> maps first keys)
        headers (mapv name columns)
        rows (mapv #(mapv % columns) maps)]
    (into [headers] rows)))

(defn write-csv-from-maps
  "Takes a file (path, name and extension) and a collection of maps
   transforms data (vector of vectors with all values) 
   writes csv file."
  [file maps]
  (->> maps maps->csv-data (write-csv file)))
