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
package io.microsphere.spring.config.env.event;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;

import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.ADDED;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.REMOVED;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.REPLACED;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.added;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.removed;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.replaced;
import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link PropertySourceChangedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceChangedEvent
 * @since 1.0.0
 */
public class PropertySourceChangedEventTest {

    ConfigurableApplicationContext context;

    private PropertySource newPropertySource;

    private PropertySource oldPropertySource;

    private PropertySourceChangedEvent addedEvent;

    private PropertySourceChangedEvent replacedEvent;

    private PropertySourceChangedEvent removedEvent;

    @Before
    public void setUp() {
        this.context = new GenericApplicationContext();
        this.newPropertySource = new MockPropertySource("new");
        this.oldPropertySource = new MockPropertySource("old");
        this.addedEvent = added(this.context, this.newPropertySource);
        this.replacedEvent = replaced(this.context, this.newPropertySource, this.oldPropertySource);
        this.removedEvent = removed(this.context, this.oldPropertySource);
    }

    @After
    public void tearDown() {
        this.context.close();
    }

    @Test
    public void testGetApplicationContext() {
        assertSame(this.context, this.addedEvent.getApplicationContext());
        assertSame(this.context, this.replacedEvent.getApplicationContext());
        assertSame(this.context, this.removedEvent.getApplicationContext());
    }

    @Test
    public void testGetTimestamp() {
        long timestamp = currentTimeMillis();
        assertTrue(this.addedEvent.getTimestamp() <= timestamp);
        assertTrue(this.replacedEvent.getTimestamp() <= timestamp);
        assertTrue(this.removedEvent.getTimestamp() <= timestamp);
    }

    @Test
    public void testGetSource() {
        assertSame(this.context, this.addedEvent.getSource());
        assertSame(this.context, this.replacedEvent.getSource());
        assertSame(this.context, this.removedEvent.getSource());
    }

    @Test
    public void testTestToString() {
        assertNotNull(this.addedEvent.toString());
        assertNotNull(this.replacedEvent.toString());
        assertNotNull(this.removedEvent.toString());
    }

    @Test
    public void testGetNewPropertySource() {
        assertSame(this.newPropertySource, this.addedEvent.getNewPropertySource());
        assertSame(this.newPropertySource, this.replacedEvent.getNewPropertySource());
        assertNull(this.removedEvent.getNewPropertySource());
    }

    @Test
    public void testGetOldPropertySource() {
        assertNull(this.addedEvent.getOldPropertySource());
        assertSame(this.oldPropertySource, this.replacedEvent.getOldPropertySource());
        assertSame(this.oldPropertySource, this.removedEvent.getOldPropertySource());
    }

    @Test
    public void testGetKind() {
        assertSame(ADDED, this.addedEvent.getKind());
        assertSame(REPLACED, this.replacedEvent.getKind());
        assertSame(REMOVED, this.removedEvent.getKind());
    }

    @Test
    public void testAdded() {
        assertEquals(this.addedEvent, added(this.context, this.newPropertySource));
        assertNotEquals(this.addedEvent, added(this.context, this.oldPropertySource));

        assertNotEquals(this.replacedEvent, added(this.context, this.newPropertySource));
        assertNotEquals(this.replacedEvent, added(this.context, this.oldPropertySource));

        assertNotEquals(this.removedEvent, added(this.context, this.newPropertySource));
        assertNotEquals(this.removedEvent, added(this.context, this.oldPropertySource));
    }

    @Test
    public void testReplaced() {
        assertNotEquals(this.addedEvent, replaced(this.context, this.newPropertySource, this.oldPropertySource));
        assertNotEquals(this.addedEvent, replaced(this.context, this.oldPropertySource, this.newPropertySource));

        assertEquals(this.replacedEvent, replaced(this.context, this.newPropertySource, this.oldPropertySource));
        assertNotEquals(this.replacedEvent, replaced(this.context, this.oldPropertySource, this.newPropertySource));

        assertNotEquals(this.removedEvent, replaced(this.context, this.newPropertySource, this.oldPropertySource));
        assertNotEquals(this.removedEvent, replaced(this.context, this.oldPropertySource, this.newPropertySource));
    }

    @Test
    public void testRemoved() {
        assertNotEquals(addedEvent, removed(this.context, this.oldPropertySource));
        assertNotEquals(addedEvent, removed(this.context, this.newPropertySource));

        assertNotEquals(replacedEvent, removed(this.context, this.oldPropertySource));
        assertNotEquals(replacedEvent, removed(this.context, this.newPropertySource));

        assertEquals(removedEvent, removed(this.context, this.oldPropertySource));
        assertNotEquals(removedEvent, removed(this.context, this.newPropertySource));
    }

    @Test
    public void testEquals() {
        assertFalse(this.addedEvent.equals(null));
        assertFalse(this.replacedEvent.equals(null));
        assertFalse(this.removedEvent.equals(null));
    }

    @Test
    public void testHashCode() {
        assertEquals(this.addedEvent.hashCode(), added(this.context, this.newPropertySource).hashCode());
        assertNotEquals(this.addedEvent.hashCode(), replaced(this.context, this.newPropertySource, this.oldPropertySource).hashCode());
        assertNotEquals(this.addedEvent.hashCode(), removed(this.context, this.oldPropertySource).hashCode());

        assertNotEquals(this.replacedEvent.hashCode(), added(this.context, this.newPropertySource).hashCode());
        assertEquals(this.replacedEvent.hashCode(), replaced(this.context, this.newPropertySource, this.oldPropertySource).hashCode());
        assertNotEquals(this.replacedEvent.hashCode(), removed(this.context, this.oldPropertySource).hashCode());

        assertNotEquals(this.removedEvent.hashCode(), added(this.context, this.oldPropertySource).hashCode());
        assertNotEquals(this.removedEvent.hashCode(), replaced(this.context, this.newPropertySource, this.oldPropertySource).hashCode());
        assertEquals(this.removedEvent.hashCode(), removed(this.context, this.oldPropertySource).hashCode());
    }
}