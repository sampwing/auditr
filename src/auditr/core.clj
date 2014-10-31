(ns auditr.core
  (:gen-class))

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
  [pl]
  (loop [[car & cdr] pl rs [] mapping {}]
    (if (nil? car) rs
    (let [{:keys [group rules]} mapping
          inner-mapping (if (= (:type car) :GROUP)
                            (conj mapping {:group (:identifier car)})
                            (if (nil? rules)
                              (conj mapping {:rules [car]})
                              (conj mapping {:rules (conj rules car)})))
        [irs im] (if (empty? cdr)
                         [(conj rs inner-mapping) {}]
                         (if (and (= (:type car) :GROUP)
                                    ((complement empty?) mapping))
                         [(conj rs mapping) inner-mapping]
                         [rs inner-mapping]))]
              (recur cdr irs im)))))

(defn parse-configuration
  [filename]
  (let [lines (take 1000 (clojure.string/split (slurp filename) #"\n"))
        parsed-lines (map config/config-parse-line lines)]
      (rules-from-lines parsed-lines)))

(defn compare-rules
  [filename]
  (let [aws-rules (get-security-group-rules2)
        config-rules (parse-configuration filename)]
    (let [config-rule (first config-rules)
          aws-rule (first (filter #(= (:group config-rule) (:group %1)) aws-rules))]
      (prn config-rule)
      (prn "**")
      (prn aws-rule))))

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
        (= arg "compare") (prn (compare-rules "configuration"))
        :else (prn "not implemented")))))

