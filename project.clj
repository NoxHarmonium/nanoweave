(defproject nanoweave "1.1.9"
  :description "A data transformation tool"
  :url "http://github.com/noxharmonium/nanoweave"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.blancas/kern "1.1.0"]
                 [org.clojure/tools.cli "1.0.219"]
                 [prismatic/schema "1.4.1"]
                 [rhizome "0.2.9"]
                 [cheshire "5.11.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/data.csv "1.0.1"]
                 [org.clojure/tools.macro "0.1.0"]]
  :main ^:skip-aot nanoweave.core
  :target-path "target/%s"
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ["-Djava.awt.headless=true"]
  :plugins []
  :profiles {:uberjar {:aot :all}
             :dev {:resource-paths ["test/resources"]
                   :plugins [[rasom/lein-githooks "0.1.5"]
                             [jonase/eastwood "1.4.0"]
                             [lein-cljfmt "0.9.2"]
                             [com.github.clj-kondo/lein-clj-kondo "0.2.5"]
                             [lein-codox "0.10.8"]]
                   :dependencies [[diff-eq "0.2.5"]]
                   :githooks {:pre-push ["lein check" "lein test"]
                              :pre-commit ["lein eastwood" "lein clj-kondo" "lein cljfmt check"]}}
             :repl {:source-paths ["profiles"]
                    :repl-options {:init-ns repl.repl-env}
                    :dependencies [[org.clojure/tools.namespace "1.3.0"]]}}
  :eastwood {:config-files ["lint_config.clj"]
             :exclude-linters [; The magic of 'declare-extern' requires a call to ns. I need that macro to avoid circular references.
                               :wrong-ns-form
                               ; The defrecord macro expands to a redundant (but harmless) dissoc statement when creating a record with no attributes (in this case NilLit).
                               ; No matter what I do, I can't get this error to be ignored via disable-warning or ignored-faults
                               :suspicious-expression]}
  :cljfmt {:remove-multiple-non-indenting-spaces? true})


