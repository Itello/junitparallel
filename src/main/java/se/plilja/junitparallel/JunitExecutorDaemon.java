package se.plilja.junitparallel;

import junit.framework.AssertionFailedError;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.io.IOException;
import java.util.Optional;

public class JunitExecutorDaemon {
    private static class Code {
        static int TEST_CLASS_FINISHED = 1;
        static int TEST_STARTED = 2;
        static int SUCCESS = 3;
        static int FAILURE = 4;
        static int IGNORED = 5;
        static int ASSUMPTION_FAILED = 6;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        try (InterProcessCommunication ipc = InterProcessCommunication.createServer(port)) {

            while (true) {
                String testClassName = ipc.receiveMessage();
                System.out.println(testClassName);
                Class<?> testClass = Class.forName(testClassName.trim());
                JUnitCore jUnitCore = new JUnitCore();
                jUnitCore.addListener(new InterProcessListener(ipc));
                jUnitCore.run(testClass);
                ipc.sendMessage(InterProcessCommunication2.generateMessage(testClass, Optional.empty(), Code.TEST_CLASS_FINISHED));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private static class InterProcessListener extends RunListener {
        private final InterProcessCommunication ipc;

        public InterProcessListener(InterProcessCommunication ipc) {
            this.ipc = ipc;
        }

        @Override
        public void testStarted(Description description) throws Exception {
            ipc.sendMessage(InterProcessCommunication2.generateMessage(description.getTestClass(), Optional.of(description.getMethodName()), Code.TEST_STARTED));
        }

        @Override
        public void testFinished(Description description) throws Exception {
            ipc.sendMessage(InterProcessCommunication2.generateMessage(description.getTestClass(), Optional.of(description.getMethodName()), Code.SUCCESS));
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            Description description = failure.getDescription();
            ipc.sendMessage(InterProcessCommunication2.generateMessage(description.getTestClass(), Optional.of(description.getMethodName()), Code.FAILURE));
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            try {
                Description description = failure.getDescription();
                ipc.sendMessage(InterProcessCommunication2.generateMessage(description.getTestClass(), Optional.of(description.getMethodName()), Code.ASSUMPTION_FAILED));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            ipc.sendMessage(InterProcessCommunication2.generateMessage(description.getTestClass(), Optional.of(description.getMethodName()), Code.IGNORED));
        }


    }

    public static class InterProcessCommunication2 {

        public static void sendMessageToNotifier(String message, RunNotifier runNotifier) throws Exception {
            String[] args = message.split(" ");
            int code = Integer.parseInt(args[0]);
            Class<?> clazz = Class.forName(args[1]);
            if (args.length >= 3) {
                String method = args[2];
                Description testDescription = Description.createTestDescription(clazz, method);
                if (code == Code.TEST_STARTED) {
                    runNotifier.fireTestStarted(testDescription);
                } else if (code == Code.SUCCESS) {
                    runNotifier.fireTestFinished(testDescription);
                } else if (code == Code.FAILURE) {
                    RuntimeException ex = new RuntimeException();
                    Failure failure = new Failure(testDescription, ex /* TODO recreate exception */);
                    runNotifier.fireTestFailure(failure);
                } else if (code == Code.ASSUMPTION_FAILED) {
                    Failure failure = new Failure(testDescription, new AssertionFailedError());
                    runNotifier.fireTestAssumptionFailed(failure);
                } else if (code == Code.IGNORED) {
                    runNotifier.fireTestIgnored(testDescription);
                }
            }
        }

        public static boolean isTestFinished(String message) {
            String[] args = message.split(" ");
            int code = Integer.parseInt(args[0]);
            return code == Code.TEST_CLASS_FINISHED;
        }

        public static String generateMessage(Class<?> testClass, Optional<String> method, int code) {
            if (method.isPresent()) {
                return String.format("%d %s %s", code, testClass.getName(), method.get());
            } else {
                return String.format("%d %s", code, testClass.getName());
            }
        }
    }
}
