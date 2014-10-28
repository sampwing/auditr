(defproject ops "0.1.0-SNAPSHOT"
  :description "audit configurations"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.clojure/tools.cli "0.3.1"]
                 [amazonica "0.2.26"]
                 [instaparse "1.3.4"]]
  :main ^:skip-aot auditr.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
