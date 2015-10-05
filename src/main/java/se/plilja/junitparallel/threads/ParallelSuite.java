package se.plilja.junitparallel.threads;

import org.junit.runners.Suite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ParallelSuite extends Suite {

    public ParallelSuite(Class<?> clazz, RunnerBuilder builder) throws InitializationError {
        super(clazz, builder);
        setScheduler(new ParallelRunnerScheduler());
    }
}