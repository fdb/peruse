(ns peruse.feed
  (:use [clojure.pprint :only [pprint]])
  (:import [java.net URL]
           [com.sun.syndication.io SyndFeedInput XmlReader]
           [com.sun.syndication.feed.synd SyndFeed]))

(defn flatten-contents [contents]
  "Flatten contents by taking out all values and appending them."
  (apply str (map #(.getValue %) contents)))

(defn entry-to-map [e]
  (let [contents (flatten-contents (.getContents e))
        description (when-let [d (.getDescription e)] (.getValue d))
        body (if-not (empty? contents) contents description)]
    {:title (.getTitle e)
     :published-date (.getPublishedDate e)
     :link (.getLink e)
     :body body
     :uri (.getUri e)}))

(defn feed-to-map [f]
  {:title (.getTitle f)
   :link (.getLink f)
   :entries (map entry-to-map (.getEntries f))})

(defn fetch [url]
  (let [feed-url (if (instance? URL url) url (URL. url))
        reader (XmlReader. feed-url)
        feed-input (SyndFeedInput.)
        feed (.build feed-input reader)]
    (feed-to-map feed)))

;(def feed-url (URL. "http://www.engadget.com/rss.xml"))
;(def feed-url (URL. "http://feeds.arstechnica.com/arstechnica/index/"))

;(def reader  (XmlReader. feed-url))
;(def feed-input (SyndFeedInput.))
;(def feed (.build feed-input reader))
;(def e (first (.getEntries feed)))

;(entry-to-map e)

;(.getValue (.getDescription e))
;(flatten-text (.getDescription e))

;(first (:entries (fetch "http://feeds.arstechnica.com/arstechnica/index/")))
;(first (:entries (fetch "http://www.engadget.com/rss.xml")))



(defn -main [url]
  (pprint (fetch url)))


