package se.plilja.junitparallel;

import se.plilja.junitparallel.ParallelProcessSuiteConfig;

public class PrintForkNumber implements ParallelProcessSuiteConfig.WhenNewProcessCreated.Callback {
    @Override
    public void execute(int forkNumber) {
        System.out.println(forkNumber);
    }
}
