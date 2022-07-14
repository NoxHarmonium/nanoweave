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
  :plugins [[lein-codox "0.10.8"]]
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]
                   :plugins [[lein-githooks "0.1.1"]
                             [jonase/eastwood "1.2.4"]
                             [lein-cljfmt "0.8.2"]]
                   :githooks {:pre-push ["lein test"]
                              :pre-commit ["lein eastwood", "lein cljfmt fix"]}}}
  :eastwood {:config-files ["lint_config.clj"]

             :exclude-linters [; The magic of 'declare-extern' requires a call to ns. I need that macro to avoid circular references.
                               :wrong-ns-form
                               ; The defrecord macro expands to a redundant (but harmless) dissoc statement when creating a record with no attributes (in this case NilLit).
                               ; No matter what I do, I can't get this error to be ignored via disable-warning or ignored-faults
                               :suspicious-expression]})


