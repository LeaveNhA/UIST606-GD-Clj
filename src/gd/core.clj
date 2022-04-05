(ns gd.core
  (:require
   [clojure.pprint  :refer [pprint]]
   [gd.hw.data      :refer [homework-data a b]]
   [gd.gds          :refer [calculate-gd]])
  (:gen-class))

(defn -main [& args]
  (let [output-of-calculation (with-out-str
                                (pprint
                                 (take 2
                                       (calculate-gd :a a
                                                     :b b
                                                     :data homework-data))))]
    (println output-of-calculation)))
