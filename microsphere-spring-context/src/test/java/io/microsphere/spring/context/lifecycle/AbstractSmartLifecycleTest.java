/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
 * {@link AbstractSmartLifecycle} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractSmartLifecycle
 * @since 1.0.0
 */
class AbstractSmartLifecycleTest {

    private static final Logger logger = getLogger(AbstractSmartLifecycleTest.class);

    private AbstractSmartLifecycle lifecycle;

    @BeforeEach
    void setUp() {
        lifecycle = new AbstractSmartLifecycle() {
            @Override
            protected void doStart() {
                logger.trace("doStart");
            }

            @Override
            protected void doStop() {
                logger.trace("doStop");
            }
        };
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