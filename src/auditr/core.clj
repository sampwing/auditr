(ns auditr.core
  (:gen-class))

(require '[clojure.set :as cset])
(require '[clojure.tools.cli :refer [parse-opts]])
(require '[amazonica.core :as amazonica])
(require '[amazonica.aws.ec2 :as ec2])
(require '[auditr.util :as util])
(require '[auditr.config :as config])

(defn aws-authenticate [& args]
  (amazonica/defcredential (System/getenv "AWS_AUDIT_KEY") (System/getenv "AWS_AUDIT_SECRET")))

(defn build-config
  [{:keys [ip-protocol from-port to-port ip-ranges user-id-group-pairs] :as data}]
  (let [ip-rules (map util/build-line (map #(conj data {:ip-address %1}) (sort #(compare (util/ip-to-number %1) (util/ip-to-number %2)) ip-ranges)))
        sg-rules (map util/build-line (map #(conj data {:security-group (:group-name %1)}) user-id-group-pairs))]
    (concat ip-rules sg-rules)))

(defn sg-info 
  "print security-group information"
  [sg]
  (let [{:keys [description group-name ip-permissions]} sg
        m {:group group-name 
           :rules (sort  (reduce concat (map build-config ip-permissions)))}]
      m))

(defn get-security-group-rules
  []
  (aws-authenticate)
  (let [{security-groups :security-groups} (ec2/describe-security-groups)
        rules (sort #(compare (:group %1) (:group %2)) (map sg-info security-groups))]
      rules))

(defn sg-info2
  "print security-group information"
  [sg]
  (let [{:keys [description group-name ip-permissions]} sg
        m {:group group-name 
           :rules ip-permissions}]
      m))

(defn get-security-group-rules2
  []
  (aws-authenticate)
  (let [{security-groups :security-groups} (ec2/describe-security-groups)
        rules (sort #(compare (:group %1) (:group %2)) (map sg-info2 security-groups))]
      rules))


(defn generate-configuration-body-helper
  [rules]
  (str (util/build-line rules) "\n" (clojure.string/join "\n" (:rules rules))))

(defn generate-configuration-body
  []
  (let [rules (get-security-group-rules)]
    (map generate-configuration-body-helper rules)))

(defn generate-configuration
  [filename]
  (let [rules (get-security-group-rules)]
    (spit filename (clojure.string/join "\n" (generate-configuration-body)))))

(defn rules-from-lines
  [parsed-lines]
  (loop [[car & cdr] parsed-lines
         mapping {}
         group-name nil
         group-rules []]
    (if (nil? car) 
      (if (empty? group-rules) mapping
        (assoc mapping group-name group-rules))
      (let [{:keys [identifier] :as data} car
            inner-group-name (case (:type car)
                               :GROUP identifier
                               nil)
            inner-rules (case (:type car)
                          :IP-ADDRESS (conj group-rules data)
                          :SECURITY-GROUP (conj group-rules data)
                          [])]
        (recur cdr (case (:type car)
                     :GROUP (if (nil? group-name) {}
                              (assoc mapping group-name group-rules))
                     mapping)
               (if (nil? inner-group-name) group-name
                 inner-group-name)
               (case (:type car)
                       :GROUP []
                       inner-rules))))))

(defn parse-configuration
  [filename]
  (let [lines (clojure.string/split (slurp filename) #"\n")
        parsed-lines (map config/config-parse-line lines)]
      (rules-from-lines parsed-lines)))

(defn make-key
  [{:keys [identifier protocol from-port]}]
  (str protocol "://" identifier ":" from-port))

(defn make-keys-aws
  [{:keys [ip-protocol user-id-group-pairs ip-ranges from-port] :as input}]
  (let [sgs (if (empty? user-id-group-pairs) []
              (map #(make-key {:identifier (:group-name %1)
                                    :protocol ip-protocol
                                    :from-port from-port})
                   user-id-group-pairs))
        ips (if (empty? ip-ranges) []
              (map #(make-key {:identifier %1
                                    :protocol ip-protocol
                                    :from-port from-port})
                   ip-ranges))]
    (cset/union (set sgs) (set ips))))

; report mismatches
(defn compare-conf-aws
  [{:keys [conf aws]}]
  (let [conf-missing (cset/difference conf aws)
        aws-extra (cset/difference aws conf)]
    (if ((complement empty?) conf-missing)
         (println "rule(s) in config which were not found:\n" conf-missing))
    (if ((complement empty?) aws-extra)
         (println "rule(s) which were unexpected:\n" aws-extra))))

; build rule maps for config and aws for each sg
(defn build-rule-map
  [{:keys [rules is-config]}]
  (loop [[car & cdr] rules
         rule-map {}]
    (if (nil? car) rule-map
      (let [{:keys [rules group]} car
            rule-set (if (nil? is-config) (reduce cset/union (map make-keys-aws rules))
                       (set (map make-key rules)))]
        (recur cdr (assoc rule-map group rule-set))))))

(defn map-kv
  [m f]
  (reduce-kv #(assoc %1 %2 (set (map f %3))) {} m))

; iterate over the configs map and check against those in sg
(defn compare-rules
  [filename]
  (let [aws-rules (get-security-group-rules2)
        aws-rule-map (build-rule-map {:rules aws-rules})
        conf-rules (parse-configuration filename)
        conf-rule-map (map-kv conf-rules make-key)]
    (doseq [security-group (sort (keys conf-rule-map))]
      (let [found-rules (get aws-rule-map security-group)
            desired-rules (get conf-rule-map security-group)]
        ;(prn "desired-rules:" desired-rules)
        ;(prn "found-rules:" found-rules)
        (println "Checking" security-group "...")
        (compare-conf-aws {:conf desired-rules :aws found-rules})))))

(def cli-options
  [["-o" "--output OUTPUT" "Output File"
    :default nil
    :id :output]])

(defn -main
  [& args]
  (let [{options :options} (parse-opts args cli-options)]
    (doseq [arg args] 
      (cond
        (= arg "generate") (generate-configuration "configuration")
        (= arg "parse") (prn (parse-configuration "configuration"))
        (= arg "compare") (compare-rules "configuration")
        :else (prn "not implemented")))))

