package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

public class TestA {
    @Test
    public void testA1() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testA2() {
        ParallelSuiteUtil.snooze(800);
    }

    @Test
    public void testA3() {
        ParallelSuiteUtil.snooze(700);
    }

    @Test
    public void testA4() {
        ParallelSuiteUtil.snooze(1100);
    }

    @Test
    public void testA5() {
        ParallelSuiteUtil.snooze(500);
    }
}
