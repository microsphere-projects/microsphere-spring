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
package io.microsphere.spring.web.method.support;

import io.microsphere.spring.context.OnceApplicationContextEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.MethodParameter;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static java.util.Collections.emptyList;

/**
 * {@link HandlerMethodAdvice} class delegates to the beans of {@link HandlerMethodArgumentInterceptor} and
 * {@link HandlerMethodInterceptor}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentInterceptor
 * @see HandlerMethodInterceptor
 * @since 1.0.0
 */
public class DelegatingHandlerMethodAdvice extends OnceApplicationContextEventListener<ContextRefreshedEvent>
        implements HandlerMethodAdvice {

    public static final String BEAN_NAME = "delegatingHandlerMethodAdvice";

    private static final Logger logger = LoggerFactory.getLogger(DelegatingHandlerMethodAdvice.class);

    private List<HandlerMethodArgumentInterceptor> argumentInterceptors = emptyList();

    private int argumentInterceptorsSize = 0;

    private List<HandlerMethodInterceptor> methodInterceptors = emptyList();

    private int methodInterceptorsSize = 0;

    @Override
    protected void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        initHandlerMethodArgumentInterceptors(context);
        initHandlerMethodInterceptors(context);
        log(context);
    }

    private void log(ApplicationContext context) {
        if (logger.isInfoEnabled()) {
            logger.info("{} HandlerMethodArgumentInterceptors and {} HandlerMethodInterceptors were initialized in the ApplicationContext[id : '{}']",
                    this.argumentInterceptorsSize, this.methodInterceptorsSize, context.getId());
        }
    }

    @Override
    public void beforeResolveArgument(MethodParameter parameter, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        if (argumentInterceptorsSize > 0) {
            for (int i = 0; i < argumentInterceptorsSize; i++) {
                HandlerMethodArgumentInterceptor interceptor = argumentInterceptors.get(i);
                interceptor.beforeResolveArgument(parameter, handlerMethod, webRequest);
            }
        }
    }

    @Override
    public void afterResolveArgument(MethodParameter parameter, Object resolvedArgument, HandlerMethod handlerMethod, NativeWebRequest webRequest) throws Exception {
        if (argumentInterceptorsSize > 0) {
            for (int i = 0; i < argumentInterceptorsSize; i++) {
                HandlerMethodArgumentInterceptor interceptor = argumentInterceptors.get(i);
                interceptor.afterResolveArgument(parameter, resolvedArgument, handlerMethod, webRequest);
            }
        }
    }

    @Override
    public void beforeExecuteMethod(HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Exception {
        if (methodInterceptorsSize > 0) {
            for (int i = 0; i < methodInterceptorsSize; i++) {
                HandlerMethodInterceptor interceptor = methodInterceptors.get(i);
                interceptor.beforeExecute(handlerMethod, args, request);
            }
        }
    }

    @Override
    public void afterExecuteMethod(HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Exception {
        if (methodInterceptorsSize > 0) {
            for (int i = 0; i < methodInterceptorsSize; i++) {
                HandlerMethodInterceptor interceptor = methodInterceptors.get(i);
                interceptor.afterExecute(handlerMethod, args, returnValue, error, request);
            }
        }
    }

    private void initHandlerMethodArgumentInterceptors(ApplicationContext context) {
        List<HandlerMethodArgumentInterceptor> argumentInterceptors = getSortedBeans(context, HandlerMethodArgumentInterceptor.class);
        this.argumentInterceptors = argumentInterceptors;
        this.argumentInterceptorsSize = argumentInterceptors.size();
    }

    private void initHandlerMethodInterceptors(ApplicationContext context) {
        List<HandlerMethodInterceptor> interceptors = getSortedBeans(context, HandlerMethodInterceptor.class);
        this.methodInterceptors = interceptors;
        this.methodInterceptorsSize = interceptors.size();
    }
}
