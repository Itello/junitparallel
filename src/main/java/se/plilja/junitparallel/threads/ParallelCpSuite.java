package se.plilja.junitparallel.threads;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

public class ParallelCpSuite extends ClasspathSuite {
    public ParallelCpSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
        super(suiteClass, builder);
        setScheduler(new ParallelRunnerScheduler());
    }

}
