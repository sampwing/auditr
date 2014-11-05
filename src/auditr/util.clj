(ns auditr.util
  (:gen-class))

(require '[clojure.edn :as edn])

(def not-nil? (complement nil?))

(defn build-line
  [{:keys [group ip-address security-group from-port to-port ip-protocol] :as line}]
  (if (not-nil? group)
    (str "[" group "]")
  (str 
    (if (not-nil? ip-address)
      (str "ip = " ip-address))
    (if (not-nil? security-group)
      (str "sg = " security-group))
    (if (not-nil? from-port)
      (str " port = " from-port))
    (if (and (not-nil? from-port) (not-nil? to-port) (< from-port to-port))
        (str "-" to-port))
    (if (not-nil? ip-protocol)
      (str " protocol = " ip-protocol)))))

(defn ip-to-number
  [ip]
  (let [ip (clojure.string/replace ip #"/\d+" "")]
    (loop [[car & cdr] (map edn/read-string (clojure.string/split ip #"\.")) total 0] 
      (if (nil? car) 
        total 
        (recur cdr (+ (bit-shift-left total 8) car))))))
