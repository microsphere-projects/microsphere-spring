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
package io.microsphere.spring.webmvc.annotation;

import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.ServletWebEndpointMappingResolver;
import io.microsphere.spring.web.metadata.SimpleWebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistrar;
import io.microsphere.spring.web.method.support.DelegatingHandlerMethodAdvice;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.advice.StoringResponseBodyReturnValueAdvice;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import io.microsphere.spring.webmvc.metadata.HandlerMappingWebEndpointMappingResolver;
import io.microsphere.spring.webmvc.method.support.InterceptingHandlerMethodProcessor;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.core.MethodParameter;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.lang.reflect.Method;
import java.util.Collection;

import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.util.ArrayUtils.isNotEmpty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

/**
 * Abstract {@link EnableWebMvcExtension} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@WebAppConfiguration
@EnableWebMvc
@Ignore
@Import(TestController.class)
abstract class AbstractEnableWebMvcExtensionTest implements HandlerMethodArgumentInterceptor {

    @Autowired
    protected ConfigurableWebApplicationContext wac;

    protected MockMvc mockMvc;

    protected boolean registerWebEndpointMappings;

    protected boolean interceptHandlerMethods;

    protected boolean publishEvents;

    protected boolean registerHandlerInterceptors;

    protected boolean storeRequestBodyArgument;

    protected boolean storeResponseBodyReturnValue;

    @Before
    public void setup() {
        this.mockMvc = webAppContextSetup(this.wac).build();
        EnableWebMvcExtension enableWebMvcExtension = this.getClass().getAnnotation(EnableWebMvcExtension.class);
        this.registerWebEndpointMappings = enableWebMvcExtension.registerWebEndpointMappings();
        this.interceptHandlerMethods = enableWebMvcExtension.interceptHandlerMethods();
        this.publishEvents = enableWebMvcExtension.publishEvents();
        this.registerHandlerInterceptors = enableWebMvcExtension.registerHandlerInterceptors() ? true :
                isNotEmpty(enableWebMvcExtension.handlerInterceptors());
        this.storeRequestBodyArgument = enableWebMvcExtension.storeRequestBodyArgument();
        this.storeResponseBodyReturnValue = enableWebMvcExtension.storeResponseBodyReturnValue();
    }

    @Test
    public void testRegisteredBeans() {
        assertTrue(isBeanPresent(this.wac, WebMvcExtensionConfiguration.class));
        // From @EnableWebExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.wac, SimpleWebEndpointMappingRegistry.class));
        assertEquals(this.interceptHandlerMethods, this.wac.containsBean(DelegatingHandlerMethodAdvice.BEAN_NAME));
        assertEquals(this.publishEvents, isBeanPresent(this.wac, WebEventPublisher.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.wac, WebEndpointMappingRegistrar.class));

        // From @EnableWebMvcExtension
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.wac, ServletWebEndpointMappingResolver.class));
        assertEquals(this.registerWebEndpointMappings, isBeanPresent(this.wac, HandlerMappingWebEndpointMappingResolver.class));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.wac, DelegatingHandlerMethodAdvice.class));
        assertEquals(this.interceptHandlerMethods, this.wac.containsBean(InterceptingHandlerMethodProcessor.BEAN_NAME));
        assertEquals(this.interceptHandlerMethods, isBeanPresent(this.wac, InterceptingHandlerMethodProcessor.class));
        assertEquals(this.registerHandlerInterceptors, isBeanPresent(this.wac, LazyCompositeHandlerInterceptor.class));
        assertEquals(this.storeRequestBodyArgument, isBeanPresent(this.wac, StoringRequestBodyArgumentAdvice.class));
        assertEquals(this.storeResponseBodyReturnValue, isBeanPresent(this.wac, StoringResponseBodyReturnValueAdvice.class));
    }

    @Test
    public void test() throws Exception {
        this.mockMvc.perform(get("/test/greeting/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Greeting : hello"));
    }

    /**
     * Test only one mapping : {@link TestController#greeting(String)}
     *
     * @param event {@link WebEndpointMappingsReadyEvent}
     */
    @EventListener(WebEndpointMappingsReadyEvent.class)
    public void onWebEndpointMappingsReadyEvent(WebEndpointMappingsReadyEvent event) {
        // Only TestController
        Collection<WebEndpointMapping> mappings = event.getMappings();
        assertFalse(mappings.isEmpty());
        WebEndpointMapping webEndpointMapping = mappings.iterator().next();
        String[] patterns = webEndpointMapping.getPatterns();
        assertEquals(1, patterns.length);
    }

    /**
     * Test only one method : {@link TestController#greeting(String)}
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
        assertEquals(TestController.class, bean.getClass());
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
