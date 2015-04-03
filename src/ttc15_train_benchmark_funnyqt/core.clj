(ns ^{:pattern-expansion-context :emf}
  ttc15-train-benchmark-funnyqt.core
  (:require [funnyqt.emf :refer :all]
            [funnyqt.pmatch :refer :all]
            [funnyqt.in-place :refer :all]))

(try
  (eclassifier 'RailwayContainer)
  (catch Exception e
    (println "Loading railway ecore model...")
    (load-ecore-resource "railway.ecore")))

(def Signal-GO (eenum-literal 'Signal.GO))

(defrule pos-length {:forall true} [g]
  [s<Segment>
   :when (<= (eget-raw s :length) 0)]
  (eset! s :length (inc (- (eget-raw s :length)))))

(defn pos-length-test [g]
  (as-test (pos-length g)))

(defrule switch-sensor {:forall true, :recheck true} [g]
  [sw<Switch> -!<:sensor>-> <>]
  (eset! sw :sensor (ecreate! nil 'Sensor)))

(defn switch-sensor-test [g]
  (as-test (switch-sensor g)))

(defrule switch-set {:forall true, :recheck true} [g]
  [route<Route> -<:entry>-> sem
   :when (= (eget-raw sem :signal) Signal-GO)
   route -<:follows>-> swp -<:switch>-> sw
   :let [cur-pos (eget-raw swp :position)]
   :when (not= (eget-raw sw :currentPosition) cur-pos)]
  (eset! sw :currentPosition cur-pos))

(defn switch-set-test [g]
  (as-test (switch-set g)))

(defrule route-sensor {:forall true, :recheck true, :transducers true} [g]
  [route<Route> -<:follows>-> <> -<:switch>-> sw
   -<:sensor>-> s --!<> route]
  (eunset! sw :sensor))

(defn route-sensor-test [g]
  (as-test (route-sensor g)))

(defrule semaphore-neighbor {:forall true, :recheck true, :transducers true} [g]
  [route1<Route> -<:exit>-> sem
   route1 -<:definedBy>-> <> -<:elements>-> <>
   -<:connectsTo>-> <> -<:sensor>-> <>
   --<> route2<Route> -!<:entry>-> sem
   :when (not= route1 route2)]
  (eunset! route1 :exit))

(defn semaphore-neighbor-test [g]
  (as-test (semaphore-neighbor g)))
