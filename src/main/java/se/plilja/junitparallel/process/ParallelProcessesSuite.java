package se.plilja.junitparallel.process;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;
import se.plilja.junitparallel.util.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.*;

import static java.util.Arrays.asList;
import static se.plilja.junitparallel.util.Util.*;

/**
 * Used when tests need to be run in separate Java processes (typically
 * because tests modify some global static state).
 */
public class ParallelProcessesSuite extends Runner {

    private int nextForkNumber = 0;
    private final Class<?> suiteClass;
    private final Set<Process> workingProcesses = new HashSet<>();
    private final Stack<Process> idleProcesses = new Stack<>();
    private final Map<Process, InterProcessCommunication> processIpc = new HashMap<>();
    private List<StreamGobbler> streamGobblers = new ArrayList<>();


    public ParallelProcessesSuite(Class<?> suiteClass) {
        this.suiteClass = suiteClass;
    }

    @Override
    public Description getDescription() {
        Description suiteDescription = Description.createSuiteDescription(getClass());
        for (Class<?> testClass : getTestClassesInSuite()) {
            suiteDescription.addChild(describeTestClass(testClass));
        }
        return suiteDescription;
    }

    private Description describeTestClass(Class<?> testClass) {
        try {
            if (isParameterized(testClass)) {
                return new Parameterized(testClass).getDescription();
            } else {
                return new BlockJUnit4ClassRunner(testClass).getDescription();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isParameterized(Class<?> testClass) {
        Optional<RunWith> runWith = getAnnotation(testClass.getAnnotations(), RunWith.class);
        return runWith.map(r -> r.value() == Parameterized.class).orElse(false);
    }

    @Override
    public void run(RunNotifier runNotifier) {
        try {
            runInTryCatch(runNotifier);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            cleanUp();
        }
    }

    private void cleanUp() {
        for (StreamGobbler streamGobbler : streamGobblers) {
            streamGobbler.pleaseStop();
        }
        for (Process workingProcess : workingProcesses) {
            workingProcess.destroyForcibly();
        }
        for (Process idleProcess : idleProcesses) {
            idleProcess.destroyForcibly();
        }
        for (InterProcessCommunication ipc : processIpc.values()) {
            try {
                ipc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void runInTryCatch(RunNotifier runNotifier) throws Exception {
        startProcesses();
        for (Class<?> testClass : getTestClassesInSuite()) {
            while (idleProcesses.isEmpty()) {
                snooze(5);
                moveFinishedProcessesToIdle(runNotifier);
            }
            Process idleProcess = idleProcesses.pop();
            sendJobToProcess(testClass, idleProcess);
            workingProcesses.add(idleProcess);

        }
        while (!workingProcesses.isEmpty()) {
            snooze(5);
            moveFinishedProcessesToIdle(runNotifier);
        }
    }

    private void sendJobToProcess(Class<?> testClass, Process idleProcess) throws IOException {
        InterProcessCommunication ipc = processIpc.get(idleProcess);
        ipc.sendMessage(testClass.getName());
    }

    private void moveFinishedProcessesToIdle(RunNotifier runNotifier) throws Exception {
        for (Process workingProcess : workingProcesses) {
            InterProcessCommunication ipc = processIpc.get(workingProcess);
            while (ipc.hasInput()) {
                Object o = ipc.receiveObject();
                if (o instanceof TestProgress) {
                    ((TestProgress) o).passToNotifier(runNotifier);
                } else if (o instanceof TestClassDone) {
                    idleProcesses.add(workingProcess);
                } else {
                    throw new IllegalArgumentException("Uknown object received");
                }
            }
        }
        for (Process idleProcess : idleProcesses) {
            if (workingProcesses.contains(idleProcess)) {
                workingProcesses.remove(idleProcess);
            }
        }
    }

    private void startProcesses() throws Exception {
        for (int i = 0; i < getNumberOfCores(); i++) {
            idleProcesses.add(startJUnitExecutorDaemon());
        }
    }

    private int getNumberOfCores() {
        return getAnnotation(suiteClass.getAnnotations(), ParallelProcessSuiteConfig.NumberOfCores.class)
                .map(a -> a.value())
                .orElse(Runtime.getRuntime().availableProcessors());
    }

    private Process startJUnitExecutorDaemon() throws IOException {
        String separator = System.getProperty("file.separator");
        String classpath = System.getProperty("java.class.path");
        String path = System.getProperty("java.home") + separator + "bin" + separator + "java";

        int port = pickAvailablePort();
        int forkNumber = nextForkNumber++;
        ProcessBuilder processBuilder = new ProcessBuilder(path,
                "-cp", classpath,
                Util.assertionsAreEnabled() ? "-ea" : "",
                JunitExecutorService.class.getName(),
                "" + port,
                "" + forkNumber,
                getNewProcessCreatedCallback().map(Class::getName).orElse("")
        );
        Process process = processBuilder.start();


        InterProcessCommunication client = InterProcessCommunication.createClient(port);
        processIpc.put(process, client);

        startStreamGobbler(process.getErrorStream(), System.err);
        startStreamGobbler(process.getInputStream(), System.out);

        return process;
    }

    private Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> getNewProcessCreatedCallback() {
        return getAnnotation(suiteClass.getAnnotations(), ParallelProcessSuiteConfig.WhenNewProcessCreated.class)
                .map(o -> o.value());
    }

    private void startStreamGobbler(InputStream inputStream, PrintStream out) {
        StreamGobbler streamGobbler = new StreamGobbler(inputStream, out);
        streamGobbler.start();
        streamGobblers.add(streamGobbler);
    }

    private List<Class<?>> getTestClassesInSuite() {
        List<Class<?>> result = getAnnotation(suiteClass.getAnnotations(), Suite.SuiteClasses.class)
                .map(a -> asList(a.value()))
                .orElseGet(Collections::emptyList);
        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

}
