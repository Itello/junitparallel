# junitparallel
Possibility to run JUnit test across multiple JVM:s as a Suite class.

## Why?
Because writing test that can truly run in parallel is hard. Especially if you
have any kind of global state, like a database or static variables. And of course
because waiting for slow tests is annoying.

## How?

To run your test suite in parallel across multiple JVM:s. Mark your suite with 
the @RunWith annotation and let ParallelProcessSuite be the runner.
```Java
@RunWith(ParallelProcessSuite.class)
@Suite.SuiteClasses({
  TestA.class, TestB.class, TestC.class // ...
})
public class TestParallelProcessSuite {
}
```

You can also run all tests across one or multiple packages using a ParallelProcessCpSuite. This uses the excellent cpsuite project. Check out the documentation for that project for instructions on how to specify what classes to run (https://github.com/takari/takari-cpsuite). 
```Java
@RunWith(ParallelProcessCpSuite.class)
@ClasspathSuite.ClassnameFilters({
        "se.itello.foo.*\\.*",
        "se.itello.bar.*\\.*"
})
public class ExampleSuite {
}
```

### Customizations
You can make a couple of configurations to the parallel runner. Check out the javadoc for ParallelProcessSuiteConfig for more details.

Example:
```Java
@RunWith(ParallelProcessCpSuite.class)
@ParallelProcessSuiteConfig.NumberOfCores(3)
@ParallelProcessSuiteConfig.WhenNewProcessCreated(RunThisForEachFork.class)
@ParallelProcessSuiteConfig.JvmArgs(MaxMemorySettings.class)
@ClasspathSuite.ClassnameFilters({
        "se.itello.*"
})
public class ExampleSuite {
}
```

## License
This project uses Apache License 2.0.

## Disclaimer 1
The runner adds some overhead to each test. If your tests are already fast, this overhead might
actually make your suite slower. But if your tests were already fast then this project
is probably not for you anyway.

## Disclaimer 2
There is currently a bug in the latest version of Intellij (15) that makes this project difficult to use.

https://youtrack.jetbrains.com/issue/IDEA-148602
