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
package io.microsphere.spring.webmvc.interceptor;

import io.microsphere.lang.function.ThrowableConsumer;
import io.microsphere.lang.function.ThrowableFunction;
import io.microsphere.spring.context.event.OnceApplicationContextEventListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static io.microsphere.collection.CollectionUtils.size;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;

/**
 * Lazy {@link HandlerInterceptor} that is composited by {@link HandlerInterceptor} beans with the specified types
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @since 1.0.0
 */
public class LazyCompositeHandlerInterceptor extends OnceApplicationContextEventListener<ContextRefreshedEvent> implements HandlerInterceptor {

    /**
     * The bean name of {@link LazyCompositeHandlerInterceptor}
     */
    public static final String BEAN_NAME = "lazyCompositeHandlerInterceptor";

    private final Class<? extends HandlerInterceptor>[] interceptorClasses;

    private List<HandlerInterceptor> interceptors;

    public LazyCompositeHandlerInterceptor(Class<? extends HandlerInterceptor>... interceptorClasses) {
        this.interceptorClasses = interceptorClasses;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        return forEach(interceptor -> {
            return interceptor.preHandle(request, response, handler);
        });
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        forEach(interceptor -> {
            interceptor.postHandle(request, response, handler, modelAndView);
        });
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        forEach(interceptor -> {
            interceptor.afterCompletion(request, response, handler, ex);
        });
    }

    @Override
    protected void onApplicationContextEvent(ContextRefreshedEvent event) {
        ApplicationContext context = event.getApplicationContext();
        List<HandlerInterceptor> allInterceptors = new LinkedList<>();
        for (Class<? extends HandlerInterceptor> interceptorClass : interceptorClasses) {
            Collection<? extends HandlerInterceptor> interceptors = context.getBeansOfType(interceptorClass).values();
            allInterceptors.addAll(interceptors);
        }
        sort(allInterceptors);
        this.interceptors = allInterceptors;
    }

    private void forEach(ThrowableConsumer<HandlerInterceptor> interceptorConsumer) throws Exception {
        forEach(interceptor -> {
            interceptorConsumer.accept(interceptor);
            return Boolean.TRUE;
        });
    }

    private Boolean forEach(ThrowableFunction<HandlerInterceptor, Boolean> interceptorFunction) throws Exception {
        List<HandlerInterceptor> interceptors = this.interceptors;
        int length = size(interceptors);
        for (int i = 0; i < length; i++) {
            HandlerInterceptor interceptor = interceptors.get(i);
            try {
                Boolean result = interceptorFunction.apply(interceptor);
                if (Boolean.FALSE.equals(result)) {
                    return Boolean.FALSE;
                }
            } catch (Throwable e) {
                if (e instanceof Exception) {
                    throw (Exception) e;
                } else {
                    throw new RuntimeException(e);
                }
            }
        }
        return Boolean.TRUE;
    }
}
