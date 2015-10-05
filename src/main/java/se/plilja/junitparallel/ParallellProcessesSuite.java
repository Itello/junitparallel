package se.plilja.junitparallel;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static se.plilja.junitparallel.TestUtil.snooze;

public class ParallellProcessesSuite extends Runner {

    private AtomicInteger nextPort = new AtomicInteger(53297);
    private final Class<?> suiteClass;
    private final Set<Process> workingProcesses = new HashSet<>();
    private final Stack<Process> idleProcesses = new Stack<>();
    private final Map<Process, InterProcessCommunication> processIpc = new HashMap<>();

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
        System.out.println("SENDING JOB " + testClass.getName());
        ipc.sendMessage(testClass.getName());
    }

    private void moveFinishedProcessesToIdle(RunNotifier runNotifier) throws Exception {
        for (Process workingProcess : workingProcesses) {
            InterProcessCommunication ipc = processIpc.get(workingProcess);
            while (ipc.hasInput()) {
                String line = ipc.receiveMessage();
                System.out.println(line);
                JunitExecutorDaemon.InterProcessCommunication2.sendMessageToNotifier(line, runNotifier);
                if (JunitExecutorDaemon.InterProcessCommunication2.isTestFinished(line)) {
                    idleProcesses.add(workingProcess);
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

            new StreamGobbler(process.getErrorStream(), System.err).start();
            new StreamGobbler(process.getInputStream(), System.out).start();

            return process;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Class<?>> getTestClassesInSuite() {
        return getAnnotation(suiteClass.getAnnotations(), Suite.SuiteClasses.class)
                .map(a -> asList(a.value()))
                .orElseGet(Collections::emptyList);
    }

    @SuppressWarnings("unchecked")
    private <T extends Annotation> Optional<T> getAnnotation(Annotation[] annotations, Class<T> annotationClazz) {
        for (Annotation a : annotations) {
            if (annotationClazz.isAssignableFrom(a.getClass())) {
                return Optional.of((T) a);
            }
        }
        return Optional.empty();
    }
}
