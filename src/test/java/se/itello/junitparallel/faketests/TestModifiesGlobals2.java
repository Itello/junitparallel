package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.Util;

import static org.junit.Assert.assertEquals;

/**
 * Simulates a suboptimal test that must be run in a separate java process as
 * it modifies global state.
 */
public class TestModifiesGlobals2 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 7;
        Util.snooze(1000);
        assertEquals(7, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 8;
        Util.snooze(1000);
        assertEquals(8, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 9;
        Util.snooze(1000);
        assertEquals(9, GlobalVariables.globalInt);
    }
}
