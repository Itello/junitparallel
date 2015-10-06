package se.plilja.junitparallel.process;

public class PrintForkNumber implements WhenNewProcessCreated.Callback {
    @Override
    public void execute(int forkNumber) {
        System.out.println(forkNumber);
    }
}
