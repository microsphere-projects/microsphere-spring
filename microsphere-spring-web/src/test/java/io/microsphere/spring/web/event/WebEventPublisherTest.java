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


import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationListener;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebEventPublisher} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEventPublisher
 * @since 1.0.0
 */
public class WebEventPublisherTest {

    @Test
    void testBeforeExecute() {
        testInSpringContainer(context -> {
            String[] arguments = ofArray("Mercy");
            HandlerMethod handlerMethod = new HandlerMethod(new TestController(), "greeting", String.class);
            NativeWebRequest webRequest = createWebRequest("/greeting/Mercy");
            context.addApplicationListener((ApplicationListener<HandlerMethodArgumentsResolvedEvent>) event -> {
                assertSame(handlerMethod, event.getHandlerMethod());
                assertSame(handlerMethod.getMethod(), event.getMethod());
                assertSame(arguments, event.getArguments());
                assertSame(webRequest, event.getWebRequest());
                assertSame(webRequest, event.getSource());
            });
            WebEventPublisher webEventPublisher = context.getBean(WebEventPublisher.class);
            webEventPublisher.beforeExecute(handlerMethod, arguments, webRequest);
        }, WebEventPublisher.class);
    }

    @Test
    void testDoStart() {
        testInSpringContainer(context -> {
            WebEndpointMappingRegistry webEndpointMappingRegistry = context.getBean(WebEndpointMappingRegistry.class);
            assertTrue(webEndpointMappingRegistry instanceof SimpleWebEndpointMappingRegistry);
        }, WebEventPublisher.class, SimpleWebEndpointMappingRegistry.class, WebEndpointMappingsReadyEventListener.class);
    }

    static class WebEndpointMappingsReadyEventListener implements ApplicationListener<WebEndpointMappingsReadyEvent> {
        @Override
        public void onApplicationEvent(WebEndpointMappingsReadyEvent event) {
            assertTrue(event.getMappings().isEmpty());
        }
    }

    @Test
    void testDoStop() {
        // test doStop in testDoStart()
    }
}