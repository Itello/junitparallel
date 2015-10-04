package se.plilja.junitparallel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestModifiesGlobals3 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 4711;
        TestUtil.sleep2(1000);
        assertEquals(4711, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 4712;
        TestUtil.sleep2(1000);
        assertEquals(4712, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 4713;
        TestUtil.sleep2(1000);
        assertEquals(4713, GlobalVariables.globalInt);
    }
}
