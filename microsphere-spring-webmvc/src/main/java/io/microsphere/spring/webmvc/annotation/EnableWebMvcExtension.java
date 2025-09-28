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

import io.microsphere.spring.web.annotation.EnableWebExtension;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.event.WebEventPublisher;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMappingFilter;
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.web.metadata.WebEndpointMappingResolver;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.web.util.RequestAttributesUtils;
import io.microsphere.spring.web.util.RequestContextStrategy;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.handler.ReversedProxyHandlerMapping;
import io.microsphere.spring.webmvc.util.WebMvcUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Import;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.FrameworkServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static io.microsphere.spring.web.util.RequestContextStrategy.DEFAULT;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Enable annotation to extend the features of Spring WebMVC
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvc
 * @see EnableWebExtension
 * @see WebMvcExtensionBeanDefinitionRegistrar
 * @see WebMvcExtensionConfiguration
 * @since 1.0.0
 */
@Retention(RUNTIME)
@Target(TYPE)
@Documented
@Import(value = {
        WebMvcExtensionBeanDefinitionRegistrar.class,
        WebMvcExtensionConfiguration.class
})
@EnableWebExtension
public @interface EnableWebMvcExtension {

    /**
     * Indicate whether The Spring Web registers the instances of {@link WebEndpointMapping}
     * that source from Spring WebMVC, Spring WebFlux or Classical Servlet.
     *
     * @return <code>true</code> as default
     * @see WebEndpointMapping
     * @see WebEndpointMappingResolver
     * @see WebEndpointMappingRegistry
     * @see WebEndpointMappingFactory
     * @see WebEndpointMappingFilter
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
     * </ul>
     *
     * @return <code>true</code> as default
     * @see WebEventPublisher
     * @see WebEndpointMappingsReadyEvent
     * @see HandlerMethodArgumentsResolvedEvent
     */
    @AliasFor(annotation = EnableWebExtension.class)
    boolean publishEvents() default true;

    /**
     * Indicate where the {@link RequestAttributes} stores.
     *
     * @return {@link RequestContextStrategy#DEFAULT} as default
     * @see RequestAttributes
     * @see RequestContextHolder
     * @see RequestContextFilter
     * @see RequestContextListener
     * @see FrameworkServlet#initContextHolders(HttpServletRequest, LocaleContext, RequestAttributes)
     * @see RequestContextStrategy
     */
    @AliasFor(annotation = EnableWebExtension.class)
    RequestContextStrategy requestContextStrategy() default DEFAULT;

    /**
     * Indicate whether the {@link InterceptorRegistry} registers the beans of {@link HandlerInterceptor}.
     * If it specifies <code>true</code>, {@link #handlerInterceptors()} method will not work anymore.
     *
     * @return <code>false</code> as default
     * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
     * @see InterceptorRegistry
     */
    boolean registerHandlerInterceptors() default false;

    /**
     * Specify {@link HandlerInterceptor} types or its inherited types as Spring beans and then register into
     * {@link InterceptorRegistry}.
     * <p>
     * If {@link #registerHandlerInterceptors()} is <code>true</code>, specified types will be ignored.
     *
     * @return <code>null</code> as default
     * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
     * @see InterceptorRegistry
     */
    Class<? extends HandlerInterceptor>[] handlerInterceptors() default {};

    /**
     * Indicate that {@link RequestAttributes} stores the {@link MethodParameter argument} of {@link HandlerMethod} that annotated {@link RequestBody}
     *
     * @return <code>false</code> as default
     * @see RequestBody
     * @see StoringRequestBodyArgumentAdvice
     * @see HandlerMethod
     * @see WebMvcUtils#getHandlerMethodRequestBodyArgument(HttpServletRequest, Method)
     * @see RequestAttributesUtils#getHandlerMethodRequestBodyArgument(RequestAttributes, HandlerMethod)
     */
    boolean storeRequestBodyArgument() default false;

    /**
     * Indicate that {@link RequestAttributes} stores the return value of {@link HandlerMethod} before write as the {@link ResponseBody}
     *
     * @return <code>false</code> as default
     * @see ResponseBody
     * @see WebMvcUtils#getHandlerMethodReturnValue(HttpServletRequest, Method)
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
