(ns concurency.core
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

;;; FUTURES

#_(let [result (future (println "this prints once")
                     (+ 1 1))]
  (println "deref: " (deref result))
  (println "@: " @result))

(let [result (future (Thread/sleep 3000)
                     (+ 1 1))]
  (println "Print immidiately")
  (println "The result is: " @result)
  (println "It will be at least 3 seconds before I print"))


; 42
(deref (future (Thread/sleep 700) 42) 1000 5)
; 5
(deref (future (Thread/sleep 200) 42) 1000 5)


; Interrogate a future
(realized? (future (Thread/sleep 10)))

(let [f (future)]
  @f
  (realized? f))

#_(defn f []
  (future (Thread/sleep 4000)
          (print "I'll print after 4 seconds"))
  (print "I'll print immidiately"))


;;; DELAYS

(def jackson-5-delay
  (delay (let [message "Just call my name and I'll be there"]
           (println "first deref: " message)
           message)))

(force jackson-5-delay)


(def gimli-headshots ["serious.jpg" "fun.jpg" "playful.jpg"])

(defn email-user
  [email-address]
  (println "Sending headshot notification to" email-address))

(defn upload-document
  "Needs to be implemented"
  [headshot]
  true)

(let [notify (delay (email-user "and-my-axe@gmail.com"))]
  (doseq [headshot gimli-headshots]
    (future (upload-document headshot)
            (force notify))))


;;; PROMISES

(def my-promise (promise))
(deliver my-promise (+ 1 2))
@my-promise

(def yak-butter-international
  {:store "Yak butter international"
   :price 90
   :smoothness 90})

(def butter-than-nothing
  {:store "Butter than nothing"
   :price 150
   :smoothness 83})

(def baby-got-yak
  {:store "Baby Got Yak"
   :price 90
   :smoothness 99})

(defn mock-api-call [result]
  (Thread/sleep 1000)
  result)

(defn satisfactory?
  "If the butter meets our criteria, return butter,
  else return false"
  [butter]
  (and (<= (:price butter) 100)
       (>= (:smoothness butter) 97)
       butter))

(def butter-retails [yak-butter-international butter-than-nothing baby-got-yak])

(time (some (comp satisfactory? mock-api-call)
            butter-retails))

(time (let [butter-promise (promise)]
        (doseq [retail butter-retails]
          (future (if-let [satisfactory-butter (-> retail
                                                   mock-api-call
                                                   satisfactory?)]
                    (deliver butter-promise satisfactory-butter))))
        (println "And the winner is: " (deref butter-promise 1500 "time out"))))

(let [p (promise)]
  (future (print "promise: " @p))
  (Thread/sleep 100)
  (deliver p 42))


;;; ROLLING YOUR OWN QUEUE
(defmacro wait
  "Sleep `timeout` seconds before evaluating body"
  [timeout & body]
  `(do (Thread/sleep ~timeout) ~@body))

(let [saying3 (promise)]
  (future (deliver saying3 (wait 100 "Cheerio!")))
  @(let [saying2 (promise)]
     (future (deliver saying2 (wait 400 "Pip pip!")))
     @(let [saying1 (promise)]
        (future (deliver saying1 (wait 200 "'Ello, gov'na!")))
        (println @saying1)
        saying1)
     (println @saying2)
     saying2)
  (println @saying3)
  saying3)

(defmacro enqueue
  ([q concurrent-promise-name concurrent serialized]
   `(let [~concurrent-promise-name (promise)]
      (future (deliver ~concurrent-promise-name ~concurrent))
      (deref ~q)
      ~serialized
      ~concurrent-promise-name))
  ([concurrent-promise-name concurrent serialized]
  query {
	user(id: "5a8bf2270f000043494781e1") {
		_id
		status
	}
} `(enqueue (future) ~concurrent-promise-name ~concurrent ~serialized)))

(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
    (enqueue saying (wait 400 "Pip pip!") (println @saying))
    (enqueue saying (wait 100 "Cheerio!") (println @saying)))

(time @(-> (enqueue saying (wait 200 "'Ello, gov'na!") (println @saying))
           (enqueue saying (wait 400 "Pip pip!") (println @saying))
           (enqueue saying (wait 100 "Cheerio!") (println @saying))))
