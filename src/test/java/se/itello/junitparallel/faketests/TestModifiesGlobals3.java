package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

import static org.junit.Assert.assertEquals;

/**
 * Simulates a suboptimal test that must be run in a separate java process as
 * it modifies global state.
 */
public class TestModifiesGlobals3 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 4711;
        ParallelSuiteUtil.snooze(1000);
        assertEquals(4711, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 4712;
        ParallelSuiteUtil.snooze(1000);
        assertEquals(4712, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 4713;
        ParallelSuiteUtil.snooze(1000);
        assertEquals(4713, GlobalVariables.globalInt);
    }
}
