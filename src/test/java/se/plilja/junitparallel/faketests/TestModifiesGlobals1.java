package se.plilja.junitparallel.faketests;

import org.junit.Test;
import se.plilja.junitparallel.util.Util;

import static org.junit.Assert.assertEquals;

/**
 * Simulates a suboptimal test that must be run in a separate java process as
 * it modifies global state.
 */
public class TestModifiesGlobals1 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 17;
        Util.snooze(1000);
        assertEquals(17, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 18;
        Util.snooze(1000);
        assertEquals(18, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 19;
        Util.snooze(1000);
        assertEquals(19, GlobalVariables.globalInt);
    }
}
