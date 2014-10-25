(ns ops.config
  (:gen-class))

(require '[instaparse.core :as parse])

(def config-parser
  (parse/parser
    "<RULES> = (IP-ADDRESS | SECURITY-GROUP) [<WS> PORT-RANGE] [<WS> PROCESS]
    SECURITY-GROUP = <'sg'> <WS-EQ> SG
    <SG> = SG-CLASSIC | SG-VPC 
    <SG-CLASSIC> = #\"\\w{1,255}\"
    <SG-VPC> = #\"\\w{1,255}\"
    IP-ADDRESS = <'ip'> <WS-EQ> IP 
    PROCESS = <'process'> <WS-EQ> TYPE
    WS-EQ = [WS] EQ [WS]
    WS = #\"\\s+\"
    EQ = #\"\\s*=\\s*\"
    <IP> = #\"\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\\.\\d{0,3}\"
    PORT-RANGE = <'port'> <WS-EQ> PORT
    <PORT> = PORT-REG [<'-'> PORT-REG]
    <PORT-REG> = #\"\\d+\"
    <TYPE> = #\"\\w+\""))

(defn config-parse-line
  [line]
  (let [[[ip-or-sg identifier] [_ port-from port-to] [_ process]] (config-parser line)]
   {:type ip-or-sg :identifier identifier :port-from port-from :port-to port-to :process process})) 

; config like
; [app]
; ip=127.0.0.1 port=5000 process=udp
; sg=jump port=22 process=ssh
; 
; [jump]
; ip=76.21.101.87 port=22 process=ssh
