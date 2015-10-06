package se.plilja.junitparallel.util;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.util.Optional;

public class Util {
    public static void snooze(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Annotation> Optional<T> getAnnotation(Annotation[] annotations, Class<T> annotationClazz) {
        for (Annotation a : annotations) {
            if (annotationClazz.isAssignableFrom(a.getClass())) {
                return Optional.of((T) a);
            }
        }
        return Optional.empty();
    }


    @SuppressWarnings("ConstantConditions")
    public static boolean assertionsAreEnabled() {
        boolean assertionsAreEnabled = false;
        assert assertionsAreEnabled = true; // boolean will be changed if assertions are enabled
        return assertionsAreEnabled;
    }

    public static int pickAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int result = serverSocket.getLocalPort();
            serverSocket.close();
            return result;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
