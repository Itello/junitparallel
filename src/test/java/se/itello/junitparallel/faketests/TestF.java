package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

public class TestF {
    @Test
    public void testF1() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testF2() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testF3() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testF4() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testF5() {
        ParallelSuiteUtil.snooze(1000);
    }
}
