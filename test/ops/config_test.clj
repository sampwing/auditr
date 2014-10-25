(ns ops.config-test
  (:require [clojure.test :refer :all]
            [ops.config :refer :all]))

(def TESTSGROUP "app")
(def TESTHOST "127.0.0.1")
(def TESTPORT "5000")
(def TESTPORT2 "5001")
(def TESTPROCESS "udp")

(def not-nil? (complement nil?))

(defn line-builder
  [{:keys [ip-address security-group port-from port-to process]}]
  (str 
    (if (not-nil? ip-address)
      "ip = " ip-address)
    (if (not-nil? security-group)
      " sg = " security-group)
    (if (not-nil? port-from)
      " port = " port-from)
    (if (and (not-nil? port-from) (not-nil? port-to))
        port-to)
    (if (not-nil? process)
      " process = " process)))

(deftest ip-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (nil? (:port-from r)))
    (is (nil? (:port-to r)))
    (is (nil? (:process r))))))

(deftest ip-port-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:port-from r) TESTPORT))
    (is (nil? (:port-to r)))
    (is (nil? (:process r))))))

(deftest ip-port-range-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT "-" TESTPORT2))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:port-from r) TESTPORT))
    (is (= (:port-to r) TESTPORT2))
    (is (nil? (:process r))))))

(deftest ip-port-range-process-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT " process=" TESTPROCESS))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:port-from r) TESTPORT))
    (is (nil? (:port-to r)))
    (is (= (:process r) TESTPROCESS)))))

(deftest ip-port-range-process-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "ip=" TESTHOST " port=" TESTPORT "-" TESTPORT2 " process=" TESTPROCESS))]
    (is (= (:type r) :IP-ADDRESS))
    (is (= (:identifier r) TESTHOST))
    (is (= (:port-from r) TESTPORT))
    (is (= (:port-to r) TESTPORT2))
    (is (= (:process r) TESTPROCESS)))))

(deftest sg-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (nil? (:port-from r)))
    (is (nil? (:port-to r)))
    (is (nil? (:process r))))))

(deftest sg-port-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:port-from r) TESTPORT))
    (is (nil? (:port-to r)))
    (is (nil? (:process r))))))

(deftest sg-port-range-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT "-" TESTPORT2))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:port-from r) TESTPORT))
    (is (= (:port-to r) TESTPORT2))
    (is (nil? (:process r))))))

(deftest sg-port-range-process-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT " process=" TESTPROCESS))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:port-from r) TESTPORT))
    (is (nil? (:port-to r)))
    (is (= (:process r) TESTPROCESS)))))

(deftest sg-port-range-process-test
  (testing "failed to parse ip address"
    (let [r (config-parse-line (str "sg=" TESTSGROUP " port=" TESTPORT "-" TESTPORT2 " process=" TESTPROCESS))]
    (is (= (:type r) :SECURITY-GROUP))
    (is (= (:identifier r) TESTSGROUP))
    (is (= (:port-from r) TESTPORT))
    (is (= (:port-to r) TESTPORT2))
    (is (= (:process r) TESTPROCESS)))))

