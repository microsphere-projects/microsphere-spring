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

package io.microsphere.spring.web.event;


import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebEndpointMappingsReadyEvent} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingsReadyEvent
 * @since 1.0.0
 */
public class WebEndpointMappingsReadyEventTest {

    private ApplicationContext context;

    private WebEndpointMappingsReadyEvent event;

    @Before
    public void setUp() throws Exception {
        this.context = new GenericApplicationContext();
        this.event = new WebEndpointMappingsReadyEvent(this.context, emptyList());
    }

    @Test
    public void testGetApplicationContext() {
        assertEquals(this.event.getSource(), this.event.getApplicationContext());
    }

    @Test
    public void testGetTimestamp() {
        assertTrue(this.event.getTimestamp() <= currentTimeMillis());
    }

    @Test
    public void testGetSource() {
        assertEquals(this.event.getSource(), this.event.getApplicationContext());
    }

    @Test
    public void testToString() {
        assertNotNull(this.event.toString());
    }

    @Test
    public void testGetMappings() {
        assertTrue(this.event.getMappings().isEmpty());
    }
}