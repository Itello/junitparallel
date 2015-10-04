package se.plilja.junitparallel;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestModifiesGlobals2 {
    @Test
    public void test1() {
        GlobalVariables.globalInt = 7;
        TestUtil.sleep2(1000);
        assertEquals(7, GlobalVariables.globalInt);
    }

    @Test
    public void test2() {
        GlobalVariables.globalInt = 8;
        TestUtil.sleep2(1000);
        assertEquals(8, GlobalVariables.globalInt);
    }

    @Test
    public void test3() {
        GlobalVariables.globalInt = 9;
        TestUtil.sleep2(1000);
        assertEquals(9, GlobalVariables.globalInt);
    }
}
