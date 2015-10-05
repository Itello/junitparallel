package se.plilja.junitparallel;


import java.lang.annotation.Annotation;
import java.util.Optional;

class Util {
    static void snooze(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    static <T extends Annotation> Optional<T> getAnnotation(Annotation[] annotations, Class<T> annotationClazz) {
        for (Annotation a : annotations) {
            if (annotationClazz.isAssignableFrom(a.getClass())) {
                return Optional.of((T) a);
            }
        }
        return Optional.empty();
    }


}
