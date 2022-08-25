(ns orgbot.core
  (:gen-class)
  (:require [clojure.string :as str]
            [morse.handlers :as h]
            [morse.api :as t]
            [morse.polling :as p]))

(def token (System/getenv "TELEGRAM_TOKEN"))

(defonce channel (atom nil))

(h/defhandler bot-api
  ; Each bot has to handle /start and /help commands.
  ; This could be done in form of a function:
  (h/command-fn "start" (fn [{{id :id :as chat} :chat}]
                          (println "Bot joined new chat: " chat)
                          (t/send-text token id "Welcome!")))

  ; You can use short syntax for same purposes
  ; Destructuring works same way as in function above
  (h/command "help" {{id :id :as chat} :chat}
    (println "Help was requested in " chat)
    (t/send-text token id "Help is on the way"))

  ; Handlers will be applied until there are any of those
  ; returns non-nil result processing update.

  ; Note that sending stuff to the user returns non-nil
  ; response from Telegram API.

  ; So match-all catch-through case would look something like this:
  (h/message message (println "Intercepted message:" message)))

(defn start-polling!
  "start polling"
  []
  (reset!
   channel
   (p/start token bot-api)))

(defn stop-polling!
  "stop polling"
  []
  (.close! @channel)
  (reset! channel nil))

(defn err
  "error util"
  [msg code]
  (println msg)
  (System/exit code))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (if (not (str/blank? token))
    (start-polling!)
    (err "TELEGRAM_TOKEN env not set" 1)))
