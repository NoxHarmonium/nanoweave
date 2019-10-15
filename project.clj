(defproject nanoweave "1.0.0"
  :description "A data transformation tool"
  :url "http://github.com/noxharmonium/nanoweave"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.blancas/kern "1.1.0"]
                 [org.clojure/tools.cli "0.4.2"]
                 [prismatic/schema "1.1.9"]
                 [rhizome "0.2.9"]
                 [org.clojure/tools.namespace "0.2.7"]
                 [diff-eq "0.2.3"]
                 [cheshire "5.8.1"]]
  :main ^:skip-aot nanoweave.core
  :target-path "target/%s"
  :jvm-opts ["-Djava.awt.headless=true"]
  :plugins [[lein-codox "0.10.4"]
            [cider/cider-nrepl "0.21.1"]]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]}}
  :eastwood {:config-files ["lint_config.clj"]})


