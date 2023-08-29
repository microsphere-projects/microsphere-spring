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
package io.microsphere.spring.resilience4j.bulkhead.web;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.core.Registry;
import io.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.microsphere.spring.resilience4j.common.web.Resilience4jHandlerMethodInterceptor;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * {@link HandlerInterceptor} based on Resilience4j {@link Bulkhead}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerInterceptor
 * @see Bulkhead
 * @since 1.0.0
 */
public class BulkheadHandlerMethodInterceptor extends Resilience4jHandlerMethodInterceptor<Bulkhead, BulkheadConfig> {

    public BulkheadHandlerMethodInterceptor(Registry<Bulkhead, BulkheadConfig> registry) {
        super(registry);
    }

    @Override
    protected void beforeExecute(Resilience4jContext<Bulkhead> context, HandlerMethod handlerMethod, Object[] args, NativeWebRequest request) throws Throwable {
        context.start(Bulkhead::acquirePermission);
    }

    @Override
    protected void afterExecute(Resilience4jContext<Bulkhead> context, HandlerMethod handlerMethod, Object[] args, Object returnValue, Throwable error, NativeWebRequest request) throws Throwable {
        context.end((bulkhead, duration) -> bulkhead.onComplete());
    }

    @Override
    protected Bulkhead createEntry(String name) {
        return Bulkhead.of(name, getConfiguration(name), registry.getTags());
    }

}
