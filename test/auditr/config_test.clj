(ns auditr.config-test
  (:require [clojure.test :refer :all]
            [auditr.config :refer :all]))

(def TESTSGROUP "app")
(def TESTHOST "127.0.0.1/32")
(def TESTPORT "5000")
(def TESTPORT2 "5001")
(def TESTPROTOCOL "udp")

(def not-nil? (complement nil?))

(defn line-builder
  [{:keys [ip-address security-group from-port to-port protocol]}]
  (str 
    (if (not-nil? ip-address)
      "ip = " ip-address)
    (if (not-nil? security-group)
      " sg = " security-group)
    (if (not-nil? from-port)
      " port = " from-port)
    (if (and (not-nil? from-port) (not-nil? to-port))
        to-port)
    (if (not-nil? protocol)
      " protocol = " protocol)))

(deftest ip-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (nil? (:from-port r)))
    (is (nil? (:to-port r)))
    (is (nil? (:protocol r))))))

(deftest ip-port-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:from-port r) TESTPORT))
    (is (nil? (:to-port r)))
    (is (nil? (:protocol r))))))

(deftest ip-port-range-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT "-" TESTPORT2))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:from-port r) TESTPORT))
    (is (= (:to-port r) TESTPORT2))
    (is (nil? (:protocol r))))))

(deftest ip-port-range-protocol-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT " protocol=" TESTPROTOCOL))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:from-port r) TESTPORT))
    (is (nil? (:to-port r)))
    (is (= (:protocol r) TESTPROTOCOL)))))

(deftest ip-port-range-protocol-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT "-" TESTPORT2 " protocol=" TESTPROTOCOL))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:from-port r) TESTPORT))
    (is (= (:to-port r) TESTPORT2))
    (is (= (:protocol r) TESTPROTOCOL)))))

(deftest sg-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (nil? (:from-port r)))
    (is (nil? (:to-port r)))
    (is (nil? (:protocol r))))))

(deftest sg-port-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:from-port r) TESTPORT))
    (is (nil? (:to-port r)))
    (is (nil? (:protocol r))))))

(deftest sg-port-range-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT "-" TESTPORT2))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:from-port r) TESTPORT))
    (is (= (:to-port r) TESTPORT2))
    (is (nil? (:protocol r))))))

(deftest sg-port-range-protocol-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT " protocol=" TESTPROTOCOL))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:from-port r) TESTPORT))
    (is (nil? (:to-port r)))
    (is (= (:protocol r) TESTPROTOCOL)))))

(deftest sg-port-range-protocol-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT "-" TESTPORT2 " protocol=" TESTPROTOCOL))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:from-port r) TESTPORT))
    (is (= (:to-port r) TESTPORT2))
    (is (= (:protocol r) TESTPROTOCOL)))))

