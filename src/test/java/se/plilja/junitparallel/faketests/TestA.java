package se.plilja.junitparallel.faketests;

import org.junit.Test;
import se.plilja.junitparallel.util.Util;

public class TestA {
    @Test public void testA1() {
        Util.snooze(1000);}
    @Test public void testA2() {
        Util.snooze(1000);}
    @Test public void testA3() {
        Util.snooze(1000);}
    @Test public void testA4() {
        Util.snooze(1000);}
    @Test public void testA5() {
        Util.snooze(1000);}
}
