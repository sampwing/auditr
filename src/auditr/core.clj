(ns auditr.core
  (:gen-class))

(require '[clojure.tools.cli :refer [parse-opts]])
(require '[amazonica.core :as amazonica])
(require '[amazonica.aws.ec2 :as ec2])
(require '[auditr.util :as util])

(defn aws-authenticate [& args]
  (amazonica/defcredential (System/getenv "AWS_AUDIT_KEY") (System/getenv "AWS_AUDIT_SECRET")))

(defn build-config
  [{:keys [ip-protocol from-port to-port ip-ranges user-id-group-pairs] :as data}]
  (let [ip-rules (map util/build-line (map #(conj data {:ip-address %1}) ip-ranges))
        sg-rules (map util/build-line (map #(conj data {:security-group (:group-name %1)}) user-id-group-pairs))]
    (concat ip-rules sg-rules)))

(defn sg-info 
  "print security-group information"
  [sg]
  (let [{:keys [description group-name ip-permissions]} sg]
    (println "")
    (println (clojure.string/join " - " [(util/build-line {:group group-name}) description]))
    (let [m {:group group-name 
             :rules (reduce concat (map build-config ip-permissions))}]
      (prn m))))
 
(aws-authenticate)

(def sgs 
  (:security-groups (ec2/describe-security-groups)))

(def cli-options
  [["-o" "--output OUTPUT" "Output File"
    :default nil
    :id :output]])

(defn -main
  [& args]
  (let [{options :options} (parse-opts args cli-options)]
    (doseq [arg args] 
      (if (and 
            (= arg "generate")
            ((complement nil?) (:output options)))
        (doseq [sg sgs] (sg-info sg))))))

