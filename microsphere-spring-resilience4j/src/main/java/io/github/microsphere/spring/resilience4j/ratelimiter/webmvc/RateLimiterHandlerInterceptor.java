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
package io.github.microsphere.spring.resilience4j.ratelimiter.webmvc;

import io.github.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.github.microsphere.spring.resilience4j.common.webmvc.Resilience4jMethodHandlerInterceptor;
import io.github.resilience4j.core.Registry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link HandlerInterceptor} based on Resilience4j {@link RateLimiter}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @see RateLimiter
 * @since 1.0.0
 */
public class RateLimiterHandlerInterceptor extends Resilience4jMethodHandlerInterceptor<RateLimiter, RateLimiterConfig> {

    public RateLimiterHandlerInterceptor(Registry<RateLimiter, RateLimiterConfig> registry) {
        super(registry);
    }

    @Override
    protected void preHandle(Resilience4jContext<RateLimiter> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Throwable {
        context.start(r -> {
            r.acquirePermission();
        });
    }

    @Override
    protected void postHandle(Resilience4jContext<RateLimiter> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Throwable {
        context.end((RateLimiter, duration) -> RateLimiter.onResult(modelAndView));
    }

    @Override
    protected void afterCompletion(Resilience4jContext<RateLimiter> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Throwable {
        context.end((RateLimiter, duration) -> {
            if (ex != null) {
                RateLimiter.onError(ex);
            }
        });
    }

    @Override
    protected RateLimiter createEntry(String name) {
        return RateLimiter.of(name, getConfiguration(name), registry.getTags());
    }

}
