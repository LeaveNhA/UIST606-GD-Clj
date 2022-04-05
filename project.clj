(defproject gd "0.1"
  :description "A naive Gradient Descent implementation for UIST606 homework."
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :profiles {:uberjar {:aot :all}
             :prod {:resource-paths ["src/main/resources"]}}
  :main gd.core
  :repl-options {:init-ns gd.core})
