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

import org.junit.Test;

import static org.junit.Assert.assertNull;

/**
 * {@link WebMvcConfigurerAdapter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebMvcConfigurerAdapter
 * @since 1.0.0
 */
public class WebMvcConfigurerAdapterTest implements WebMvcConfigurerAdapter {

    @Test
    public void testConfigurePathMatch() {
        WebMvcConfigurerAdapter.super.configurePathMatch(null);
    }

    @Test
    public void testConfigureContentNegotiation() {
        WebMvcConfigurerAdapter.super.configureContentNegotiation(null);
    }

    @Test
    public void testConfigureAsyncSupport() {
        WebMvcConfigurerAdapter.super.configureAsyncSupport(null);
    }

    @Test
    public void testConfigureDefaultServletHandling() {
        WebMvcConfigurerAdapter.super.configureDefaultServletHandling(null);
    }

    @Test
    public void testAddFormatters() {
        WebMvcConfigurerAdapter.super.addFormatters(null);
    }

    @Test
    public void testAddInterceptors() {
        WebMvcConfigurerAdapter.super.addInterceptors(null);
    }

    @Test
    public void testAddResourceHandlers() {
        WebMvcConfigurerAdapter.super.addResourceHandlers(null);
    }

    @Test
    public void testAddCorsMappings() {
        WebMvcConfigurerAdapter.super.addCorsMappings(null);
    }

    @Test
    public void testAddViewControllers() {
        WebMvcConfigurerAdapter.super.addViewControllers(null);
    }

    @Test
    public void testConfigureViewResolvers() {
        WebMvcConfigurerAdapter.super.configureViewResolvers(null);
    }

    @Test
    public void testAddArgumentResolvers() {
        WebMvcConfigurerAdapter.super.addArgumentResolvers(null);
    }

    @Test
    public void testAddReturnValueHandlers() {
        WebMvcConfigurerAdapter.super.addReturnValueHandlers(null);
    }

    @Test
    public void testConfigureMessageConverters() {
        WebMvcConfigurerAdapter.super.configureMessageConverters(null);
    }

    @Test
    public void testExtendMessageConverters() {
        WebMvcConfigurerAdapter.super.extendMessageConverters(null);
    }

    @Test
    public void testConfigureHandlerExceptionResolvers() {
        WebMvcConfigurerAdapter.super.configureHandlerExceptionResolvers(null);
    }

    @Test
    public void testExtendHandlerExceptionResolvers() {
        WebMvcConfigurerAdapter.super.extendHandlerExceptionResolvers(null);
    }

    @Test
    public void testGetValidator() {
        assertNull(WebMvcConfigurerAdapter.super.getValidator());
    }

    @Test
    public void testGetMessageCodesResolver() {
        assertNull(WebMvcConfigurerAdapter.super.getMessageCodesResolver());
    }
}