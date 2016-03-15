package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

public class TestC {
    @Test
    public void testC1() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testC2() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testC3() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testC4() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testC5() {
        ParallelSuiteUtil.snooze(1000);
    }
}
