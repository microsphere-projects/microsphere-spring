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

import io.microsphere.spring.webmvc.config.GenericWebMvcConfigurer;
import io.microsphere.spring.webmvc.event.WebMvcEventPublisher;
import io.microsphere.spring.webmvc.metadata.RequestMappingMetadataReadyEvent;
import io.microsphere.spring.webmvc.metadata.WebEndpointMappingsReadyEvent;
import io.microsphere.spring.webmvc.method.HandlerMethodArgumentsResolvedEvent;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
 * @see GenericWebMvcConfigurer
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(WebMvcExtensionBeanDefinitionRegistrar.class)
public @interface EnableWebMvcExtension {

    /**
     * Publishes the events around the Spring WebMVC
     *
     * @return <code>true</code> as default
     * @see WebMvcEventPublisher
     * @see RequestMappingMetadataReadyEvent
     * @see WebEndpointMappingsReadyEvent
     * @see HandlerMethodArgumentsResolvedEvent
     */
    boolean publishEvents() default true;


    /**
     * @return
     */
    boolean delegateHandlerInterceptor() default true;
}
