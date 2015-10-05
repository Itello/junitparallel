package se.plilja.junitparallel.faketests;

import org.junit.Test;
import se.plilja.junitparallel.util.Util;

import static org.junit.Assert.assertEquals;

public class TestE_failing {
    @Test
    public void testE1() {
        Util.snooze(1000);
    }

    @Test
    public void testE2_shouldFailWithAssertion() {
        Util.snooze(1000);
        assert false;
        throw new RuntimeException("We should not see this error");
    }

    @Test
    public void testE3_shouldFailWithException() {
        throw new RuntimeException();
    }

    @Test
    public void testE4_shouldFailWithAssertion() {
        Util.snooze(1000);
        assertEquals(1, 2);
    }

    @Test
    public void testE5_shouldFailWithAssertion() {
        Util.snooze(1000);
        assertEquals(1, 2);
    }
}
