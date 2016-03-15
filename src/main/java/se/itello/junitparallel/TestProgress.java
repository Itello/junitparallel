package se.itello.junitparallel;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

import java.io.Serializable;

interface TestProgress extends Serializable {

    void passToNotifier(RunNotifier runNotifier);

    class TestStarted implements TestProgress {
        private final Description description;

        public TestStarted(Description description) {
            this.description = description;
        }

        @Override
        public void passToNotifier(RunNotifier runNotifier) {
            runNotifier.fireTestStarted(description);
        }
    }

    class TestFinished implements TestProgress {
        private final Description description;

        public TestFinished(Description description) {
            this.description = description;
        }

        @Override
        public void passToNotifier(RunNotifier runNotifier) {
            runNotifier.fireTestFinished(description);
        }
    }

    class TestFailure implements TestProgress {
        private final Failure failure;

        public TestFailure(Failure failure) {
            this.failure = failure;
        }

        @Override
        public void passToNotifier(RunNotifier runNotifier) {
            runNotifier.fireTestFailure(failure);
        }
    }

    class TestAssumptionFailed implements TestProgress {
        private final Failure failure;

        public TestAssumptionFailed(Failure failure) {
            this.failure = failure;
        }

        @Override
        public void passToNotifier(RunNotifier runNotifier) {
            runNotifier.fireTestAssumptionFailed(failure);
        }
    }

    class TestIgnored implements TestProgress {
        private final Description description;

        public TestIgnored(Description description) {
            this.description = description;
        }

        @Override
        public void passToNotifier(RunNotifier runNotifier) {
            runNotifier.fireTestIgnored(description);
        }
    }

}
