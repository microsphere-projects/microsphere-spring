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

package io.microsphere.spring.web.metadata;


import org.junit.Before;
import org.junit.Test;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.servlet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link SimpleWebEndpointMappingRegistry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SimpleWebEndpointMappingRegistry
 * @since 1.0.0
 */
public class SimpleWebEndpointMappingRegistryTest {

    private static final WebEndpointMapping mapping = servlet(Object.class).pattern("/*").build();

    private SimpleWebEndpointMappingRegistry registry;

    @Before
    public void setUp() {
        this.registry = new SimpleWebEndpointMappingRegistry();
    }

    @Test
    public void testRegister() {
        assertTrue(registry.register(mapping));
        assertFalse(registry.register(mapping));
    }

    @Test
    public void testGetWebEndpointMappings() {
        registry.register(mapping);
        assertEquals(1, registry.getWebEndpointMappings().size());
        assertTrue(registry.getWebEndpointMappings().contains(mapping));

        registry.register(mapping);
        assertEquals(1, registry.getWebEndpointMappings().size());
        assertTrue(registry.getWebEndpointMappings().contains(mapping));
    }
}