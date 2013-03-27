(ns peruse.web
  (:use [compojure.core :only [defroutes GET POST]]
        [ring.adapter.jetty :only [run-jetty]]
        [hiccup.page :only [include-css include-js html5]])
  (:require [compojure.route :as route]
            [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as q]))


(defn layout [title & content]
  (html5
    [:head
     [:title (str title " | Peruse")]
     (include-css "/css/bootstrap.min.css")]
    [:body
     [:div.container
      [:h1 title]
      content]]))

(defn latest-entries []
  (let [entries (take 100 (q/with-collection "entries" (q/sort (sorted-map :published-date -1))))]
    (layout "Latest Entries"
            (for [entry entries]
              [:div.entry
               [:a {:href (:link entry) :target "_blank"} [:h2 (:title entry)]]
               [:h6 (:feed-title entry)]
               [:div.body (:body entry)]
               [:hr]]))))

(defn feed-list []
  (let [feeds (q/with-collection "feeds")]
    (layout "All Feeds"
            [:ul
             (for [feed feeds]
               [:li [:a {:href "#"} (feed :title)]])])))

(defroutes routes
  (GET "/" [] (latest-entries))
  (GET "/feeds" [] (feed-list))
  (route/resources "/")
  (route/not-found "<h1>Page not found.</h1>"))

(def app routes)

;(mg/connect!)
;(mg/set-db! (mg/get-db "peruse"))
;(defonce server (run-jetty app {:port 8080 :join? false}))

(defn -main []
  (mg/connect!)
  (mg/set-db! (mg/get-db "peruse"))
  (run-jetty app {:port 8080}))
