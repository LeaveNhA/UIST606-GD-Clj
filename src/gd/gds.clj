(ns gd.gds
  [:require [gd.calculation :refer [min-max-normalization
                                    new-a new-b
                                    derr-a derr-b
                                    error
                                    YP]]])

;; Everything starts with data in CS:
(defn ->gds
  "GD structure on Clojure.
   Use to generate salt GD map to process."
  []
  {:learning-rate 0.01
   :tolerance 0.001
   :iteration-limit 1000
   :iteration 0

   :a (rand) :b (rand)

   :data []

   :X-data []
   :Y-data []
   :X []
   :Y []
   :YP []
   :Err []
   :Err-total 0})

;; And Domain functions:
(defn insert->gds
  "GDS -> keyword -> fn -> GDS"
  [gds key fn]
  ((comp #(assoc (first %) key (second %))
         (juxt identity
               fn)) gds))

(def insert-data
  "Domain data -> GDS -> GDS'"
  #(assoc %2 :data %))

(def init-a-b
  "A value -> B value -> GDS -> GDS'"
  #(assoc % :a %2 :b %3))

(def X-data
  "GDS{:data ,,} -> GDS{:X-data ,,}
   Extract X-data from :data and put it to :X-data key."
  #(insert->gds % :X-data (comp vec (partial map first) :data)))

(def Y-data
  "GDS{:data ,,} -> GDS{:Y-data ,,}
   Extract Y-data from :data and put it to :Y-data key.
   Domain is too small to create a handler function
   to balance complexity<->simplicity."
  #(insert->gds % :Y-data (comp vec (partial map first) :data)))

(def calculate-YP
  "GDS{:a ,,
       :b ,,
       :X ,,} -> GDS{:YP ,,}
   Calculates the YP on the given GDS."
  #(insert->gds % :YP YP))

(def calculate-error
  ""
  #(insert->gds % :Err error))

(def calculate-total-error
  ""
  #(insert->gds % :Err-total (comp (partial reduce +) :Err)))

(def calculate-X
  "GDS{:X-data ,,} -> GDS{:X ,,}
   Calculate X data from :X-data on GDS."
  #(insert->gds % :X (partial min-max-normalization :X-data)))

(def calculate-Y
  "GDS{:Y-data ,,} -> GDS{:Y ,,}
   Calculate Y data from :Y-data on GDS."
  #(insert->gds % :Y (partial min-max-normalization :Y-data)))

(def calculate-derr-a
  "GDS{:Y  ,,
       :YP ,,}
   ->
   GDS{:derr-a ,,}"
  #(insert->gds % :derr-a derr-a))

(def calculate-derr-b
  "GDS{:Y  ,,
       :YP ,,
       :X  ,,}
   ->
   GDS{:derr-b ,,}"
  #(insert->gds % :derr-b derr-b))

(def calculate-total-derr-s
  "GDS{derr-x ,,} -> GDS{total-derr-x ,,}"
  #(-> %
       (insert->gds :total-derr-a
                    (comp (partial reduce +)
                          :derr-a))
       (insert->gds :total-derr-b
                   (comp (partial reduce +)
                         :derr-b))))

(def calculate-new-a
  #(insert->gds % :a new-a))

(def calculate-new-b
  #(insert->gds % :b new-b))

(defn calculate-gradient
  [gds]
  (-> gds
      calculate-derr-a
      calculate-derr-b
      calculate-total-derr-s
      calculate-new-a
      calculate-new-b
      calculate-YP))

(defn gradient-calculation
  "Error function ⨔ over Domain ->
   Gradient function ⨔ over Domain ->
   GDS -> GDS{CALCULATED}"
  [error-fn gradient-fn
   gds]
  (-> gds
      gradient-fn
      error-fn))

(defn continue?
  "[GDS] -> Boolean
   It takes collection of previously calculated GDS-es and
   and calculate/return if the calculation margin met or not."
  [gdss]
  (let [diff-of-errors (Math/abs (apply -
                                   (map :Err-total (take 2 gdss))))
        tolerance (-> gdss first :tolerance)]
    #_(println "DEBUG: diff-of-errors:" diff-of-errors
             (<= tolerance diff-of-errors))
    (<= tolerance diff-of-errors)))

(def inc-iteration
  #(insert->gds % :iteration (comp inc :iteration)))

(defn iteration-continue? [gds]
  (-> gds
      ((juxt :iteration-limit :iteration))
      vec
      ((partial reduce -))
      pos?))

#_(iteration-continue? (->gds))

(defn calculation-loop
  ([initial-gds] (calculation-loop (list initial-gds) 1))
  ([[gds & gdss] counter] (let [new-gds (-> gds
                                         ((partial gradient-calculation
                                                   (comp calculate-total-error
                                                         calculate-error)
                                                   calculate-gradient))
                                         inc-iteration)]
                         (if (and (continue? [new-gds gds])
                                  (iteration-continue? new-gds))
                           (calculation-loop (conj gdss gds new-gds)
                                             #_short-cur-flag?
                                             (inc counter))
                           #_[new-gds gds gdss counter]
                           (conj gdss gds)))))

(defn calculate-gd
  "Logic function to handle everything. Yes, everything.
   Input, Calculation and Output."
  [& {:as input-gds}]
  (let [initial-gds (->gds)]
    (-> initial-gds
        (merge input-gds)
        X-data
        Y-data
        calculate-X
        calculate-Y
        calculation-loop)))
