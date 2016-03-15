package se.itello.junitparallel.faketests;

import org.junit.Test;
import se.itello.junitparallel.Util;

public class TestA {
    @Test
    public void testA1() {
        Util.snooze(1000);
    }

    @Test
    public void testA2() {
        Util.snooze(800);
    }

    @Test
    public void testA3() {
        Util.snooze(700);
    }

    @Test
    public void testA4() {
        Util.snooze(1100);
    }

    @Test
    public void testA5() {
        Util.snooze(500);
    }
}
