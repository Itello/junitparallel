package se.itello.junitparallel;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

class InterProcessListener extends RunListener {
    private final InterProcessCommunication ipc;

    public InterProcessListener(InterProcessCommunication ipc) {
        this.ipc = ipc;
    }

    @Override
    public void testStarted(Description description) throws Exception {
        ipc.sendObject(new TestProgress.TestStarted(description));
    }

    @Override
    public void testFinished(Description description) throws Exception {
        ipc.sendObject(new TestProgress.TestFinished(description));
    }

    @Override
    public void testFailure(Failure failure) throws Exception {
        TestProgress.TestFailure primary = new TestProgress.TestFailure(convertToSerializable(failure));
        ipc.sendObject(primary);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        try {
            TestProgress.TestAssumptionFailed primary = new TestProgress.TestAssumptionFailed(convertToSerializable(failure));
            ipc.sendObject(primary);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
    private Failure convertToSerializable(Failure failure) {
        Throwable originalException = failure.getException();
        Throwable re;
        if (originalException instanceof AssertionError) {
            re = new AssertionError(originalException.getMessage());
        } else {
            re = new RuntimeException(originalException.getMessage());
        }
        re.setStackTrace(originalException.getStackTrace());
        return new Failure(failure.getDescription(), re);
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        ipc.sendObject(new TestProgress.TestIgnored(description));
    }

}
