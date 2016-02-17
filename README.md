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

## Disclaimer 1
The runner adds some overhead to each test. If your tests are already fast, this overhead might
actually make your suite slower. But if your tests were already fast then this project
is probably not for you anyway.

## Disclaimer 2
There is currently a bug in the latest version of Intellij (15) that makes this project difficult to use.

https://youtrack.jetbrains.com/issue/IDEA-148602