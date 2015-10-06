package se.plilja.junitparallel.process;

public class PrintForkNumber implements ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback {
    @Override
    public void execute(int forkNumber) {
        System.out.println(forkNumber);
    }
}
