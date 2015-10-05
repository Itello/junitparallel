package se.plilja.junitparallel;


public class TestUtil {
    public static void snooze(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
