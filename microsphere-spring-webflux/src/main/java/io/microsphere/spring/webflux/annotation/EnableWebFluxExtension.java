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

import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.web.util.RequestAttributesUtils;
import io.microsphere.spring.webflux.context.event.ServerRequestHandledEvent;
import io.microsphere.spring.webflux.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webflux.server.filter.RequestHandledEventPublishingWebFilter;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.support.RequestHandledEvent;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable annotation to extend the features of Spring WebFlux
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.reactive.config.EnableWebFlux
 * @see EnableWebExtension
 * @see WebFluxExtensionBeanDefinitionRegistrar
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Import(WebFluxExtensionBeanDefinitionRegistrar.class)
@EnableWebExtension
public @interface EnableWebFluxExtension {

    /**
     * Indicate whether The Spring Web registers the instances of {@link WebEndpointMapping}
     * that source from Spring WebMVC, Spring WebFlux or Classical Servlet.
     *
     * @return <code>true</code> as default
     * @see WebEndpointMapping
     * @see WebEndpointMappingRegistry
     */
    @AliasFor(annotation = EnableWebExtension.class)
    boolean registerWebEndpointMappings() default true;

    /**
     * Indicate whether Spring Web {@link HandlerMethod} should be intercepted.
     * If <code>true</code>, {@link HandlerMethodArgumentInterceptor} and {@link HandlerMethodInterceptor} beans
     * will be initialized and then be invoked around {@link HandlerMethod} being executed.
     *
     * @return <code>true</code> as default
     * @see HandlerMethodArgumentInterceptor
     * @see HandlerMethodInterceptor
     */
    @AliasFor(annotation = EnableWebExtension.class)
    boolean interceptHandlerMethods() default true;

    /**
     * Indicate whether it publishes the Spring Web extension events:
     * <ul>
     *     <li>{@link HandlerMethodArgumentsResolvedEvent}({@link EnableWebExtension#interceptHandlerMethods() if enabled})</li>
     *     <li>{@link WebEndpointMappingsReadyEvent}({@link EnableWebExtension#registerWebEndpointMappings() if enabled})</li>
     *     <li>{@link ServerRequestHandledEvent} after {@link WebHandler#handle(ServerWebExchange) WebHandler was handled}</li>
     * </ul>
     *
     * @return <code>true</code> as default
     * @see WebEventPublisher
     * @see WebEndpointMappingsReadyEvent
     * @see HandlerMethodArgumentsResolvedEvent
     * @see RequestHandledEventPublishingWebFilter
     * @see ServerRequestHandledEvent
     * @see RequestHandledEvent
     */
    @AliasFor(annotation = EnableWebExtension.class)
    boolean publishEvents() default true;

    /**
     * Indicate that {@link RequestAttributes} stores the {@link MethodParameter argument} of {@link HandlerMethod} that annotated {@link RequestBody}
     *
     * @return <code>false</code> as default
     * @see RequestBody
     * @see HandlerMethod
     * @see RequestAttributesUtils#getHandlerMethodRequestBodyArgument(RequestAttributes, HandlerMethod)
     */
    boolean storeRequestBodyArgument() default false;

    /**
     * Indicate that {@link RequestAttributes} stores the return value of {@link HandlerMethod} before write as the {@link ResponseBody}
     *
     * @return <code>false</code> as default
     * @see ResponseBody
     * @see RequestAttributesUtils#getHandlerMethodReturnValue(RequestAttributes, HandlerMethod)
     */
    boolean storeResponseBodyReturnValue() default false;

    /**
     * Indicate whether the {@link ReversedProxyHandlerMapping} is enabled or not.
     *
     * @return <code>false</code> as default
     * @see ReversedProxyHandlerMapping
     */
    boolean reversedProxyHandlerMapping() default false;
}
