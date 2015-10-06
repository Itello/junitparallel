package se.plilja.junitparallel.process;

import se.plilja.junitparallel.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static se.plilja.junitparallel.util.Util.pickAvailablePort;

/**
 * Starts and destroys the executor service.
 */
class JunitExecutorTaskManager {

    private int nextForkNumber = 0;
    private final Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> newProcessCreatedCallback;
    private List<Process> createdProcesses = new ArrayList<>();
    private List<StreamGobbler> streamGobblers = new ArrayList<>();
    private List<InterProcessCommunication> openConnections = new ArrayList<>();


    JunitExecutorTaskManager(Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> newProcessCreatedCallback) {
        this.newProcessCreatedCallback = newProcessCreatedCallback;
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

        int port = pickAvailablePort();
        int forkNumber = nextForkNumber++;
        ProcessBuilder processBuilder = new ProcessBuilder(path,
                "-cp", classpath,
                Util.assertionsAreEnabled() ? "-ea" : "",
                JunitExecutor.class.getName(),
                "" + port,
                "" + forkNumber,
                newProcessCreatedCallback.map(Class::getName).orElse("")
        );
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
