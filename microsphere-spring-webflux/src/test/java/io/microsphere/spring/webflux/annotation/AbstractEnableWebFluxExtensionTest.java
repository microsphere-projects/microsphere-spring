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
package io.microsphere.spring.webflux.annotation;

import io.microsphere.spring.test.web.controller.TestRestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.webflux.test.AbstractWebFluxTest;
import io.microsphere.spring.webflux.metadata.HandlerMappingWebEndpointMappingResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract {@link EnableWebFluxExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@Disabled
@Import(TestRestController.class)
abstract class AbstractEnableWebFluxExtensionTest extends AbstractWebFluxTest implements HandlerMethodArgumentInterceptor {

    protected boolean registerWebEndpointMappings;

    protected boolean interceptHandlerMethods;

    protected boolean publishEvents;

    protected boolean storeRequestBodyArgument;

    protected boolean storeResponseBodyReturnValue;

    @BeforeEach
    void setup() {
        EnableWebFluxExtension enableWebFluxExtension = this.getClass().getAnnotation(EnableWebFluxExtension.class);
        this.registerWebEndpointMappings = enableWebFluxExtension.registerWebEndpointMappings();
        this.interceptHandlerMethods = enableWebFluxExtension.interceptHandlerMethods();
        this.publishEvents = enableWebFluxExtension.publishEvents();
        this.storeRequestBodyArgument = enableWebFluxExtension.storeRequestBodyArgument();
        this.storeResponseBodyReturnValue = enableWebFluxExtension.storeResponseBodyReturnValue();
    }

    @Test
    void testRegisteredBeans() {
        assertTrue(isBeanPresent(this.context, WebFluxExtensionConfiguration.class));
        // From @EnableWebExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, SimpleWebEndpointMappingRegistry.class));
        assertEquals(this.interceptHandlerMethods, this.context.containsBean(DelegatingHandlerMethodAdvice.BEAN_NAME));
        assertEquals(this.publishEvents, isBeanPresent(this.context, WebEventPublisher.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, WebEndpointMappingRegistrar.class));

        // From @EnableWebFluxExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.context, HandlerMappingWebEndpointMappingResolver.class));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.context, DelegatingHandlerMethodAdvice.class));
        // assertEquals(this.interceptHandlerMethods, this.wac.containsBean(InterceptingHandlerMethodProcessor.BEAN_NAME));
        // assertEquals(this.interceptHandlerMethods, isBeanPresent(this.wac, InterceptingHandlerMethodProcessor.class));
        // assertEquals(this.registerHandlerInterceptors, isBeanPresent(this.wac, LazyCompositeHandlerInterceptor.class));
        // assertEquals(this.storeRequestBodyArgument, isBeanPresent(this.wac, StoringRequestBodyArgumentAdvice.class));
        // assertEquals(this.storeResponseBodyReturnValue, isBeanPresent(this.wac, StoringResponseBodyReturnValueAdvice.class));
    }

    @Test
    void test() throws Exception {
        this.webTestClient.get().uri("/test/greeting/hello")
                .exchange()
                .expectBody(String.class).isEqualTo("Greeting : hello");
    }

    /**
     * Test only one mapping : {@link TestRestController#greeting(String)}
     *
     * @param event {@link WebEndpointMappingsReadyEvent}
     */
    @EventListener(WebEndpointMappingsReadyEvent.class)
    void onWebEndpointMappingsReadyEvent(WebEndpointMappingsReadyEvent event) {
        Collection<WebEndpointMapping> mappings = event.getMappings();
        assertEquals(9, mappings.size());
        WebEndpointMapping webEndpointMapping = mappings.iterator().next();
        String[] patterns = webEndpointMapping.getPatterns();
        assertEquals(1, patterns.length);
    }


    /**
     * Test only one method : {@link TestRestController#greeting(String)}
     *
     * @param event {@link HandlerMethodArgumentsResolvedEvent}
     */
    @EventListener(HandlerMethodArgumentsResolvedEvent.class)
    public void onHandlerMethodArgumentsResolvedEvent(HandlerMethodArgumentsResolvedEvent event) {
        Method method = event.getMethod();
        assertMethod(method);

        HandlerMethod handlerMethod = event.getHandlerMethod();
        assertEquals(method, handlerMethod.getMethod());

        assertHandlerMethod(handlerMethod);

        Object[] arguments = event.getArguments();
        assertArguments(arguments);
    }

    /**
     * callback before the {@link MethodParameter} being resolved
     *
     * @param parameter     the method parameter to resolve.
     * @param handlerMethod the method to handle
     * @param webRequest    the current request
     * @throws Exception in case of errors with the preparation of argument values
     */
    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        assertMethodParameter(parameter);
        assertHandlerMethod(handlerMethod);
        assertNativeWebRequest(webRequest);
    }

    /**
     * callback after the {@link MethodParameter} being resolved
     *
     * @param parameter        the method parameter to resolve.
     * @param resolvedArgument the resolved argument
     * @param handlerMethod    the method to handle
     * @param webRequest       the current request
     * @return the resolved argument value, or {@code null} if not resolvable
     * @throws Exception in case of errors with the preparation of argument values
     */
    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        // Reuse
        beforeResolveArgument(parameter, handlerMethod, webRequest);
        assertEquals("hello", resolvedArgument);
    }

    private void assertMethod(Method method) {
        assertEquals("greeting", method.getName());
        assertEquals(String.class, method.getReturnType());

        Class<?>[] parameterTypes = method.getParameterTypes();
        assertEquals(1, parameterTypes.length);
        assertEquals(String.class, parameterTypes[0]);
    }

    private void assertHandlerMethod(HandlerMethod handlerMethod) {
        assertNotNull(handlerMethod);
        Object bean = handlerMethod.getBean();
        assertNotNull(bean);
        assertEquals(TestRestController.class, bean.getClass());
        assertMethod(handlerMethod.getMethod());
    }

    private void assertArguments(Object[] arguments) {
        assertEquals(1, arguments.length);
        assertEquals("hello", arguments[0]);
    }

    private void assertMethodParameter(MethodParameter parameter) {
        assertNotNull(parameter);
        assertEquals(0, parameter.getParameterIndex());
        assertEquals(String.class, parameter.getParameterType());
    }

    private void assertNativeWebRequest(NativeWebRequest webRequest) {
    }


}
