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


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.mock.env.MockPropertySource;

import java.util.Map;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.added;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.removed;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.replaced;
import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link PropertySourcesChangedEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourcesChangedEvent
 * @since 1.0.0
 */
public class PropertySourcesChangedEventTest {

    private ConfigurableApplicationContext context;

    private MutablePropertySources propertySources;

    private MockPropertySource newPropertySource;

    private MockPropertySource oldPropertySource;

    private PropertySourceChangedEvent addedEvent;

    private PropertySourceChangedEvent replacedEvent;

    private PropertySourceChangedEvent removedEvent;

    private PropertySourcesChangedEvent event;

    @BeforeEach
    public void before() {
        this.context = new GenericApplicationContext();
        this.propertySources = context.getEnvironment().getPropertySources();
        this.newPropertySource = new MockPropertySource("new");
        this.oldPropertySource = new MockPropertySource("old");
        this.addedEvent = added(this.context, this.newPropertySource);
        this.replacedEvent = replaced(this.context, this.newPropertySource, this.oldPropertySource);
        this.removedEvent = removed(this.context, this.oldPropertySource);
        this.event = new PropertySourcesChangedEvent(this.context, this.addedEvent, this.replacedEvent, this.removedEvent);

        this.newPropertySource.setProperty("test-key", "test-value");
        this.oldPropertySource.setProperty("test-key-2", "test-value-2");
    }

    @AfterEach
    public void after() {
        this.context.close();
    }

    @Test
    public void testGetApplicationContext() {
        assertSame(this.context, this.event.getApplicationContext());
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(this.event.getTimestamp() <= currentTimeMillis());
    }

    @Test
    public void testGetSource() {
        assertSame(this.context, this.event.getSource());
    }

    @Test
    public void testTestToString() {
        assertNotNull(this.event.toString());
    }

    @Test
    public void testGetSubEvents() {
        assertEquals(this.event.getSubEvents(), ofList(this.addedEvent, this.replacedEvent, this.removedEvent));
    }

    @Test
    public void testGetChangedProperties() {
        Map<String, Object> properties = this.event.getChangedProperties();
        assertEquals(properties.size(), 1);
        assertEquals("test-value", properties.get("test-key"));
    }

    @Test
    public void testGetAddedProperties() {
        Map<String, Object> properties = this.event.getAddedProperties();
        assertEquals(properties.size(), 1);
        assertEquals("test-value", properties.get("test-key"));
    }

    @Test
    public void testGetRemovedProperties() {
        Map<String, Object> properties = this.event.getRemovedProperties();
        assertEquals(properties.size(), 1);
        assertEquals("test-value-2", properties.get("test-key-2"));
    }
}