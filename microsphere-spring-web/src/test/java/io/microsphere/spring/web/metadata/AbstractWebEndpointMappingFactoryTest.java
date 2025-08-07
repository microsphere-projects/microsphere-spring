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

import java.util.Optional;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractWebEndpointMappingFactory
 * @since 1.0.0
 */
public class AbstractWebEndpointMappingFactoryTest {

    private AbstractWebEndpointMappingFactory<String> factory;

    @BeforeEach
    void setUp() throws Exception {
        this.factory = new AbstractWebEndpointMappingFactory<String>() {

            @Override
            protected WebEndpointMapping<?> doCreate(String endpoint) throws Throwable {
                if ("test".equalsIgnoreCase(endpoint)) {
                    return of("/test").build();
                }
                throw new Throwable();
            }
        };
    }

    @Test
    void testSupports() {
        assertTrue(this.factory.supports(null));
    }

    @Test
    void testCreate() {
        Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create("test");
        assertTrue(webEndpointMapping.isPresent());
        webEndpointMapping.ifPresent(mapping -> assertEquals(of("/test").build(), mapping));
    }

    @Test
    void testCreateOnFailed() {
        Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(null);
        assertFalse(webEndpointMapping.isPresent());
    }

    @Test
    void testGetSourceType() {
        assertEquals(String.class, this.factory.getSourceType());
    }

}