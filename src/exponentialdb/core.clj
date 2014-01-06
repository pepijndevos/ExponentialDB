(ns exponentialdb.core
  (require [clojure.java.io :as io]
           [exponentialdb.protocol :as protocol]
           [exponentialdb.commands :as commands]
           [exponentialdb.db :as db]))

(def pool (java.util.concurrent.Executors/newCachedThreadPool))

(defn serve [state sock]
  (let [in (io/reader sock)
        out (io/writer sock)]
    (loop []
      (let [[cmd & args] (protocol/parse-bulk in)]
        (protocol/try-command out
          (apply (ns-resolve 'exponentialdb.commands (symbol cmd)) state args)))
      (recur))))

(defn -main [port]
  (let [sock (java.net.ServerSocket. (Integer/parseInt port))
        state (db/new-state)]
    (loop []
      (let [client-sock (.accept sock)]
        (.execute pool #(serve state client-sock)))
      (recur))))
