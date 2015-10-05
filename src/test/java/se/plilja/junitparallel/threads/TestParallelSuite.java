package se.plilja.junitparallel.threads;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import se.plilja.junitparallel.faketests.*;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
        TestA.class,
        TestB.class,
        TestC.class,
        TestD_parameterized.class,
        TestE_failing.class,
        TestF.class,
        TestG.class
})
public class TestParallelSuite {

}
