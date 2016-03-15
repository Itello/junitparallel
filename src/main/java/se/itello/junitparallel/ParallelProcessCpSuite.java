package se.itello.junitparallel;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class ParallelProcessCpSuite extends ClasspathSuite {
    private final Class<?> suiteClass;

    public ParallelProcessCpSuite(Class<?> suiteClass, RunnerBuilder builder) throws InitializationError {
        super(suiteClass, builder);
        this.suiteClass = suiteClass;
    }

    @Override
    public void run(RunNotifier notifier) {
        final List<Class<?>> testClasses = getChildren().stream()
                .map(r -> r.getDescription().getTestClass())
                .collect(toList());
        new ParallelProcessSuite(suiteClass).runTests(notifier, testClasses);
    }
}
