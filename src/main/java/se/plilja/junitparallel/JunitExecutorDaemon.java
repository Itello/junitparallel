package se.plilja.junitparallel;

import org.junit.runner.JUnitCore;

public class JunitExecutorDaemon {

    private int port;

    public JunitExecutorDaemon(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        JunitExecutorDaemon daemon = new JunitExecutorDaemon(port);
        daemon.run();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    public void run() {
        try (InterProcessCommunication ipc = InterProcessCommunication.createServer(port)) {
            while (true) {
                String testClassName = ipc.receiveMessage();
                Class<?> testClass = Class.forName(testClassName.trim());
                JUnitCore jUnitCore = new JUnitCore();
                jUnitCore.addListener(new InterProcessListener(ipc));
                jUnitCore.run(testClass);
                ipc.sendObject(new TestClassDone());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
