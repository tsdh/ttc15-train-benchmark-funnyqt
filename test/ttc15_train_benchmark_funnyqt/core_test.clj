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

(deftest test-all
  (doseq [rule [pos-length switch-sensor switch-set route-sensor semaphore-neighbor]]
    (println "Rule" (u/fn-name rule))
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (let [g (u/timing "Loading time: %T" (load-resource f))
            _ (println (format "Model size:   %s elements\n              %s refs"
                               (count (eallcontents g))
                               (count (epairs g))))
            r (u/timing "Query time:   %T"
                        (as-test (rule g)))]
        (println (format "Match count:  %s" (count r)))
        (u/timing       "Repair time:  %T"
                        (doseq [t r] (t))))
      (println))))

(deftest test-fixed
  (doseq [rule [pos-length switch-sensor switch-set route-sensor semaphore-neighbor]]
    (println "Rule" (u/fn-name rule))
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (let [g (u/timing "Loading time: %T" (load-resource f))]
        (println (format "Model size:   %s elements\n              %s refs"
                         (count (eallcontents g))
                         (count (epairs g))))
        (u/timing "Fixed Q/Rs:   %T"
                  (loop [i 10, r (as-test (rule g))]
                    (doseq [t (take 10 r)]
                      (t))
                    (when (pos? i)
                      (recur (dec i) (as-test (rule g)))))))
      (println))))

(deftest test-proportional
  (doseq [rule [pos-length switch-sensor switch-set route-sensor semaphore-neighbor]]
    (println "Rule" (u/fn-name rule))
    (println "====")
    (doseq [^java.io.File f (all-models)]
      (println "File:" (.getPath f))
      (let [g (u/timing "Loading time: %T" (load-resource f))]
        (println (format "Model size:   %s elements\n              %s refs"
                         (count (eallcontents g))
                         (count (epairs g))))
        (u/timing "Prop. Q/Rs:   %T"
                  (loop [i 10, r (as-test (rule g))]
                    (doseq [t (take (/ (count r) 10) r)]
                      (t))
                    (when (pos? i)
                      (recur (dec i) (as-test (rule g)))))))
      (println))))
