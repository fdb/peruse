(ns peruse.worker
  (:use [monger.operators :only [$lt]])
  (:require [peruse.feed :as feed]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]))

; The feed update interval, in milleseconds.
(def ^:const update-interval (* 5 60 1000))

(defn get-feed-with-name [name]
  "Find a feed with the given name in the database."
  (q/with-collection "feeds" (q/find {:title name})))

(defn feeds-to-update
  "Get the list of feeds to update from the database.
  Feeds to update are those where the fetchTime is more than 5 minutes ago."
  []
  (q/with-collection "feeds"
                     (q/find {:fetchTime {$lt (- (System/currentTimeMillis) update-interval)}}))) 
 
(defn merge-feed-into-entries
  "Add the feed id and title to each entry.
  Feed items from feed/fetch come back without a link to their feed."
  [feed entries]
  (map 
    #(merge % {:feed (:id feed) :feed-title (:title feed)}) 
   entries))

(defn update-feed
  "Update the feed.
  - fetch all its entries,
  - insert them into the database (ignoring duplicates),
  - and updating the fetchTime.
  This is the main worker function."
  [feed]
  (let [feed-contents (feed/fetch (:uri feed))
        entries (merge-feed-into-entries feed (:entries feed-contents))]
    (doseq [entry entries]
      (try
        (mc/insert "entries" entry)
        (catch Exception e)))
    (mc/save "feeds" (assoc feed :fetchTime (System/currentTimeMillis)))
    entries))

(defn fetch-out-of-date-feeds []
  "Find all feeds that are out of date and fetch them."
  (doseq [feed (feeds-to-update)]
    (println (str "Fetching " (:title feed)))
    (update-feed feed)))

(comment
  (mg/connect!)
  (mg/set-db! (mg/get-db "peruse"))
  (def ars (get-feed-with-name "Ars Technica"))
  (update-feed ars)
)

(defn -main []
  (mg/connect!)
  (mg/set-db! (mg/get-db "peruse"))
  (loop []
    (println "Fetching updated feeds")
    (fetch-out-of-date-feeds)
    (Thread/sleep update-interval)
    (recur)))
