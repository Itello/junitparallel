package se.itello.junitparallel;

import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

import java.io.IOException;
import java.util.*;

import static java.util.Arrays.asList;
import static se.itello.junitparallel.Util.getAnnotation;
import static se.itello.junitparallel.Util.snooze;

/**
 * Used when tests need to be run in separate Java processes (typically
 * because tests modify some global static state).
 */
public class ParallelProcessSuite extends Runner {

    private final Class<?> suiteClass;
    private final Set<InterProcessCommunication> workingProcesses = new HashSet<>();
    private final Stack<InterProcessCommunication> idleProcesses = new Stack<>();
    private final JunitExecutorTaskManager taskManager;

    public ParallelProcessSuite(Class<?> suiteClass) {
        this.suiteClass = suiteClass;
        taskManager = new JunitExecutorTaskManager(getNewProcessCreatedCallback());
    }

    private Optional<Class<? extends ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback>> getNewProcessCreatedCallback() {
        return getAnnotation(suiteClass.getAnnotations(), ParallelProcessSuiteConfig.WhenNewProcessCreated.class)
                .map(o -> o.value());
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
            taskManager.cleanUp();
        }
    }

    private void runInTryCatch(RunNotifier runNotifier) throws Exception {
        startProcesses();
        for (Class<?> testClass : getTestClassesInSuite()) {
            while (idleProcesses.isEmpty()) {
                snooze(5);
                moveFinishedProcessesToIdle(runNotifier);
            }
            InterProcessCommunication idleProcess = idleProcesses.pop();
            sendJobToProcess(testClass, idleProcess);
            workingProcesses.add(idleProcess);

        }
        while (!workingProcesses.isEmpty()) {
            snooze(5);
            moveFinishedProcessesToIdle(runNotifier);
        }
    }

    private void sendJobToProcess(Class<?> testClass, InterProcessCommunication idleProcess) throws IOException {
        idleProcess.sendMessage(testClass.getName());
    }

    private void moveFinishedProcessesToIdle(RunNotifier runNotifier) throws Exception {
        for (InterProcessCommunication workingProcess : workingProcesses) {
            while (workingProcess.hasInput()) {
                Object o = workingProcess.receiveObject();
                if (o instanceof TestProgress) {
                    ((TestProgress) o).passToNotifier(runNotifier);
                } else if (o instanceof TestClassDone) {
                    idleProcesses.add(workingProcess);
                } else {
                    throw new IllegalArgumentException("Uknown object received");
                }
            }
        }
        for (InterProcessCommunication idleProcess : idleProcesses) {
            if (workingProcesses.contains(idleProcess)) {
                workingProcesses.remove(idleProcess);
            }
        }
    }

    private void startProcesses() throws Exception {
        for (int i = 0; i < getNumberOfCores(); i++) {
            idleProcesses.add(taskManager.startJUnitExecutorDaemon());
        }
    }

    private int getNumberOfCores() {
        return getAnnotation(suiteClass.getAnnotations(), ParallelProcessSuiteConfig.NumberOfCores.class)
                .map(a -> a.value())
                .orElse(Runtime.getRuntime().availableProcessors());
    }

    private List<Class<?>> getTestClassesInSuite() {
        List<Class<?>> result = getAnnotation(suiteClass.getAnnotations(), Suite.SuiteClasses.class)
                .map(a -> asList(a.value()))
                .orElseGet(Collections::emptyList);
        result.sort((a, b) -> a.getName().compareTo(b.getName()));
        return result;
    }

}
