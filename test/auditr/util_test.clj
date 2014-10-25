(ns auditr.util-test
  (:require [clojure.test :refer :all]
            [auditr.util :refer :all]))

(def TESTSGROUP "app")
(def TESTHOST "127.0.0.1/32")

(deftest line-builder-test
  (testing "failed to build line"
    (let [test-string (str "ip = " TESTHOST)
          built-string (line-builder {:ip-address TESTHOST})]
    (is (= test-string built-string)))))

(deftest group-test
  (testing "failed to build group"
    (let [test-string (str "[" TESTSGROUP "]")
          built-string (line-builder {:group TESTSGROUP})]
      (is (= test-string built-string)))))
