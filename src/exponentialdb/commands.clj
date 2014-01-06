(ns exponentialdb.commands
  (:use exponentialdb.db
        clojure.set))

(def this *ns*)

(defn GET
  "Get the value of a key."
  [state & ks]
  (get-in (newest @state) ks))

(defn REVGET
  "Get the value of a key at a revision"
  [state rev & ks]
  (get-in @state (cons (Integer/parseInt rev) ks)))

(defn SET
  "Set the string value of a key"
  [state & kvs]
  (update state assoc-in (drop-last kvs) (last kvs))
  true)

(defn SADD
  "Add one or more members to a set"
  [state k & members]
  (let [members (set members)]
    (update state update-in [k] union members)
    (count members))) ; useless

(defn SUNION
  "Add multiple sets"
  [state & ks]
  (apply union (map (partial GET state) ks)))

(defn STORE
  "Store the result of a command in the db"
  [state k cmd & args]
  (let [cmdfn (ns-resolve this (symbol cmd))]
    (update state assoc k
            (apply cmdfn state args)))
  true)
