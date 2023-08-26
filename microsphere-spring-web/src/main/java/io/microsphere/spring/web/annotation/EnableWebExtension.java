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
package io.microsphere.spring.web.annotation;

import io.microsphere.spring.web.event.EventPublishingHandlerMethodInterceptor;
import io.microsphere.spring.web.event.HandlerMethodArgumentsResolvedEvent;
import io.microsphere.spring.web.event.RequestMappingMetadataReadyEvent;
import io.microsphere.spring.web.metadata.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.web.method.support.HandlerMethodArgumentInterceptor;
import io.microsphere.spring.web.method.support.HandlerMethodInterceptor;
import org.springframework.context.annotation.Import;
import org.springframework.web.method.HandlerMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Enable annotation to extend the features of Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.config.annotation.EnableWebMvc
 * @see org.springframework.web.reactive.config.EnableWebFlux
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Documented
@Inherited
@Import(WebExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebExtension {

    /**
     * Indicate whether Spring Web {@link HandlerMethod} should be intercepted.
     * If <code>true</code>, {@link HandlerMethodArgumentInterceptor} and {@link HandlerMethodInterceptor} beans
     * will be initialized and then be invoked around {@link HandlerMethod} being executed.
     *
     * @return <code>true</code> as default
     * @see HandlerMethodArgumentInterceptor
     * @see HandlerMethodInterceptor
     */
    boolean interceptHandlerMethods() default true;

    /**
     * Indicate whether it publishes the Spring Web extension events :
     * <ul>
     *     <li>{@link RequestMappingMetadataReadyEvent}</li>
     *     <li>{@link WebEndpointMappingsReadyEvent}</li>
     *     <li>{@link HandlerMethodArgumentsResolvedEvent}</li>
     * </ul>
     *
     * @return <code>true</code> as default
     * @see EventPublishingHandlerMethodInterceptor
     * @see RequestMappingMetadataReadyEvent
     * @see WebEndpointMappingsReadyEvent
     * @see HandlerMethodArgumentsResolvedEvent
     */
    boolean publishEvents() default true;

}
