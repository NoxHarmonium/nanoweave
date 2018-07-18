(defproject jweave "0.1.0-SNAPSHOT"
  :description "A data transformation tool"
  :url "http://github.com/noxharmonium/jweave"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.blancas/kern "1.1.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.clojure/algo.generic "0.1.2"]
                 [prismatic/schema "1.1.9"]]
  :main ^:skip-aot jweave.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
