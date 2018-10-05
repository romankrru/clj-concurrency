(ns concurency.search
  (:gen-class))


(def sources {:twitter "https://twitter.com/search?q=%s"
              :wiki "https://en.wikipedia.org/w/index.php?search=%s"})

(defn search
  "Takes a string as an argument and search for it on Wiki or Twitter.
  Returns the HTML of the first page returned by the search."
  [term]
  (let [search-promise (promise)]
    (doseq [[engine-name search-template] sources]
      (->> term
           (format search-template)
           (slurp)
           (deliver search-promise)
           (future)))
    (deref search-promise 3500 "Time out!")))


(search "cats")

