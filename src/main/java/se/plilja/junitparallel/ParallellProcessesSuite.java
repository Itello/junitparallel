package se.plilja.junitparallel;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static se.plilja.junitparallel.Util.getAnnotation;
import static se.plilja.junitparallel.Util.snooze;

public class ParallellProcessesSuite extends Runner {

    private AtomicInteger nextPort = new AtomicInteger(53297);
    private final Class<?> suiteClass;
    private final Set<Process> workingProcesses = new HashSet<>();
    private final Stack<Process> idleProcesses = new Stack<>();
    private final Map<Process, InterProcessCommunication> processIpc = new HashMap<>();
    private List<StreamGobbler> streamGobblers = new ArrayList<>();


    public ParallellProcessesSuite(Class<?> suiteClass) {
        this.suiteClass = suiteClass;
    }

    @Override
    public Description getDescription() {
        Description suiteDescription = Description.createSuiteDescription(getClass());
        for (Class<?> testClass : getTestClassesInSuite()) {
            Description testClassDescription = Description.createTestDescription(testClass, testClass.getName());
            if (!isParameterized(testClass)) {
                for (Method method : sortedByName(testClass.getMethods())) {
                    getAnnotation(method.getAnnotations(), Test.class)
                            .ifPresent(a -> testClassDescription.addChild(Description.createTestDescription(testClass, method.getName())));
                }
                suiteDescription.addChild(testClassDescription);
            }
        }
        return suiteDescription;
    }

    /**
     * Sort to make IntelliJ happy.
     */
    private List<Method> sortedByName(Method[] methods) {
        List<Method> res = asList(methods);
        Collections.sort(res, (a, b) -> a.getName().compareTo(b.getName()));
        return res;
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
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            idleProcesses.add(startJUnitExecutorDaemon());
        }
    }

    private Process startJUnitExecutorDaemon() {
        try {
            String separator = System.getProperty("file.separator");
            String classpath = System.getProperty("java.class.path");
            String path = System.getProperty("java.home")
                    + separator + "bin" + separator + "java";
            int port = nextPort.incrementAndGet();
            Process process = new ProcessBuilder(path, "-cp",
                    classpath,
                    JunitExecutorDaemon.class.getName(), "" + port)
                    .start();


            InterProcessCommunication client = InterProcessCommunication.createClient(port);
            processIpc.put(process, client);

            startStreamGobbler(process.getErrorStream(), System.err);
            startStreamGobbler(process.getInputStream(), System.out);

            return process;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void startStreamGobbler(InputStream inputStream, PrintStream out) {
        StreamGobbler streamGobbler = new StreamGobbler(inputStream, out);
        streamGobbler.start();
        streamGobblers.add(streamGobbler);
    }

    private List<Class<?>> getTestClassesInSuite() {
        return getAnnotation(suiteClass.getAnnotations(), Suite.SuiteClasses.class)
                .map(a -> asList(a.value()))
                .orElseGet(Collections::emptyList);
    }

}
