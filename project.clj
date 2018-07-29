(defproject nanoweave "0.1.0-SNAPSHOT"
  :description "A data transformation tool"
  :url "http://github.com/noxharmonium/nanoweave"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.blancas/kern "1.1.0"]
                 [org.clojure/tools.cli "0.3.7"]
                 [prismatic/schema "1.1.9"]
                 [org.clojure/data.json "0.2.6"]
                 [rhizome "0.2.9"]]
  :main ^:skip-aot nanoweave.core
  :target-path "target/%s"
  :jvm-opts ["-Djava.awt.headless=true"]
  :plugins [[lein-zprint "0.3.9"]]
  :profiles {
             :uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]}
             }
  )
