package se.itello.junitparallel;


import java.io.*;
import java.lang.annotation.Annotation;
import java.net.ServerSocket;
import java.util.Optional;

public class ParallelSuiteUtil {
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
    static boolean assertionsAreEnabled() {
        boolean assertionsAreEnabled = false;
        assert assertionsAreEnabled = true; // boolean will be changed if assertions are enabled
        return assertionsAreEnabled;
    }

    static int pickAvailablePort() {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            int result = serverSocket.getLocalPort();
            serverSocket.close();
            return result;
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static boolean isSerializable(Object o) throws IOException {
        if (!(o instanceof Serializable)) {
            return false;
        }
        try {
            // Let's test to make sure, the object may consist of an object that is not serializable
            new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(o);
            return true;
        } catch (NotSerializableException nse) {
            return false;
        }
    }

}
