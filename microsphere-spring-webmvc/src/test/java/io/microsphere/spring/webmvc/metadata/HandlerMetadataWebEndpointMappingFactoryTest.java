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

package io.microsphere.spring.webmvc.metadata;


import io.microsphere.spring.web.metadata.HandlerMetadata;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.handler.SimpleUrlHandlerMapping;

import java.util.Optional;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.webmvc;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HandlerMetadataWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMetadataWebEndpointMappingFactory
 * @since 1.0.0
 */
class HandlerMetadataWebEndpointMappingFactoryTest {

    private HandlerMetadataWebEndpointMappingFactory factory;

    private HandlerMetadata<Object, String> handlerMetadata;

    @BeforeEach
    void setUp() {
        HandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        this.factory = new HandlerMetadataWebEndpointMappingFactory(handlerMapping);
        this.handlerMetadata = new HandlerMetadata<>("handler", "/test");
    }

    @Test
    void testGetHandler() {
        assertEquals("handler", this.factory.getHandler(this.handlerMetadata));
    }

    @Test
    void testGetMetadata() {
        assertEquals("/test", this.factory.getMetadata(this.handlerMetadata));
    }

    @Test
    void testSupports() {
        assertTrue(this.factory.supports(this.handlerMetadata));
    }

    @Test
    void testCreate() {
        Optional<WebEndpointMapping<HandlerMetadata<Object, String>>> webEndpointMappingOptional = this.factory.create(this.handlerMetadata);
        assertTrue(webEndpointMappingOptional.isPresent());
        webEndpointMappingOptional.ifPresent(mapping -> {
            WebEndpointMapping webEndpointMapping = webmvc()
                    .endpoint("handler")
                    .patterns("/test")
                    .methods(ALL_HTTP_METHODS)
                    .source(handlerMetadata)
                    .build();
            assertEquals(webEndpointMapping, mapping);
        });
    }

    @Test
    void testGetSourceType() {
        assertEquals(this.handlerMetadata.getClass(), this.factory.getSourceType());
    }

    @Test
    void testGetMethods() {
        assertSame(ALL_HTTP_METHODS, this.factory.getMethods(this.handlerMetadata.getHandler(), this.handlerMetadata.getMetadata()));
    }

    @Test
    void testGetPatterns() {
        assertEquals(ofSet(this.handlerMetadata.getMetadata()), this.factory.getPatterns(this.handlerMetadata.getHandler(), this.handlerMetadata.getMetadata()));
    }
}