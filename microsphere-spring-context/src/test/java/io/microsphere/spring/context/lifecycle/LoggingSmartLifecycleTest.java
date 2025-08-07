package io.microsphere.spring.context.lifecycle;

import io.microsphere.logging.Logger;
import org.junit.Before;
import org.junit.Test;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.context.lifecycle.AbstractSmartLifecycle.DEFAULT_PHASE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link LoggingSmartLifecycle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see LoggingSmartLifecycle
 * @since 1.0.0
 */
public class LoggingSmartLifecycleTest {

    private static final Logger logger = getLogger(LoggingSmartLifecycleTest.class);

    private LoggingSmartLifecycle lifecycle;

    @Before
    public void setUp() {
        lifecycle = new LoggingSmartLifecycle();
    }

    @Test
    public void testIsAutoStartup() {
        assertTrue(lifecycle.isAutoStartup());
    }

    @Test
    public void testStop() {
        lifecycle.stop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    public void testGetPhase() {
        assertEquals(DEFAULT_PHASE, lifecycle.getPhase());
    }

    @Test
    public void testStart() {
        assertFalse(lifecycle.isStarted());
        lifecycle.start();
        assertTrue(lifecycle.isStarted());
    }

    @Test
    public void testDoStart() {
        assertFalse(lifecycle.isStarted());
        lifecycle.doStart();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    public void testStopWithRunnable() {
        assertFalse(lifecycle.isStarted());
        lifecycle.stop(() -> {
            logger.trace("stop running");
        });
        assertFalse(lifecycle.isStarted());
    }

    @Test
    public void testDoStop() {
        assertFalse(lifecycle.isStarted());
        lifecycle.doStop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    public void testIsRunning() {
        assertFalse(lifecycle.isRunning());
        lifecycle.start();
        assertTrue(lifecycle.isRunning());
    }

    @Test
    public void testIsStarted() {
        assertFalse(lifecycle.isStarted());

        lifecycle.start();
        assertTrue(lifecycle.isStarted());

        lifecycle.stop();
        assertFalse(lifecycle.isStarted());
    }

    @Test
    public void testSetPhase() {
        int phase = 1;
        lifecycle.setPhase(phase);
        assertEquals(phase, lifecycle.getPhase());
    }

    @Test
    public void testSetStarted() {
        assertFalse(lifecycle.isStarted());
        lifecycle.setStarted(true);
        assertTrue(lifecycle.isStarted());
    }
}