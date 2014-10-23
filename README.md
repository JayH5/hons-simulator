hons-simulator
==============

A common simulator platform based on MASON for our honours project (2014).

### Compile and run:
The project requires Java 8.
```bash
git clone https://github.com/JayH5/hons-simulator.git
cd hons-simulator
./gradlew run
```

To package the project as a single large jar with all dependencies, run `./gradlew fatJar`. The output will be `build/libs/hons-simulator-all.jar`.

### Dependencies
The project dependencies can be seen in `build.gradle`. Two dependencies are included as .jars in `/libs`. This is because:
* MASON is not on any package repository that we know of.
* We forked JBox2D in order to implement ground friction in our top-down environment. See [here](https://github.com/JayH5/jbox2d/tree/topdown).

### Experiments
Each group member has their own experiment set-up.
* To see @xenos5's, checkout the branch `GP`
* To see @mracter's, checkout the branch `hetero_comp`
* To see @JayH5's, see [here](https://github.com/JayH5/hons-experiment)
