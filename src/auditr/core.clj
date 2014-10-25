(ns auditr.core
  (:gen-class))

(require '[amazonica.core :as amazonica])
(require '[amazonica.aws.ec2 :as ec2])

(defn aws-authenticate [& args]
  (amazonica/defcredential (System/getenv "AWS_AUDIT_KEY") (System/getenv "AWS_AUDIT_SECRET")))

(defn user-id-group-pair-info
  "print the security group names"
  [user-id-group-pair]
  (let [{:keys [group-name]} user-id-group-pair]
    (println group-name)))

(defn ip-permission-info
  "print information about the ip-permission"
  [ip-permission]
  (let [{:keys [ip-protocol from-port to-port user-id-group-pairs ip-ranges]} ip-permission]
    (println (clojure.string/join ["** (" ip-protocol ") " from-port " - " to-port]))
    (println (doseq [ip-address ip-ranges] (println ip-address)))
    (if ((complement empty?) user-id-group-pairs)
      (do (println "has inbound from security groups:")
        (doseq [user-id-group-pair user-id-group-pairs] (user-id-group-pair-info user-id-group-pair))))
    (println "")))

(defn sg-info 
  "print security-group information"
  [sg]
  (let [{:keys [description group-name ip-permissions]} sg]
    (println "")
    (println (clojure.string/join " - " [group-name description]))
    (doseq [ip-permission ip-permissions] (ip-permission-info ip-permission))
    (println "")))
    
(aws-authenticate)
(def sgs (:security-groups (ec2/describe-security-groups)))

(defn -main
  [& args]
  (println sgs)
  (doseq [sg sgs] (sg-info sg)))

