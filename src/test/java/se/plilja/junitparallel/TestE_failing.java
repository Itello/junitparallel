package se.plilja.junitparallel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestE_failing {
    @Test public void testE1() {
        Util.snooze(1000);}
    @Test public void testE2() {
        Util.snooze(1000);}
    @Test public void testE3() {
        throw new RuntimeException();
    }
    @Test public void testE4_shouldFail() {
        Util.snooze(1000);
        assertEquals(1, 2);
    }
    @Test public void testE5_shouldFail() {
        Util.snooze(1000);
        assertEquals(1, 2);
    }
}
