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
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.test.RouterFunctionTestConfig;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

/**
 * {@link EnableWebMvcExtension} Test when all status are disabled with test cases:
 * <ul>
 *     <li>No {@link WebEndpointMapping} will be exposed from {@link TestController} and {@link RouterFunctionTestConfig}</li>
 *     <li>All {@link HandlerMethod HandlerMethods} of {@link TestController} will not be intercepted</li>
 *     <li>No ApplicationEvent {@link WebEndpointMappingsReadyEvent} or {@link HandlerMethodArgumentsResolvedEvent}
 *     will be published</li>
 *     <li>No {@link HandlerInterceptor} bean will be registered into {@link InterceptorRegistry}</li>
 *     <li>All {@link RequestBody} method arguments of {@link TestController} will not be stored</li>
 *     <li>All {@link ResponseBody} method return values of {@link TestController} will not be stored</li>
 *     <li>The {@link ReversedProxyHandlerMapping} bean will not be registered</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        EnableWebMvcExtensionAllDisabledTest.class
})
@EnableWebMvcExtension(
        registerWebEndpointMappings = false,
        interceptHandlerMethods = false,
        publishEvents = false,
        registerHandlerInterceptors = false,
        storeRequestBodyArgument = true,
        storeResponseBodyReturnValue = false,
        reversedProxyHandlerMapping = false
)
class EnableWebMvcExtensionAllDisabledTest extends AbstractEnableWebMvcExtensionTest {
}