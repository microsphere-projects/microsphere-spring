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

import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * The configuration class for {@link EnableWebMvcExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see WebMvcConfigurer
 * @since 1.0.0
 */
public class WebMvcExtensionConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    private ObjectProvider<LazyCompositeHandlerInterceptor> lazyCompositeHandlerInterceptorProvider;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LazyCompositeHandlerInterceptor lazyCompositeHandlerInterceptor = lazyCompositeHandlerInterceptorProvider.getIfAvailable();
        if (lazyCompositeHandlerInterceptor != null) {
            registry.addInterceptor(lazyCompositeHandlerInterceptor);
        }
    }
}
