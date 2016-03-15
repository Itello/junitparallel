package se.itello.junitparallel;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

/**
 * Starts and destroys the executor service.
 */
class JunitExecutorTaskManager {

    private int nextForkNumber = 0;
    private final Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> newProcessCreatedCallback;
    private final ParallelProcessSuiteConfig.JvmArgs.JvmArgsProvider jvmArgsProvider;
    private final List<Process> createdProcesses = new ArrayList<>();
    private final List<StreamGobbler> streamGobblers = new ArrayList<>();
    private final List<InterProcessCommunication> openConnections = new ArrayList<>();

    JunitExecutorTaskManager(Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> newProcessCreatedCallback, ParallelProcessSuiteConfig.JvmArgs.JvmArgsProvider jvmArgsProvider) {
        this.newProcessCreatedCallback = newProcessCreatedCallback;
        this.jvmArgsProvider = jvmArgsProvider;
    }

    void cleanUp() {
        for (StreamGobbler streamGobbler : streamGobblers) {
            streamGobbler.pleaseStop();
        }
        for (Process workingProcess : createdProcesses) {
            workingProcess.destroyForcibly();
        }
        for (InterProcessCommunication ipc : openConnections) {
            try {
                ipc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    InterProcessCommunication startJUnitExecutorDaemon() throws IOException {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
        int port = ParallelSuiteUtil.pickAvailablePort();
        int forkNumber = nextForkNumber++;

        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add(path);
        jvmArgs.addAll(jvmArgsProvider.execute(forkNumber));
        jvmArgs.add("-Xmx1024m");
        jvmArgs.addAll(asList("-cp", classpath));
        if (ParallelSuiteUtil.assertionsAreEnabled()) {
            jvmArgs.add("-ea");
        }
        jvmArgs.add(JunitExecutor.class.getName());
        jvmArgs.add("" + port);
        jvmArgs.add("" + forkNumber);
        jvmArgs.add(newProcessCreatedCallback.map(Class::getName).orElse(""));
        ProcessBuilder processBuilder = new ProcessBuilder(jvmArgs);
        Process process = processBuilder.start();

        createdProcesses.add(process);

        InterProcessCommunication client = InterProcessCommunication.createClient(port);

        openConnections.add(client);

        startStreamGobbler(process.getErrorStream(), System.err);
        startStreamGobbler(process.getInputStream(), System.out);

        return client;
    }

    private void startStreamGobbler(InputStream inputStream, PrintStream out) {
        StreamGobbler streamGobbler = new StreamGobbler(inputStream, out);
        streamGobbler.start();
        streamGobblers.add(streamGobbler);
    }
}
