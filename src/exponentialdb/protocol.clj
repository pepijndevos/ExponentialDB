(ns exponentialdb.protocol)

(defn parse-line [reader]
  [(char (.read reader))
   (Integer/parseInt (.readLine reader))])

(defn parse-bulk [reader]
  (let [[typ length] (parse-line reader)]
    (assert (= typ \*) "Not the start of a bulk reply")
    (for [i (range length)]
      (let [[typ length] (parse-line reader)
            buf (char-array length)
            arg (.read reader buf 0 length)]
        (assert (= typ \$) "Not a valid bulk argument")
        (.skip reader 2) ;CR LF
        (String. buf)))))

(defn write-bulk [writer arg]
  (let [len (if arg (count (str arg)) -1)]
    (doto writer
      (.write "$")
      (.write (str len))
      (.write "\r\n"))
    (when arg
      (doto writer
        (.write (str arg))
        (.write "\r\n")))))

(defn write-multi-bulk [writer args]
  (doto writer
    (.write "*")
    (.write (str (count args)))
    (.write "\r\n"))
  (doseq [arg args]
    (write-bulk writer arg))
  (.flush writer))

(defn write-status [writer status]
  (doto writer
    (.write "+")
    (.write (str status))
    (.write "\r\n")
    .flush))

(defn write-error [writer error]
  (doto writer
    (.write "-ERR ")
    (.write (str error))
    (.write "\r\n")
    .flush))

(defn write-int [writer integer]
  (doto writer
    (.write ":")
    (.write (str integer))
    (.write "\r\n")
    .flush))

(defn serialize [writer data]
  (cond
    (coll? data)       (write-multi-bulk writer data)
    (or (string? data)
        (nil? data))   (do (write-bulk writer data) (.flush writer))
    (number? data)     (write-int writer data)
    (true? data)       (write-status writer "OK")
    :else              (write-error "Unrecognized return type")))

(defmacro try-command [writer & body]
  `(let [writer# ~writer]
     (try
       (serialize writer#
                  (do ~@body))
       (catch Exception e#
         (.printStackTrace e#)
         (write-error writer#
                      (.getMessage e#))))))
