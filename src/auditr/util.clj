(ns auditr.util
  (:gen-class))

(def not-nil? (complement nil?))

(defn line-builder
  [{:keys [group ip-address security-group from-port to-port protocol]}]
  (if (not-nil? group)
    (str "[" group "]")
  (str 
    (if (not-nil? ip-address)
      (str "ip = " ip-address))
    (if (not-nil? security-group)
      (str " sg = " security-group))
    (if (not-nil? from-port)
      (str " port = " from-port))
    (if (and (not-nil? from-port) (not-nil? to-port))
        (str to-port))
    (if (not-nil? protocol)
      (str " protocol = " protocol)))))

