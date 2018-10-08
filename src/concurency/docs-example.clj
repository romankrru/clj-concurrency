(ns concurency.docs-example
  (:gen-class))


;;;; ATOMS

(def currently-connected (atom []))
(swap! currently-connected conj "chatty joe")
@currently-connected

;;;; AGENTS

(def errors-counter (agent 0))
errors-counter
@errors-counter

(send errors-counter inc)
(send errors-counter + 10)
@errors-counter

#_(send errors-counter / 0)
(send errors-counter inc)

;; To access the exception that occured during the agent's
;; state mutation use `clojure.core/agent-error`

#_(send errors-counter / 0)
#_(agent-error errors-counter)

;; Agents can be restarted with `clojure.core/agent-restart`
(restart-agent errors-counter 0)
(send errors-counter + 10)
@errors-counter

;; Ignore exception instead of going to failure mode
(def errors-counter-1 (agent 0
                             :error-mode :continue
                             :error-handler (fn [failed-agent ^Exception exception]
                                              (println (.getMessage exception)))))

(send errors-counter-1 inc)
(send errors-counter-1 / 0) ; Print error to the console
(send errors-counter-1 inc)


;;;; REFS

(def account-a (ref 0))
(def account-b (ref 0))

@account-b

;; Refs modified using `alter`

(def account-c (ref 1000))
(def account-d (ref 400))

(dosync
 (alter account-c + 100)
 (alter account-d - 100))

@account-c ; 1100
@account-d ; 300


;;; Vars
(def url "http://en.wikipedia.org/wiki/Margarita")

;; Dynamic Scoping and Thread-local Bindings
(def ^:dynamic *url* "http://en.wikipedia.org/wiki/Margarita")

(println (format "url is now %s" *url*))

(binding [*url* "test!!!"]
  (println (format "url is now %s" *url*)))

(.start (Thread. (fn []
                   (binding [*url* "http://en.wikipedia.org/wiki/Cointreau"]
                     (println (format "*url* is now %s" *url*))))))
;; outputs "*url* is now http://en.wikipedia.org/wiki/Cointreau"
;; ⇒ nil
(.start (Thread. (fn []
                   (binding [*url* "http://en.wikipedia.org/wiki/Guignolet"]
                     (println (format "*url* is now %s" *url*))))))
;; outputs "*url* is now http://en.wikipedia.org/wiki/Guignolet"
;; ⇒ nil
(.start (Thread. (fn []
                   (binding [*url* "http://en.wikipedia.org/wiki/Apéritif"]
                     (println (format "*url* is now %s" *url*))))))
;; outputs "*url* is now http://en.wikipedia.org/wiki/Apéritif"
;; ⇒ nil
(println (format "*url* is now %s" *url*))
;; outputs "*url* is now http://en.wikipedia.org/wiki/Margarita"
;; ⇒ nil

;; Alter var root
*url*
;; ⇒ "http://en.wikipedia.org/wiki/Margarita"
(.start (Thread. (fn []
                   (alter-var-root (var concurency.docs-example/*url*) (fn [_] "http://en.wikipedia.org/wiki/Apéritif"))
                   (println (format "*url* is now %s" *url*)))))
;; outputs "*url* is now http://en.wikipedia.org/wiki/Apéritif"
;; ⇒ nil
*url*
;; ⇒ "http://en.wikipedia.org/wiki/Apéritif"


;;; Delays
(def d (delay (System/currentTimeMillis)))
@d
(force d)

;;; Futures
(def ft (future ( + 1 2 3 4 5)))
@ft

;; will block the current thread for 10 seconds, returns :completed
(def ft (future (Thread/sleep 10000) :completed))
;; ⇒ #'user/ft
(deref ft 2000 :timed-out)
;; ⇒ :timed-out

;;; Promises
(def p (promise))
(deliver p 42)
@p


