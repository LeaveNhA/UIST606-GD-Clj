(ns gd.calculation)

(defn min-max-normalization [gds data-key]
  (let [data' (data-key gds)
        dmax (apply max data')
        dmin (apply min data')
        diff-of-max-min (- dmax dmin)]
    (vec (map (comp identity #(/ (-' % dmin) diff-of-max-min)) data'))))

(defn YP [{a :a b :b X :X}]
  (vec (map #(with-precision 3 (+ a (* b %)))
            X)))

(defn error [{Y :Y YP :YP}]
  (vec (map #(with-precision 3 (* 1/2 (Math/pow (- % %2) 2)))
            Y, YP)))

(defn derr-a [{Y :Y YP :YP}]
  (vec (map #(with-precision 3 :rounding HALF_UP
               (- (- % %2)))
            Y, YP)))

(defn derr-b [{Y :Y YP :YP X :X}]
  (vec (map #(with-precision 3 :rounding HALF_UP
               (* (- (- % %2)) %3))
            Y, YP, X)))

(defn new-a [{a  :a
              r  :learning-rate
              aΔ :total-derr-a}]
  (- a (* r aΔ)))

(defn new-b [{b  :b
              r  :learning-rate
              bΔ :total-derr-b}]
  (- b (* r bΔ)))
