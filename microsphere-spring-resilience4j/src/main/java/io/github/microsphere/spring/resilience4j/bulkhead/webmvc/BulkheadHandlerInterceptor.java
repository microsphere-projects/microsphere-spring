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
package io.github.microsphere.spring.resilience4j.bulkhead.webmvc;

import io.github.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.github.microsphere.spring.resilience4j.common.webmvc.Resilience4jMethodHandlerInterceptor;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.core.Registry;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerInterceptor} based on Resilience4j {@link Bulkhead}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @see Bulkhead
 * @since 1.0.0
 */
public class BulkheadHandlerInterceptor extends Resilience4jMethodHandlerInterceptor<Bulkhead, BulkheadConfig> {

    public BulkheadHandlerInterceptor(Registry<Bulkhead, BulkheadConfig> registry) {
        super(registry);
    }

    @Override
    protected void preHandle(Resilience4jContext<Bulkhead> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Throwable {
        context.start(Bulkhead::acquirePermission);
    }

    @Override
    protected void postHandle(Resilience4jContext<Bulkhead> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Throwable {
    }

    @Override
    protected void afterCompletion(Resilience4jContext<Bulkhead> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Throwable {
        context.end((bulkhead, duration) -> {
            if (ex != null) {
                bulkhead.onComplete();
            }
        });
    }

    @Override
    protected Bulkhead createEntry(String name) {
        return Bulkhead.of(name, getConfiguration(name), registry.getTags());
    }

}
