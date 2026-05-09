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

package io.microsphere.spring.webmvc.config;

import org.springframework.format.FormatterRegistry;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.validation.MessageCodesResolver;
import org.springframework.validation.Validator;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodReturnValueHandler;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * The {@link WebMvcConfigurer} adapter interface replaces {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter},
 * which breaks the single inheritance rule of Java using Java 8 default methods.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
 * @since 1.0.0
 */
public interface WebMvcConfigurerAdapter extends WebMvcConfigurer {

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configurePathMatch(PathMatchConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureAsyncSupport(AsyncSupportConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addFormatters(FormatterRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addInterceptors(InterceptorRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addResourceHandlers(ResourceHandlerRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addCorsMappings(CorsRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addViewControllers(ViewControllerRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureViewResolvers(ViewResolverRegistry registry) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void configureHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation is empty.
     */
    @Override
    default void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> exceptionResolvers) {
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns {@code null}.
     */
    @Override
    default Validator getValidator() {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>This implementation returns {@code null}.
     */
    @Override
    default MessageCodesResolver getMessageCodesResolver() {
        return null;
    }
}
