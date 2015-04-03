(ns ^{:pattern-expansion-context :emf}
  ttc15-train-benchmark-funnyqt.core
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]))

;; Load the metamodel if needed.
(try
  (eclassifier 'RailwayContainer)
  (catch Exception e
    (println "Loading railway ecore model...")
    (load-ecore-resource "railway.ecore")))

(def Signal-GO (eenum-literal 'Signal.GO))

;;* Rules

(defrule ^:forall ^:recheck pos-length [g]
  [s<Segment>
   :when (<= (eget-raw s :length) 0)]
  (eset! s :length (inc (- (eget-raw s :length)))))

(defrule ^:forall ^:recheck switch-sensor [g]
  [sw<Switch> -!<:sensor>-> <>]
  (eset! sw :sensor (ecreate! nil 'Sensor)))

(defrule ^:forall ^:recheck switch-set [g]
  [route<Route> -<:entry>-> sem
   :when (= (eget-raw sem :signal) Signal-GO)
   route -<:follows>-> swp -<:switch>-> sw
   :let [cur-pos (eget-raw swp :position)]
   :when (not= (eget-raw sw :currentPosition) cur-pos)]
  (eset! sw :currentPosition cur-pos))

(defrule ^:forall ^:recheck ^:transducers route-sensor [g]
  [route<Route> -<:follows>-> <> -<:switch>-> sw
   -<:sensor>-> s --!<> route]
  (eunset! sw :sensor))

(defrule ^:forall ^:recheck ^:transducers semaphore-neighbor [g]
  [route1<Route> -<:exit>-> sem
   route1 -<:definedBy>-> <> -<:elements>-> <>
   -<:connectsTo>-> <> -<:sensor>-> <>
   --<> route2<Route> -!<:entry>-> sem
   :when (not= route1 route2)]
  (eunset! route1 :exit))

;;* Functions for applying the rules as tests

(defn pos-length-test [g]
  (as-test (pos-length g)))

(defn switch-sensor-test [g]
  (as-test (switch-sensor g)))

(defn switch-set-test [g]
  (as-test (switch-set g)))

(defn route-sensor-test [g]
  (as-test (route-sensor g)))

(defn semaphore-neighbor-test [g]
  (as-test (semaphore-neighbor g)))

;;* Match Comparator

(defn match-comparator [t1 t2]
  (loop [m1 (vals (:match (meta t1)))
         m2 (vals (:match (meta t2)))]
    (if (seq m1)
      (let [r (compare (eget (first m1) :id)
                       (eget (first m2) :id))]
        (if (zero? r)
          (recur (rest m1) (rest m2))
          r))
      (funnyqt.utils/errorf "These two matches compare to zero: %s %s"
                            (:match (meta t1))
                            (:match (meta t2))))))
