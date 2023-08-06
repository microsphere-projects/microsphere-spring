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
package io.microsphere.spring.context.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;

import java.util.Iterator;

/**
 * {@link ApplicationEventInterceptor} Chain
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventInterceptor
 * @since 1.0.0
 */
class DefaultApplicationEventInterceptorChain implements ApplicationEventInterceptorChain {

    private final InterceptingApplicationEventMulticaster applicationEventMulticaster;

    private final Iterator<ApplicationEventInterceptor> iterator;

    public DefaultApplicationEventInterceptorChain(InterceptingApplicationEventMulticaster applicationEventMulticaster,
                                                   Iterable<ApplicationEventInterceptor> interceptors) {
        this.applicationEventMulticaster = applicationEventMulticaster;
        this.iterator = interceptors.iterator();
    }

    @Override
    public void intercept(ApplicationEvent event, ResolvableType eventType) {
        while (iterator.hasNext()) {
            ApplicationEventInterceptor interceptor = iterator.next();
            interceptor.intercept(event, eventType, this);
            return;
        }
    applicationEventMulticaster.doMulticastEvent(event, eventType);
    }
}
