(ns gd.hw.data
  (:require
   [clojure.java.io :as io]
   [clojure.edn     :as edn]))

(def homework-st
  (-> "public/homework.data.edn"
      io/resource
      slurp
      edn/read-string))

(def homework-data
  (-> homework-st :data))

(def a
  "a value from the specifications of HomeWork."
  (-> homework-st :a))

(def b
  "b value from the specifications of HomeWork."
  (-> homework-st :b))
