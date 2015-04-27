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
strategy which finds all broken elements and fixes all of them at once, i.e.,
this is a non-incremental strategy.

Depending on the model sizes you want to transform, you can adjust the `-Xmx`
JVM argument in `project.clj`.  The preset value should be enough for all
models up to the size of the `railway-4096.railway` model.

### Running in the train benchmark framework

Deploy this project to your local maven repository using `lein install`.  Then
follow the instruction of
[my fork of the train benchmark framework](https://github.com/tsdh/trainbenchmark-ttc)
which contains a glue project running the FunnyQT solution.  This glue project
contains some Java classes implementing the framework's interfaces.  These
classes simply call the FunnyQT/Clojure solution.

## License

Copyright © 2015 Tassilo Horn <horn@uni-koblenz.de

Distributed under the
[GNU General Public License, version 3](https://www.gnu.org/copyleft/gpl.html),
or at your opinion any later version.
