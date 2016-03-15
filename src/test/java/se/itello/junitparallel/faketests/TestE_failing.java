package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.ParallelSuiteUtil;

import static org.junit.Assert.assertEquals;

public class TestE_failing {
    @Test
    public void testE1() {
        ParallelSuiteUtil.snooze(1000);
    }

    @Test
    public void testE2_shouldFailWithAssertion() {
        ParallelSuiteUtil.snooze(1000);
        assert false;
        throw new RuntimeException("We should not see this error");
    }

    @Test
    public void testE3_shouldFailWithException() {
        ParallelSuiteUtil.snooze(1000);
        throw new RuntimeException();
    }

    @Test
    public void testE4_shouldFailWithAssertion() {
        ParallelSuiteUtil.snooze(1000);
        assertEquals(1, 2);
    }

    @Test
    public void testE5_shouldFailWithAssertion() {
        ParallelSuiteUtil.snooze(1000);
        assertEquals(1, 2);
    }

    @Test
    public void testE6_throwsUnserializableError() throws Exception {
        assert !ParallelSuiteUtil.isSerializable(new UnserializableError("foo"));
        throw new UnserializableError("This message should show correctly");
    }

    private static class UnserializableError extends RuntimeException {
        @SuppressWarnings("unused")
        UnserializableClass foo = new UnserializableClass();

        UnserializableError(String m) {
            super(m);
        }
    }

    private static class UnserializableClass {

    }
}
