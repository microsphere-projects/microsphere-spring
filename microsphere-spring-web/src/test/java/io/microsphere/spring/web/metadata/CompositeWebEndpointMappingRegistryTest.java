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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.servlet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link CompositeWebEndpointMappingRegistry} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CompositeWebEndpointMappingRegistry
 * @since 1.0.0
 */
class CompositeWebEndpointMappingRegistryTest {


    private WebEndpointMapping webEndpointMapping;

    @BeforeEach
    void setUp() {
        this.webEndpointMapping = servlet()
                .patterns("/users/**")
                .methods("GET")
                .endpoint(this)
                .build();
    }

    @Test
    void test() {
        testInSpringContainer(context -> {
            CompositeWebEndpointMappingRegistry registry = context.getBean(CompositeWebEndpointMappingRegistry.class);
            assertTrue(registry.register(webEndpointMapping));
            Collection<WebEndpointMapping> webEndpointMappings = registry.getWebEndpointMappings();
            assertEquals(1, webEndpointMappings.size());
            assertTrue(webEndpointMappings.contains(webEndpointMapping));
        }, CompositeWebEndpointMappingRegistry.class, SimpleWebEndpointMappingRegistry.class);
    }

    @Test
    void testOnNoDelegatingWebEndpointMappingRegistry() {
        testInSpringContainer(context -> {
            CompositeWebEndpointMappingRegistry registry = context.getBean(CompositeWebEndpointMappingRegistry.class);
            assertFalse(registry.register(webEndpointMapping));
            assertTrue(registry.getWebEndpointMappings().isEmpty());
        }, CompositeWebEndpointMappingRegistry.class);
    }
}