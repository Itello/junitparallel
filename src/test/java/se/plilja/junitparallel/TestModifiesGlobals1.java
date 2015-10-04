package se.plilja.junitparallel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestModifiesGlobals1 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 17;
        TestUtil.sleep2(1000);
        assertEquals(17, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 18;
        TestUtil.sleep2(1000);
        assertEquals(18, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 19;
        TestUtil.sleep2(1000);
        assertEquals(19, GlobalVariables.globalInt);
    }
}
