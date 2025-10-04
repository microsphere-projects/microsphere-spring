package io.microsphere.spring.context.lifecycle;

import io.microsphere.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.context.lifecycle.AbstractSmartLifecycle.DEFAULT_PHASE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link LoggingSmartLifecycle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingSmartLifecycle
 * @since 1.0.0
 */
class LoggingSmartLifecycleTest {

    private static final Logger logger = getLogger(LoggingSmartLifecycleTest.class);

    private LoggingSmartLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new LoggingSmartLifecycle();
    }

    @Test
    void testIsAutoStartup() {
        assertTrue(lifecycle.isAutoStartup());
    }

    @Test
    void testStop() {
        lifecycle.stop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    void testGetPhase() {
        assertEquals(DEFAULT_PHASE, lifecycle.getPhase());
    }

    @Test
    void testStart() {
        assertFalse(lifecycle.isStarted());
        lifecycle.start();
        assertTrue(lifecycle.isStarted());
    }

    @Test
    void testDoStart() {
        assertFalse(lifecycle.isStarted());
        lifecycle.doStart();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    void testStopWithRunnable() {
        assertFalse(lifecycle.isStarted());
        lifecycle.stop(() -> {
            logger.trace("stop running");
        });
        assertFalse(lifecycle.isStarted());
    }

    @Test
    void testDoStop() {
        assertFalse(lifecycle.isStarted());
        lifecycle.doStop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    void testIsRunning() {
        assertFalse(lifecycle.isRunning());
        lifecycle.start();
        assertTrue(lifecycle.isRunning());
    }

    @Test
    void testIsStarted() {
        assertFalse(lifecycle.isStarted());

        lifecycle.start();
        assertTrue(lifecycle.isStarted());

        lifecycle.stop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    void testSetPhase() {
        int phase = 1;
        lifecycle.setPhase(phase);
        assertEquals(phase, lifecycle.getPhase());
    }

    @Test
    void testSetStarted() {
        assertFalse(lifecycle.isStarted());
        lifecycle.setStarted(true);
        assertTrue(lifecycle.isStarted());
    }
}