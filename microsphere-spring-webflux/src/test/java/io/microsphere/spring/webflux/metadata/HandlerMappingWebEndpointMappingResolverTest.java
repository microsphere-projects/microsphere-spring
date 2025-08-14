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


import io.microsphere.spring.test.web.controller.TestRestController;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webflux.AbstractWebFluxTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;

import java.util.Collection;
import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HandlerMappingWebEndpointMappingResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMappingWebEndpointMappingResolver
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        HandlerMappingWebEndpointMappingResolver.class,
        HandlerMappingWebEndpointMappingResolverTest.class,
        HandlerMappingWebEndpointMappingResolverTest.Config.class,
})
@Import(TestRestController.class)
class HandlerMappingWebEndpointMappingResolverTest extends AbstractWebFluxTest {

    @Autowired
    private HandlerMappingWebEndpointMappingResolver resolver;

    @Test
    void testResolve() {
        Collection<WebEndpointMapping> webEndpointMappings = resolver.resolve(context);
        assertEquals(5, webEndpointMappings.size());
    }

    static class Config {

        @Bean
        public SimpleUrlHandlerMapping simpleUrlHandlerMapping() {
            SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
            Map<String, ?> urlMap = ofMap("/test", this, "/test/1", this);
            mapping.setUrlMap(urlMap);
            return mapping;
        }
    }
}