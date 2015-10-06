# junitparallel
Some strategies to run JUnit test suites in parallel. **Still very beta**. 

Current strategies are:
* Run tests in multiple threads
* Run tests across multiple JVM:s

## How?

To run your test suite in parallel using one JVM with multiple threads. Mark your 
suite with the @RunWith annotation and let ParallelSuite be the runner.
```Java
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
  TestA.class, TestB.class, TestC.class // ... 
})
public class ThreadedSuite {
}
```

To run your test suite in parallel across multiple JVM:s. Mark your suite with 
the @RunWith annotation and let ParallelProcessSuite be the runner.
```Java
@RunWith(ParallelProcessesSuite.class)
@Suite.SuiteClasses({
  TestA.class, TestB.class, TestC.class // ...
})
public class TestParallelProcessSuite {
}
```
