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
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import io.microsphere.spring.webflux.test.RouterFunctionTestConfig;
import io.microsphere.spring.webflux.test.SimpleUrlHandlerMappingTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link HandlerMappingWebEndpointMappingResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMappingWebEndpointMappingResolver
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        RouterFunctionTestConfig.class,
        SimpleUrlHandlerMappingTestConfig.class,
        HandlerMappingWebEndpointMappingResolver.class,
        HandlerMappingWebEndpointMappingResolverTest.class,

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

}