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

import io.microsphere.spring.test.web.controller.TestController;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webflux.context.event.ServerRequestHandledEvent;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.server.filter.RequestContextWebFilter;
import io.microsphere.spring.webflux.test.EnableWebFluxExtensionInterceptorsTestConfig;
import io.microsphere.spring.webflux.test.RouterFunctionTestConfig;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import static io.microsphere.spring.web.util.RequestContextStrategy.THREAD_LOCAL;

/**
 * {@link EnableWebFluxExtension} Test with test cases :
 * <ul>
 *     <li>All {@link WebEndpointMapping WebEndpointMappings} will be exposed from {@link TestController} and
 *     {@link RouterFunctionTestConfig}</li>
 *     <li>The {@link EnableWebFluxExtensionInterceptorsTestConfig}({@link HandlerMethodArgumentInterceptor}
 *     and {@link HandlerMethodInterceptor}) bean will be registered and intercept the
 *     {@link HandlerMethod HandlerMethods} of {@link Controller Controllers}</li>
 *     <li>The {@link WebEndpointMappingsReadyEvent}, {@link HandlerMethodArgumentsResolvedEvent}
 *     and {@link ServerRequestHandledEvent} will be published</li>
 *     <li>The {@link RequestContextWebFilter} with {@link ThreadLocal} bean will be registered</li>
 *     <li>The {@link RequestBody} method arguments of {@link Controller Controllers} will be stored</li>
 *     <li>The {@link ResponseBody} method return values of {@link Controller Controllers} will be stored</li>
 *     <li>The {@link ReversedProxyHandlerMapping} bean will not be registered</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        EnableWebFluxExtensionInterceptorsTest.class,
        EnableWebFluxExtensionInterceptorsTestConfig.class
})
@EnableWebFluxExtension(requestContextStrategy = THREAD_LOCAL)
class EnableWebFluxExtensionInterceptorsTest extends AbstractEnableWebFluxExtensionTest {
}