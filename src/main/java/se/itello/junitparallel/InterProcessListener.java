package se.itello.junitparallel;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.IOException;

import static se.itello.junitparallel.Util.isSerializable;

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
        TestProgress.TestFailure primary = new TestProgress.TestFailure(failure);
        TestProgress.TestFailure backup = new TestProgress.TestFailure(new Failure(failure.getDescription(), createBackupException(failure)));
        sendOrBackup(primary, backup);
    }

    @Override
    public void testAssumptionFailure(Failure failure) {
        try {
            TestProgress.TestAssumptionFailed primary = new TestProgress.TestAssumptionFailed(failure);
            TestProgress.TestAssumptionFailed backup = new TestProgress.TestAssumptionFailed(new Failure(failure.getDescription(), createBackupException(failure)));
            sendOrBackup(primary, backup);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private RuntimeException createBackupException(Failure failure) {
        RuntimeException re = new RuntimeException(failure.getMessage());
        re.setStackTrace(failure.getException().getStackTrace());
        return re;
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        ipc.sendObject(new TestProgress.TestIgnored(description));
    }

    private void sendOrBackup(Object primary, Object backup) throws IOException, ClassNotFoundException {
        if (isSerializable(primary)) {
            ipc.sendObject(primary);
        } else {
            ipc.sendObject(backup);
        }
    }

}
