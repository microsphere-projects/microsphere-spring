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
import io.microsphere.spring.web.metadata.WebEndpointMappingRegistry;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import io.microsphere.spring.webmvc.advice.StoringRequestBodyArgumentAdvice;
import io.microsphere.spring.webmvc.metadata.WebEndpointMappingRegistrar;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
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
     * @see WebEndpointMappingRegistry
     * @see WebEndpointMappingRegistrar
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
     * Indicate whether the {@link InterceptorRegistry} registers the beans of {@link HandlerInterceptor}
     * by the specified types
     *
     * @return <code>false</code> as default
     * @see WebMvcConfigurer#addInterceptors(InterceptorRegistry)
     * @see InterceptorRegistry
     */
    Class<? extends HandlerInterceptor>[] registerHandlerInterceptors() default {};

    /**
     * Indicate that Stores the {@link MethodParameter argument} of {@link HandlerMethod} that annotated {@link RequestBody}
     *
     * @return <code>false</code> as default
     * @see RequestBody
     * @see StoringRequestBodyArgumentAdvice
     * @see HandlerMethod
     */
    boolean storeRequestBodyArgument() default false;

    /**
     * Stores the return value of {@link HandlerMethod} before write as the {@link ResponseBody}
     *
     * @return <code>false</code> as default
     * @see ResponseBody
     */
    boolean storeResponseBodyReturnValue() default false;
}
