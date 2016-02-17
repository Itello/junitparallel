package se.plilja.junitparallel.faketests;

import org.junit.Test;
import se.plilja.junitparallel.Util;

public class TestB {
    @Test
    public void testB3() {
        Util.snooze(400);
    }

    @Test
    public void testB2() {
        Util.snooze(300);
    }

    @Test
    public void testB1() {
        Util.snooze(2000);
    }

    @Test
    public void testB4() {
        Util.snooze(900);
    }

    @Test
    public void testB5() {
        Util.snooze(1000);
    }
}
