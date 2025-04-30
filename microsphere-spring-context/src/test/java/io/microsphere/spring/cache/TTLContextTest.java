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
package io.microsphere.spring.cache;


import io.microsphere.logging.Logger;
import org.junit.After;
import org.junit.Test;

import java.time.Duration;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.cache.TTLContext.*;
import static io.microsphere.spring.cache.TTLContext.doWithTTL;
import static io.microsphere.spring.cache.TTLContext.setTTL;
import static java.time.Duration.ofMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * {@link TTLContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TTLContext
 * @since 1.0.0
 */
public class TTLContextTest {

    private static final Logger logger = getLogger(TTLContextTest.class);

    @After
    public void destroy() {
        clearTTL();
    }

    @Test
    public void testDoWithTTL() {
        doWithTTL(d -> {
            logger.trace("doWithTTL(Consumer) : {}", d);
        }, ofMillis(10));
    }

    @Test
    public void testDoWithTTLWithFunction() {
        Duration duration = ofMillis(10);
        assertEquals(duration, doWithTTL(d -> {
            logger.trace("doWithTTL(Function) : {}", d);
            return d;
        }, duration));
    }

    @Test
    public void testSetTTL() {
        Duration duration = ofMillis(100);
        setTTL(duration);

        doWithTTL(d -> {
            assertEquals(d, duration);
        }, ofMillis(10));
    }

    @Test
    public void testGetTTL() {
        Duration duration = ofMillis(100);
        setTTL(duration);
        assertEquals(duration, getTTL());
    }

    @Test
    public void testClearTTL() {
        testGetTTL();
        clearTTL();
        assertNull(getTTL());
    }
}