package se.plilja.junitparallel.faketests;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.plilja.junitparallel.Util;

import java.util.Collection;

import static java.util.Arrays.asList;

@RunWith(Parameterized.class)
public class TestD_parameterized {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return asList(new Object[]{},
                new Object[]{});
    }

    @Test
    public void testD1() {
        Util.snooze(1000);
    }

    @Test
    public void testD2() {
        Util.snooze(1000);
    }
}
