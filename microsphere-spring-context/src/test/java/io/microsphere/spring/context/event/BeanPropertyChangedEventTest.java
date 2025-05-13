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
package io.microsphere.spring.context.event;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link BeanPropertyChangedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanPropertyChangedEvent
 * @since 1.0.0
 */
public class BeanPropertyChangedEventTest {

    private BeanPropertyChangedEvent event;

    @BeforeEach
    public void before() {
        this.event = new BeanPropertyChangedEvent(this, "event", null, this);
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(this.event.getTimestamp() > 0);
    }

    @Test
    public void testGetSource() {
        assertSame(this, this.event.getSource());
    }

    @Test
    public void testToString() {
        assertNotNull(this.event.toString());
    }

    @Test
    public void testGetBean() {
        assertSame(this, this.event.getBean());
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("event", this.event.getPropertyName());
    }

    @Test
    public void testGetOldValue() {
        assertNull(this.event.getOldValue());
    }

    @Test
    public void testGetNewValue() {
        assertSame(this, this.event.getNewValue());
    }
}