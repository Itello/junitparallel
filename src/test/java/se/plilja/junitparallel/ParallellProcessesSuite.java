package se.plilja.junitparallel;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;
import static se.plilja.junitparallel.TestUtil.sleep2;

public class ParallellProcessesSuite extends Runner {

    private AtomicInteger nextPort = new AtomicInteger(53145);
    private final Class<?> suiteClass;
    private final Set<Process> workingProcesses = new HashSet<>();
    private final Stack<Process> idleProcesses = new Stack<>();
    private final Map<Process, Socket> processSocket = new HashMap<>();

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
        }
    }

    private void runInTryCatch(RunNotifier runNotifier) throws Exception {
        startProcesses();
        for (Class<?> testClass : getTestClassesInSuite()) {
            while (idleProcesses.isEmpty()) {
                sleep2(5);
                moveFinishedProcessesToIdle(runNotifier);
            }
            Process idleProcess = idleProcesses.pop();
            sendJobToProcess(testClass, idleProcess);
            workingProcesses.add(idleProcess);

        }
        while (!workingProcesses.isEmpty()) {
            sleep2(5);
            moveFinishedProcessesToIdle(runNotifier);
        }
    }

    private void sendJobToProcess(Class<?> testClass, Process idleProcess) {
        try {
            Socket socket = processSocket.get(idleProcess);
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Sending job : " + testClass.getName());
            writer.println(testClass.getName());
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void moveFinishedProcessesToIdle(RunNotifier runNotifier) throws Exception {
        for (Process workingProcess : workingProcesses) {
            BufferedReader br = new BufferedReader(new InputStreamReader(processSocket.get(workingProcess).getInputStream()));
            while (br.ready()) {
                String line = br.readLine();
                System.out.println(line);
                JunitExecutorDaemon.InterProcessCommunication.sendMessageToNotifier(line, runNotifier);
                if (JunitExecutorDaemon.InterProcessCommunication.isTestFinished(line)) {
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
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int i = 0; i < Runtime.getRuntime().availableProcessors(); i++) {
            Future<Process> process = executorService.submit(() -> startJUnitExecutorDaemon());
            idleProcesses.add(process.get());
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

            Socket socket = null;
            while (socket == null) {
                try {
                    socket = new Socket("localhost", port);
                } catch (ConnectException ce) {
                    sleep2(10); // Expected
                }
            }

            processSocket.put(process, socket);

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
