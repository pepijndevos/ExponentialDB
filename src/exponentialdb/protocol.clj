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
  (doto writer
    (.write "$")
    (.write (str (count arg)))
    (.write "\r\n")
    (.write arg)
    (.write "\r\n")))

(defn write-multi-bulk [writer args]
  (doto writer
    (.write "*")
    (.write (str (count args)))
    (.write "\r\n"))
  (doseq [arg args]
    (write-bulk writer arg)))

(defn write-status [writer status]
  (doto writer
    (.write "+")
    (.write status)
    (.write "\r\n")))

(defn write-error [writer error]
  (doto writer
    (.write "-ERR ")
    (.write error)
    (.write "\r\n")))

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
    (.write "\r\n")))

