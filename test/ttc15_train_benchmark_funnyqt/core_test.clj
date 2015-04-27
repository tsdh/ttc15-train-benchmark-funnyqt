(ns ttc15-train-benchmark-funnyqt.core-test
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]
            [funnyqt.utils :as u]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ttc15-train-benchmark-funnyqt.core :refer :all]))

(def model-sizes (let [min 0   ;; min/max as log_2, e.g., max = 10 ==> 1024,
                       max 10] ;; min = 0 ==> 1
                   (drop min (take (inc max) (iterate #(* 2 %) 1)))))

(def runs 1)

(def rules [
            pos-length
            switch-sensor
            switch-set
            route-sensor
            semaphore-neighbor
            ])

(defn model-regexp []
  (re-pattern (str ".*-("
                   (str/join "|" (map str model-sizes))
                   ")\\.railway$")))

(defn all-models []
  (sort (fn [^java.io.File a ^java.io.File b]
          (if (< (count (.getName a))
                 (count (.getName b)))
            -1
            (if (> (count (.getName a))
                   (count (.getName b)))
              1
              (compare (.getName a) (.getName b)))))
        (filter (fn [^java.io.File f]
                  (and (.isFile f)
                       (re-matches (model-regexp) (.getPath f))))
                (file-seq (io/file "test/models/")))))

(defn load-and-check [rule f]
  (u/timing "      Load & Check:          %T"
            (let [g (load-resource f)]
              [g (call-rule-as-test rule g)])))

(deftest test-fixed
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule) "(fixed strategy)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [[g results] (load-and-check rule f)]
          (u/timing "      10 x Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take 10 r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (call-rule-as-test rule g)))))))
      (println))))

(deftest test-proportional
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule) "(proportional strategy)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [[g results] (load-and-check rule f)]
          (u/timing "      10 x Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take (/ (count r) 10) r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (call-rule-as-test rule g)))))))
      (println))))

(deftest test-non-incremental
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule) "(non-incremental strategy)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [g (u/timing "      Loading time: %T" (load-resource f))
              _ (println (format "      Model size:   %s elements\n                    %s refs"
                                 (count (eallcontents g))
                                 (count (epairs g))))
              r (u/timing "      Query time:   %T"
                          (call-rule-as-test rule g))]
          (println (format "      Match count:  %s" (count r)))
          (u/timing       "      Repair time:  %T"
                          (doseq [t r] (t)))))
      (println))))
