(ns youtube-channel-data.add
  (:require [clojure.string :as str]
            [youtube-channel-data.utilities :as u]
            [youtube-channel-data.calc :refer [video-id->channel-id
                                               channel-id->playlist-id+title
                                               playlist-id->playlist-items
                                               playlist-items->videos
                                               transform-playlist-items]]
            [youtube-channel-data.output :as output]))



(defn video-title-filter
  [{{filter-option :filter} :options :as data}]
  (assoc data :video-title-filter (and filter-option
                                       (str/lower-case filter-option))))

(defn video-id
  [{:keys [id-or-url] :as data}]
  (assoc data :video-id (u/parse-input id-or-url)))

(defn channel-id
  [{:keys [video-id] :as data}]
  (assoc data :channel-id (video-id->channel-id video-id)))

(defn playlist-id+channel-title
  [{:keys [channel-id] :as data}]
  (let [[playlist-id channel-title] (channel-id->playlist-id+title channel-id)]
    (assoc data
      :playlist-id playlist-id
      :channel-title channel-title)))

(defn output-data
  [{:keys [video-title-filter channel-title] :as data}]
  (let [output-map {:location (output/location)
                    :filename (output/filename video-title-filter channel-title)
                    :separator \.
                    :extension (output/extension (get-in data [:options :output]))}
        output (assoc output-map :file (apply str (vals output-map)))]
    (assoc data :output output)))

(defn playlist-items
  [{:keys [video-title-filter playlist-id] :as data}]
  (let [title-match? (u/title-match-builder video-title-filter)]
    (assoc data :playlist-items (cond->> (playlist-id->playlist-items playlist-id)
                                         video-title-filter (filter title-match?)))))

(defn videos-data
  [{:keys [playlist-items] :as data}]
  (assoc data :videos (playlist-items->videos playlist-items)))

(defn transformed-playlist-items
  [{:keys [playlist-items videos] :as data}]
  (assoc data :playlist-items-transformed (transform-playlist-items playlist-items videos)))