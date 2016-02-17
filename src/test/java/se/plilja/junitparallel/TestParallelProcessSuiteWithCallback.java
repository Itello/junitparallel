package se.plilja.junitparallel;


import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import se.plilja.junitparallel.faketests.*;

@RunWith(ParallelProcessSuite.class)
@ParallelProcessSuiteConfig.NumberOfCores(4)
@ParallelProcessSuiteConfig.WhenNewProcessCreated(PrintForkNumber.class)
@Suite.SuiteClasses({
        TestA.class,
        TestB.class,
        TestC.class,
        TestD_parameterized.class,
        TestE_failing.class,
        TestF.class,
        TestG.class,
        TestModifiesGlobals1.class,
        TestModifiesGlobals2.class,
        TestModifiesGlobals3.class
})
public class TestParallelProcessSuiteWithCallback {

}
