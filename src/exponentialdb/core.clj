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

(defn cardinality [state]
  (if (seq state)
    (inc (key (first state)))
    0))

(defn newest [state]
  (when (seq state)
    (val (first state))))

(defn decay [state distfn mergefn]
  (let [car (cardinality state)
        idx (distfn car)
        [leftkey leftval]   (nth (seq state) idx nil)
        [rightkey rightval] (nth (seq state) (inc idx) nil)]
    (if (and leftval rightval)
      (-> state
          (dissoc rightkey)
          (assoc leftkey (mergefn leftval rightval)))
      state)))

(defn add-state [state f args]
  (assoc state
    (cardinality state) 
    (apply f (newest state) args)))

(defn basic-update [f & args]
  (swap! state
         #(-> %
              (decay
                exponential-distribution
                (fn [a b] b))
              (add-state f args))))

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
