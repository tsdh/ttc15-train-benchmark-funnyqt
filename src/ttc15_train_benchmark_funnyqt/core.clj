(ns ^{:pattern-expansion-context :emf}
  ttc15-train-benchmark-funnyqt.core
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]
            [funnyqt.utils :refer [errorf]]))

;; Load the metamodel if needed.
(try
  (eclassifier 'RailwayContainer)
  (catch Exception e
    (println "Loading railway ecore model...")
    (load-ecore-resource "railway.ecore")))

;;* Rules

(defrule ^:forall ^:recheck pos-length [g]
  [segment<Segment>
   :when (<= (eget-raw segment :length) 0)]
  (eset! segment :length (inc (- (eget-raw segment :length)))))

(defrule ^:forall ^:recheck switch-sensor [g]
  [sw<Switch> -!<:sensor>-> <>]
  (eset! sw :sensor (ecreate! nil 'Sensor)))

(def Signal-GO (eenum-literal 'Signal.GO))

(defrule ^:forall ^:recheck switch-set [g]
  [route<Route> -<:entry>-> semaphore
   :when (= (eget-raw semaphore :signal) Signal-GO)
   route -<:follows>-> swp -<:switch>-> sw
   :let [swp-pos (eget-raw swp :position)]
   :when (not= (eget-raw sw :currentPosition) swp-pos)]
  (eset! sw :currentPosition swp-pos))

(defrule ^:forall ^:recheck route-sensor [g]
  [route<Route> -<:follows>-> swp -<:switch>-> sw
   -<:sensor>-> sensor --!<> route]
  (eadd! route :definedBy sensor))

(defrule ^:forall ^:recheck semaphore-neighbor [g]
  [route1<Route> -<:exit>-> semaphore
   route1 -<:definedBy>-> sensor1 -<:elements>-> te1
   -<:connectsTo>-> te2 -<:sensor>-> sensor2
   --<> route2<Route> -!<:entry>-> semaphore
   :when (not= route1 route2)]
  (eset! route2 :entry semaphore))

(defn call-rule-as-test [r g]
  (as-test (r g)))

;;* Match Comparator

(defn make-match-comparator [& kws]
  (fn [t1 t2]
    (loop [kws kws]
      (if (seq kws)
        (let [m1 ((first kws) (:match (meta t1)))
              m2 ((first kws) (:match (meta t2)))]
          (let [r (compare (eget m1 :id) (eget m2 :id))]
            (if (zero? r)
              (recur (rest kws))
              r)))
        (errorf "These two matches compare to zero: %s %s"
                (:match (meta t1))
                (:match (meta t2)))))))
