# The FunnyQT Solution to the TTC 2015 Train Benchmark Case

This project contains the [FunnyQT](http://funnyqt.org) solution to the
[TTC](http://www.transformation-tool-contest.eu/)
[Train Benchmark Case](https://github.com/FTSRG/trainbenchmark-ttc).

## Usage

There are two ways to run the solution: standalone or in the
[train benchmark framework](https://github.com/FTSRG/trainbenchmark-ttc).

In both cases, you need the Clojure build tool
[Leiningen homepage](http://leiningen.org/).  Get the `lein` shell script from
the Leiningen homepage and put it in your `PATH`.

### Running standalone

Just run `lein test`.  That will execute the fixed and proportional strategies
on all railway models in `test/models/`.  Additionally, it'll run another
strategy which find all broken elements and fixes all of them at once.

### Running in the train benchmark framework

Deploy this project to your local maven repository using `lein install`.  Then
follow the instruction of
[my fork of the train benchmark framework](https://github.com/tsdh/trainbenchmark-ttc)
which contains a glue project running the FunnyQT solution.

## License

Copyright © 2015 Tassilo Horn <horn@uni-koblenz.de

Distributed under the
[GNU General Public License, version 3](https://www.gnu.org/copyleft/gpl.html),
or at your opinion any later version.
