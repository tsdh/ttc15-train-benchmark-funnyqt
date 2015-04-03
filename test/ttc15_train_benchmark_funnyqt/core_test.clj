(ns ttc15-train-benchmark-funnyqt.core-test
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]
            [funnyqt.utils :as u]
            [clojure.test :refer :all]
            [clojure.java.io :as io]
            [ttc15-train-benchmark-funnyqt.core :refer :all]))

(defn all-models []
  (for [^java.io.File f (file-seq (io/file "test/models/"))
        :when (and (.isFile f)
                   (re-matches #".*\.railway$" (.getPath f)))]
    f))

(def rule-test-fns [
                    pos-length-test
                    switch-sensor-test
                    switch-set-test
                    route-sensor-test
                    semaphore-neighbor-test
                    ])

(def runs 2)

(deftest test-all
  (doseq [rule-test rule-test-fns]
    (println "Rule" (u/fn-name rule-test))
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (dotimes [run runs]
        (println "Run:" (inc run))
        (let [g (u/timing "Loading time: %T" (load-resource f))
              _ (println (format "Model size:   %s elements\n              %s refs"
                                 (count (eallcontents g))
                                 (count (epairs g))))
              r (u/timing "Query time:   %T"
                          (rule-test g))]
          (println (format "Match count:  %s" (count r)))
          (u/timing       "Repair time:  %T"
                          (doseq [t r] (t)))))
      (println))))

(defn load-and-check [rule-test-fn f]
  (u/timing "Load & Check:     %T"
            (let [g (load-resource f)]
              [g (rule-test-fn g)])))

(deftest test-fixed
  (doseq [rule-test rule-test-fns]
    (println "Rule" (u/fn-name rule-test) "(fixed)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (dotimes [run runs]
        (println "Run:" (inc run))
        (let [[g results] (load-and-check rule-test f)]
          (u/timing "Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take 10 r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (rule-test g)))))))
      (println))))

(deftest test-proportional
  (doseq [rule-test rule-test-fns]
    (println "Rule" (u/fn-name rule-test) "(proportional)")
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (dotimes [run runs]
        (println "Run:" (inc run))
        (let [[g results] (load-and-check rule-test f)]
          (u/timing "Repair & Recheck: %T"
                    (loop [i 10, r results]
                      (doseq [t (take (/ (count r) 10) r)]
                        (t))
                      (when (pos? i)
                        (recur (dec i) (rule-test g)))))))
      (println))))
