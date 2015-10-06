package se.plilja.junitparallel.process;

import org.junit.runner.JUnitCore;

import java.lang.reflect.Constructor;

/**
 * Service that can execute junit tests on demand.
 */
class JunitExecutorService {

    private int port;

    public JunitExecutorService(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(args[0]);
        int forkNumber = Integer.parseInt(args[1]);
        try {
            if (args.length >= 3 && args[2].trim().length() > 0) {
                Class<?> clazz = Class.forName(args[2]);
                Constructor<?> constructor = clazz.getConstructor();
                ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback callback = (ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback) constructor.newInstance();
                callback.execute(forkNumber);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        JunitExecutorService daemon = new JunitExecutorService(port);
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
