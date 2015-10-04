package se.plilja.junitparallel;


public class TestUtil {
    public static void sleep2(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static interface Action {
        void exec() throws Exception;
    }
    public static void withTryCatch(Action op) {
        try {
            op.exec();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
