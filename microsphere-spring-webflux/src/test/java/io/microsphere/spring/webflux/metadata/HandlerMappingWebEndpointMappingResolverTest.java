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

package io.microsphere.spring.webflux.metadata;


import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webflux.annotation.EnableWebFluxExtension;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import io.microsphere.spring.webflux.test.SimpleUrlHandlerMappingTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link HandlerMappingWebEndpointMappingResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMappingWebEndpointMappingResolver
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        SimpleUrlHandlerMappingTestConfig.class,
        HandlerMappingWebEndpointMappingResolverTest.class,
})
@EnableWebFluxExtension
class HandlerMappingWebEndpointMappingResolverTest extends AbstractWebFluxTest {

    @Autowired
    private HandlerMappingWebEndpointMappingResolver resolver;

    @Test
    void testResolve() {
        Collection<WebEndpointMapping> webEndpointMappings = resolver.resolve(context);
        assertFalse(webEndpointMappings.isEmpty());
    }

    @Test
    void testResolveWithoutHandlerMappings() {
        testInSpringContainer(context -> {
            Collection<WebEndpointMapping> webEndpointMappings = resolver.resolve(context);
            assertTrue(webEndpointMappings.isEmpty());
        });
    }

    @Test
    void testResolveWithEmptyHandlerMappings() {
        testInSpringContainer(context -> {
            Collection<WebEndpointMapping> webEndpointMappings = resolver.resolve(context);
            assertTrue(webEndpointMappings.isEmpty());
        }, SimpleUrlHandlerMapping.class, RequestMappingHandlerMapping.class, RouterFunctionMapping.class);
    }

}