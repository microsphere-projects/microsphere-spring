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

import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.webflux.context.event.ServerRequestHandledEvent;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.server.filter.RequestContextWebFilter;
import org.springframework.stereotype.Controller;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.reactive.function.server.RouterFunction;

import static io.microsphere.spring.web.util.RequestContextStrategy.DEFAULT;

/**
 * {@link EnableWebFluxExtension} Test when all status are disabled with test cases:
 * <ul>
 *     <li>No {@link WebEndpointMapping} will be exposed from from {@link Controller @Controller} and {@link RouterFunction}</li>
 *     <li>No {@link HandlerMethod HandlerMethods} of {@link Controller @Controller} will be intercepted</li>
 *     <li>No ApplicationEvent {@link WebEndpointMappingsReadyEvent} or {@link HandlerMethodArgumentsResolvedEvent}
 *     or {@link ServerRequestHandledEvent} will be published</li>
 *     <li>No {@link RequestContextWebFilter} bean will be registered</li>
 *     <li>No {@link RequestBody} method arguments of {@link Controller @Controller} will be stored</li>
 *     <li>No {@link ResponseBody} method return values of {@link Controller @Controller} will be stored</li>
 *     <li>No {@link ReversedProxyHandlerMapping} bean will be registered</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        EnableWebFluxExtensionAllDisabledTest.class
})
@EnableWebFluxExtension(
        registerWebEndpointMappings = false,
        interceptHandlerMethods = false,
        publishEvents = false,
        requestContextStrategy = DEFAULT,
        storeRequestBodyArgument = false,
        storeResponseBodyReturnValue = false
)
class EnableWebFluxExtensionAllDisabledTest extends AbstractEnableWebFluxExtensionTest {
}