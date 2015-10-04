package se.plilja.junitparallel;

import junit.framework.AssertionFailedError;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
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
        ServerSocket server = null;
        Socket socket = null;
        try {
            int port = Integer.parseInt(args[0]);
            server = new ServerSocket(port);
            socket = server.accept();
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (true) {
                String testClassName = br.readLine();
                Class<?> testClass = Class.forName(testClassName.trim());
                JUnitCore jUnitCore = new JUnitCore();
                jUnitCore.addListener(new InterProcessListener(socket));
                jUnitCore.run(testClass);
                InterProcessCommunication.writeMessage(socket, testClass, Optional.empty(), Code.TEST_CLASS_FINISHED);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (server != null && !server.isClosed()) {
                server.close();
            }
        }

    }

    private static class InterProcessListener extends RunListener {
        private Socket outSocket;

        public InterProcessListener(Socket outSocket) {
            this.outSocket = outSocket;
        }

        @Override
        public void testStarted(Description description) throws Exception {
            InterProcessCommunication.writeMessage(outSocket, description.getTestClass(), Optional.of(description.getMethodName()), Code.TEST_STARTED);
        }

        @Override
        public void testFinished(Description description) throws Exception {
            InterProcessCommunication.writeMessage(outSocket, description.getTestClass(), Optional.of(description.getMethodName()), Code.SUCCESS);
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            Description description = failure.getDescription();
            InterProcessCommunication.writeMessage(outSocket, description.getTestClass(), Optional.of(description.getMethodName()), Code.FAILURE);
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            Description description = failure.getDescription();
            InterProcessCommunication.writeMessage(outSocket, description.getTestClass(), Optional.of(description.getMethodName()), Code.ASSUMPTION_FAILED);
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            InterProcessCommunication.writeMessage(outSocket, description.getTestClass(), Optional.of(description.getMethodName()), Code.IGNORED);
        }


    }

    public static class InterProcessCommunication {

        public static void sendMessageToNotifier(String message, RunNotifier runNotifier) throws Exception {
            String[] args = message.split(" ");
            int code = Integer.parseInt(args[0]);
            Class<?> clazz = Class.forName(args[1]);
            if (args.length >= 3) {
                String method = args[2];
//                Description classDesc = new JUnit4Builder().safeRunnerForClass(clazz).getDescription();
//                Description testDescription = classDesc.getChildren().stream().filter(d -> d.getMethodName().equals(method)).collect(toList()).get(0);
//                System.out.println(testDescription.toString());
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

        public static void writeMessage(Socket socket, Class<?> testClass, Optional<String> method, int code) {
            try {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                if (method.isPresent()) {
                    out.println(String.format("%d %s %s", code, testClass.getName(), method.get()));
                } else {
                    out.println(String.format("%d %s", code, testClass.getName()));
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
