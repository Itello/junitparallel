package se.plilja.junitparallel.process;

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
        ipc.sendObject(new TestProgress.TestFailure(failure));
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        try {
            ipc.sendObject(new TestProgress.TestAssumptionFailed(failure));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        ipc.sendObject(new TestProgress.TestIgnored(description));
    }

}
