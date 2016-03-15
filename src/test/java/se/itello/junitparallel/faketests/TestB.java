package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

public class TestB {
    @Test
    public void testB3() {
        ParallelSuiteUtil.snooze(400);
    }

    @Test
    public void testB2() {
        ParallelSuiteUtil.snooze(300);
    }

    @Test
    public void testB1() {
        ParallelSuiteUtil.snooze(2000);
    }

    @Test
    public void testB4() {
        ParallelSuiteUtil.snooze(900);
    }

    @Test
    public void testB5() {
        ParallelSuiteUtil.snooze(1000);
    }
}
