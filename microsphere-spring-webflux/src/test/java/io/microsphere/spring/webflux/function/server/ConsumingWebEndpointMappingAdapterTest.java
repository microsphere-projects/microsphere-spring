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

package io.microsphere.spring.webflux.function.server;

import io.microsphere.spring.test.web.controller.TestRestController;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import io.microsphere.spring.webflux.test.RouterFunctionTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.support.RouterFunctionMapping;

import java.util.Collection;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ConsumingWebEndpointMappingAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConsumingWebEndpointMappingAdapter
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        ConsumingWebEndpointMappingAdapterTest.class,
        RouterFunctionTestConfig.class
})
@Import(TestRestController.class)
public class ConsumingWebEndpointMappingAdapterTest extends AbstractWebFluxTest {

    @Test
    void test() {
        RouterFunctionMapping routerFunctionMapping = context.getBean(RouterFunctionMapping.class);
        RouterFunction<?> routerFunction = routerFunctionMapping.getRouterFunction();
        Collection<WebEndpointMapping> webEndpointMappings = newLinkedList();
        routerFunction.accept(new ConsumingWebEndpointMappingAdapter(webEndpointMappings::add));
        assertEquals(5, webEndpointMappings.size());
    }

}

