(ns exponentialdb.commands
  (:use exponentialdb.db))

(defn GET
  "Get the value of a key,
  optionally at a revison"
  ([state k]
   (get (newest @state) k))
  ([state k rev]
   (get-in @state rev k)))

(defn SET
  "Set the string value of a key"
  [state k v]
  (cardinality (update state assoc k v)))
