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
package io.microsphere.spring.resilience4j.circuitbreaker.web;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.Registry;
import io.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.microsphere.spring.resilience4j.common.web.Resilience4jHandlerMethodInterceptor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * {@link HandlerInterceptor} based on Resilience4j {@link CircuitBreaker}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @see CircuitBreaker
 * @since 1.0.0
 */
public class CircuitBreakerHandlerMethodInterceptor extends Resilience4jHandlerMethodInterceptor<CircuitBreaker, CircuitBreakerConfig> {

    public CircuitBreakerHandlerMethodInterceptor(Registry<CircuitBreaker, CircuitBreakerConfig> registry) {
        super(registry);
    }

    @Override
    protected void beforeExecute(Resilience4jContext<CircuitBreaker> context, HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Throwable {
        context.start(CircuitBreaker::acquirePermission);
    }

    @Override
    protected void afterExecute(Resilience4jContext<CircuitBreaker> context, HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Throwable {
        context.end((circuitBreaker, duration) -> {
            if (error == null) {
                circuitBreaker.onResult(duration, TimeUnit.NANOSECONDS, args);
            } else {
                circuitBreaker.onError(duration, TimeUnit.NANOSECONDS, error);
            }
        });
    }

    @Override
    protected CircuitBreaker createEntry(String name) {
        return CircuitBreaker.of(name, getConfiguration(name), registry.getTags());
    }

}
