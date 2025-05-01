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
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.mock.env.MockPropertySource;

import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.added;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.removed;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.replaced;

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

    private PropertySource newPropertySource;

    private PropertySource oldPropertySource;

    private PropertySourceChangedEvent addedEvent;

    private PropertySourceChangedEvent replacedEvent;

    private PropertySourceChangedEvent removedEvent;

    @Before
    public void before() {
        this.context = new GenericApplicationContext();
        this.propertySources = context.getEnvironment().getPropertySources();
        this.newPropertySource = new MockPropertySource("new");
        this.oldPropertySource = new MockPropertySource("old");
        this.addedEvent = added(this.context, this.newPropertySource);
        this.replacedEvent = replaced(this.context, this.newPropertySource, this.oldPropertySource);
        this.removedEvent = removed(this.context, this.oldPropertySource);
    }

    @After
    public void after() {
        this.context.close();
    }

    @Test
    public void testGetApplicationContext() {
    }

    @Test
    public void testGetTimestamp() {
    }

    @Test
    public void testGetSource() {
    }

    @Test
    public void testTestToString() {
    }

    @Test
    public void testGetSubEvents() {
    }

    @Test
    public void testGetChangedProperties() {
    }

    @Test
    public void testGetAddedProperties() {
    }

    @Test
    public void testGetRemovedProperties() {
    }

    @Test
    public void testGetProperties() {
    }
}