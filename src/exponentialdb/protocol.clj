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
  (let [len (if arg (count arg) -1)]
    (doto writer
      (.write "$")
      (.write (str len))
      (.write "\r\n"))
    (when arg
      (doto writer
        (.write arg)
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
    (.write status)
    (.write "\r\n")
    .flush))

(defn write-error [writer error]
  (doto writer
    (.write "-ERR ")
    (.write error)
    (.write "\r\n")
    .flush))

(defmacro try-command [writer & body]
  `(try
     ~@body
     (catch Exception e#
       (write-error ~writer
                    (.getMessage e#)))))

(defn write-int [writer integer]
  (doto writer
    (.write ":")
    (.write (str integer))
    (.write "\r\n")
    .flush))

