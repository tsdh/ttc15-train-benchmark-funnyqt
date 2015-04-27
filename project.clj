(defproject ttc15-train-benchmark-funnyqt "0.1.0-SNAPSHOT"
  :description "The FunnyQT solution to the TTC15 TrainBenchmark case."
  :url "https://github.com/tsdh/ttc15-train-benchmark-funnyqt"
  :license {:name "GNU General Public License, Version 3 (or later)"
            :url "http://www.gnu.org/licenses/gpl.html"
            :distribution :repo}
  :dependencies [[funnyqt "0.46.3"]]
  :global-vars {*warn-on-reflection* true}
  :jvm-opts ^:replace ["-Xmx8G"])
