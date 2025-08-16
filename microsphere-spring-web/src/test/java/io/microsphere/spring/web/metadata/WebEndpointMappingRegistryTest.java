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

import java.util.Collection;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.servlet;
import static java.util.Collections.emptyList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.HttpMethod.GET;

/**
 * {@link WebEndpointMappingRegistry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistry
 * @since 1.0.0
 */
public class WebEndpointMappingRegistryTest {

    private static final WebEndpointMapping mapping = servlet(Object.class).patterns("/*").method(GET).build();

    private static final WebEndpointMapping non_mapping = servlet(Object.class).patterns("!/*").method(GET).build();


    static class WebEndpointMappingRegistryImpl implements WebEndpointMappingRegistry {

        @Override
        public boolean register(WebEndpointMapping webEndpointMapping) {
            return mapping == webEndpointMapping;
        }

        @Override
        public Collection<WebEndpointMapping> getWebEndpointMappings() {
            return emptyList();
        }
    }

    private WebEndpointMappingRegistry registry;

    @Before
    public void setUp() {
        this.registry = new WebEndpointMappingRegistryImpl();
    }

    @Test
    public void testRegister() {
        assertTrue(registry.register(mapping));
        assertFalse(registry.register(non_mapping));
    }

    @Test
    public void testRegisterWithMultipleMappings() {
        assertEquals(3, registry.register(mapping, mapping, mapping));
        assertEquals(1, registry.register(non_mapping, mapping));
    }

    @Test
    public void testRegisterWithMappingsList() {
        assertEquals(3, registry.register(ofList(mapping, mapping, mapping)));
        assertEquals(1, registry.register(ofList(non_mapping, mapping)));
    }

    @Test
    public void testGetWebEndpointMappings() {
        assertEquals(emptyList(), registry.getWebEndpointMappings());
    }

}