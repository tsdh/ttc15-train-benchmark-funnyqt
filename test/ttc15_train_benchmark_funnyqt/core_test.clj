(ns ttc15-train-benchmark-funnyqt.core-test
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]
            [funnyqt.utils :as u]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [ttc15-train-benchmark-funnyqt.core :refer :all]))

(def model-sizes (take 13 (iterate #(* 2 %) 1)))
(def runs 2)
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
          (u/pr-identity (format "%s %s => " (.getName a) (.getName b))
                         (if (< (count (.getName a))
                                (count (.getName b)))
                           -1
                           (if (> (count (.getName a))
                                  (count (.getName b)))
                             1
                             (compare (.getName a) (.getName b))))))
        (filter (fn [^java.io.File f]
                  (and (.isFile f)
                       (re-matches (model-regexp) (.getPath f))))
                (file-seq (io/file "test/models/")))))

(deftest test-all
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule))
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [g (u/timing "    Loading time: %T" (load-resource f))
              _ (println (format "    Model size:   %s elements\n              %s refs"
                                 (count (eallcontents g))
                                 (count (epairs g))))
              r (u/timing "    Query time:   %T"
                          (call-rule-as-test rule g))]
          (println (format "    Match count:  %s" (count r)))
          (u/timing       "    Repair time:  %T"
                          (doseq [t r] (t)))))
      (println))))

(defn load-and-check [rule f]
  (u/timing "    Load & Check:     %T"
            (let [g (load-resource f)]
              [g (call-rule-as-test rule g)])))

(deftest test-fixed
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule) "(fixed)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [[g results] (load-and-check rule f)]
          (u/timing "    Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take 10 r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (call-rule-as-test rule g)))))))
      (println))))

(deftest test-proportional
  (doseq [rule rules]
    (println "Rule" (u/fn-name rule) "(proportional)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "  File:" (.getPath f))
      (dotimes [run runs]
        (println "    Run:" (inc run))
        (System/gc)
        (let [[g results] (load-and-check rule f)]
          (u/timing "    Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take (/ (count r) 10) r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (call-rule-as-test rule g)))))))
      (println))))
