package se.itello.junitparallel;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class ParallelProcessSuiteConfig {
    /**
     * Perform a callback when a new process has been created.
     *
     * Remember that this will be a separate JVM, hence any state
     * in your application will be state in that JVM.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface WhenNewProcessCreated {
        Class<? extends Callback> value();

        interface Callback {
            void execute(int forkNumber);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface NumberOfCores {
        int value();
    }

    /**
     * Possibility to provide custom JVM-parameters to the
     * different forks.
     */
    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface JvmArgs {
        Class<? extends JvmArgsProvider> value();

        interface JvmArgsProvider {
            List<String> execute(int forkNumber);
        }
    }
}
