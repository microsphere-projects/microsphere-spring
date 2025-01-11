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

import io.microsphere.logging.Logger;
import io.microsphere.spring.webmvc.interceptor.LazyCompositeHandlerInterceptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.length;

/**
 * The configuration class for {@link EnableWebMvcExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see WebMvcConfigurer
 * @see EnableWebMvcExtension
 * @since 1.0.0
 */
public class WebMvcExtensionConfiguration extends WebMvcConfigurerAdapter {

    private static final Logger logger = getLogger(WebMvcExtensionConfiguration.class);

    private final ObjectProvider<LazyCompositeHandlerInterceptor[]> lazyCompositeHandlerInterceptorProvider;

    public WebMvcExtensionConfiguration(ObjectProvider<LazyCompositeHandlerInterceptor[]> lazyCompositeHandlerInterceptorProvider) {
        this.lazyCompositeHandlerInterceptorProvider = lazyCompositeHandlerInterceptorProvider;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        LazyCompositeHandlerInterceptor[] lazyCompositeHandlerInterceptors = lazyCompositeHandlerInterceptorProvider.getIfAvailable();
        int length = length(lazyCompositeHandlerInterceptors);
        if (length == 0) {
            if (logger.isTraceEnabled()) {
                logger.trace("No LazyCompositeHandlerInterceptor Bean was registered.");
            }
            return;
        }
        if (logger.isTraceEnabled()) {
            logger.trace("{} LazyCompositeHandlerInterceptor Beans will be added into InterceptorRegistry.", length);
        }
        for (int i = 0; i < length; i++) {
            LazyCompositeHandlerInterceptor lazyCompositeHandlerInterceptor = lazyCompositeHandlerInterceptors[i];
            registry.addInterceptor(lazyCompositeHandlerInterceptor);
        }
    }
}
