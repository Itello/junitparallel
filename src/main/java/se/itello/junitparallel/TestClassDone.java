package se.itello.junitparallel;

import java.io.Serializable;

/**
 * Marker interface to communicate between processes that
 * the executor service has finished a test class and is ready to
 * receive more work.
 */
class TestClassDone implements Serializable {
}
