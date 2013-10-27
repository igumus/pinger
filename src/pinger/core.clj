(ns pinger.core
  (:import (java.net URL HttpURLConnection))
  (:use [pinger.scheduler])
  (:require [clojure.tools.logging :as logger]
            [pinger.config :as config])
  (:gen-class))


(defn response-code [address]
  (let [conn ^HttpURLConnection (.openConnection (URL. address))
        code (.getResponseCode conn)]
    (when (< code 400)
      (-> conn .getInputStream .close))
    code))


(defn avaliable?
  [address]
  (= 200 (response-code address)))


(defn record-availability [address]
  (if (avaliable? address)
    (logger/info (str address " is responding normally"))
    (logger/error (str address " is not available"))))


(defn check []
  (doseq [addres (config/urls (config/config))]
    (println (record-availability addres))))

(def immediately 0)

(def every-minute (* 60 1000))

(defn start [e]
  "REPL helper. Start pinger on executor e."
  (periodically e check :initial-delay immediately :delay every-minute))

(defn stop [e]
  "REPL helper. Stop executor e."
  (shutdown-executor e))


(defn -main []
  (start (scheduled-executor 1)))
