(ns concurency.vars
  (:gen-class))


;;; ATOMS

(def fred (atom {:cuddle-hunger-level 0
                 :percent-deteriorated 0}))

@fred

(let [zombie-state @fred]
  (if (>= (:percent-deteriorated zombie-state))
    (future (print (:cuddle-hunger-level zombie-state)))))

(swap! fred (fn [current-state]
              (merge-with + current-state {:cuddle-hunger-level 1})))

@fred

(defn inc-cuddle-hunger-level [zombie-state increase-by]
  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

(inc-cuddle-hunger-level @fred 24)

(swap! fred inc-cuddle-hunger-level 10)

(swap! fred update-in [:cuddle-hunger-level] + 21)

(let [num (atom 1)
      s1 @num]
  (swap! num inc)
  (println "State 1: " s1)
  (print "Current state: " @num))

(reset! fred {:cuddle-hunger-level 0
              :percent-deteriorated 0})


;;; WATCHES AND VALIDATORS

;;; Watches

(defn shuffle-speed [zombie]
  (* (:cuddle-hunger-level zombie)
     (- 100 (:percent-deteriorated zombie))))

(defn shuffle-alert [key watched old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "Run, you fool!")
        (println "The zombie's SPH is now " sph)
        (println "This message is brough to your courtesy by " key))
      (do
        (println "All well with " key)
        (println "Cuddle hunger: " (:cuddle-hunger-level new-state))
        (println "Percent deteriorated: " (:percent-deteriorated new-state))
        (println "SPH: " sph)))))

(reset! fred {:cuddle-hunger-level 22
              :percent-deteriorated 2})

(add-watch fred :fred-shuffle-alert shuffle-alert)

(swap! fred update-in [:percent-deteriorated] + 1)

(swap! fred update-in [:cuddle-hunger-level] + 30)

;;; Validators

(defn percent-deteriorated-validator [{:keys [percent-deteriorated]}]
  (and (>= percent-deteriorated 0)
       (<= percent-deteriorated 100)))

(def bobby (atom {:cuddle-hunger-level 0
                  :percent-deteriorated 0}
                 :validator percent-deteriorated-validator))

#_(swap! bobby update-in [:percent-deteriorated] + 200) ; This throws "Invalid Refernce State"


;;; Modelling Sock Transfer

(def sock-varieties
  #{"darned" "argyle" "wool" "horsehair" "mulleted"
    "passive-aggressive" "striped" "polka-dotted"
    "athletic" "business" "power" "invisible" "gollumed"})

(defn sock-count
  [sock-variety count]
  {:variety sock-variety
   :count count})

(defn generate-sock-gnome
  "Create an initial sock gnome state with no socks"
  [name]
  {:name name
   :socks #{}})

(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))
(def dryer (ref {:name "LG 1337"
                 :socks (set (map #(sock-count % 2) sock-varieties))}))

(defn steal-sock
  [gnome dryer]
  (dosync
   (when-let [pair (some #(if (= (:count %) 2) %) (:socks @dryer))]
     (let [updated-count (sock-count (:variety pair) 1)]
       (alter gnome update-in [:socks] conj updated-count)
       (alter dryer update-in [:socks] disj pair)
       (alter dryer update-in [:socks] conj updated-count)))))
(steal-sock sock-gnome dryer)

(:socks @sock-gnome)

