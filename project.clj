(defproject nanoweave "1.0.2"
  :description "A data transformation tool"
  :url "http://github.com/noxharmonium/nanoweave"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.blancas/kern "1.1.0"]
                 [org.clojure/tools.cli "1.0.206"]
                 [prismatic/schema "1.3.0"]
                 [rhizome "0.2.9"]
                 [org.clojure/tools.namespace "0.3.1"]
                 [diff-eq "0.2.5"]
                 [cheshire "5.11.0"]]
  :main ^:skip-aot nanoweave.core
  :target-path "target/%s"
  :jvm-opts ["-Djava.awt.headless=true"]
  :plugins [[lein-codox "0.10.8"]
            [cider/cider-nrepl "0.28.5"]]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]}}
  :eastwood {:config-files ["lint_config.clj"]})


