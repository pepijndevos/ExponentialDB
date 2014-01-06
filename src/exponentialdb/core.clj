(ns exponentialdb.core
  (require [clojure.java.io :as io]))

(def state (atom (sorted-map-by (comparator >))))

(def pool (java.util.concurrent.Executors/newCachedThreadPool))

(defn exponential-distribution
  "Between N and N+1, find the bit that became one.
  This exponent of 2 has 1 bit set.
  Decrement by one to set all bits below.
  Count the bits."
  [n]
  (Long/bitCount
    (dec
      (bit-and (inc n) (bit-not n)))))

(def ^:dynamic *distribution* exponential-distribution)
(def ^:dynamic *merge-fn* (fn [a b] b))

(defn cardinality [state]
  (if (seq state)
    (inc (key (first state)))
    0))

(defn newest [state]
  (when (seq state)
    (val (first state))))

(defn decay [state]
  (let [car (cardinality state)
        idx (*distribution* car)
        [leftkey leftval]   (nth (seq state) idx nil)
        [rightkey rightval] (nth (seq state) (inc idx) nil)]
    (if (and leftval rightval)
      (-> state
          (dissoc rightkey)
          (assoc leftkey (*merge-fn* leftval rightval)))
      state)))

(defn add-state [state f args]
  (assoc state
    (cardinality state) 
    (apply f (newest state) args)))

(defn update [f & args]
  (swap! state #(add-state (decay %) f args)))

(defn serve [sock]
  (let [in (io/reader sock)
        out (io/writer sock)]
    (loop []
      (println "hoi")
      (recur))))

(defn -main [port]
  (let [sock (java.net.ServerSocket. (Integer/parseInt port))]
    (loop []
      (let [client-sock (.accept sock)]
        (.execute pool #(serve client-sock)))
      (recur))))
