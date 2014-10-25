(ns auditr.config
  (:gen-class))

(require '[instaparse.core :as parse])

(def config-parser
  (parse/parser
    "<START> = GROUP | RULE | Epsilon
    GROUP = <'['> SG <']'>
    RULE = (IP-ADDRESS | SECURITY-GROUP) [<WS> PORT-RANGE] [<WS> PROTOCOL]
    SECURITY-GROUP = <'sg'> <WS-EQ> SG
    <SG> = SG-CLASSIC | SG-VPC 
    <SG-CLASSIC> = #\"\\w{1,255}\"
    <SG-VPC> = #\"\\w{1,255}\"
    IP-ADDRESS = <'ip'> <WS-EQ> IP 
    PROTOCOL = <'protocol'> <WS-EQ> TYPE
    WS-EQ = [WS] EQ [WS]
    WS = #\"\\s+\"
    EQ = #\"\\s*=\\s*\"
    <IP> = #\"\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}/\\d+\"
    PORT-RANGE = <'port'> <WS-EQ> PORT
    <PORT> = PORT-REG [<'-'> PORT-REG]
    <PORT-REG> = #\"\\d+\"
    <TYPE> = #\"\\w+\""))

(defn config-parse-line
  [line]
  (let [r (first (config-parser line))]
    (if (= (first r) :GROUP)
      {:type :GROUP :identifier (second r)}
    (let [[[ip-or-sg identifier] [_ from-port to-port] [_ protocol]] (rest r)]
      {:type ip-or-sg :identifier identifier :from-port from-port :to-port to-port :protocol protocol}))))

; config like
; [app]
; ip=127.0.0.1/32 port=5000 protocol=udp
; sg=jump port=22 protocol=ssh
; 
; [jump]
; ip=76.21.101.87 port=22 protocol=ssh
