package se.plilja.junitparallel;

import org.junit.Test;

public class TestB {
    @Test public void testB3() {
        TestUtil.snooze(1000);}
    @Test public void testB2() {
        TestUtil.snooze(1000);}
    @Test public void testB1() {
        TestUtil.snooze(1000);}
    @Test public void testB4() {
        TestUtil.snooze(1000);}
    @Test public void testB5() {
        TestUtil.snooze(1000);}
}
