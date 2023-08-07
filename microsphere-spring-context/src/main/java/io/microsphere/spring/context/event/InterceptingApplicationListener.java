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
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.context.event.GenericApplicationListenerAdapter;
import org.springframework.core.ResolvableType;

import java.util.List;

/**
 * Intercepting {@link ApplicationListener} Wrapper
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class InterceptingApplicationListener implements GenericApplicationListener {

    private final ApplicationListener<?> delegate;

    private final GenericApplicationListener smartListener;

    private final List<ApplicationListenerInterceptor> interceptors;

    InterceptingApplicationListener(ApplicationListener<?> delegate, List<ApplicationListenerInterceptor> interceptors) {
        this.delegate = delegate;
        this.smartListener = (delegate instanceof GenericApplicationListener ?
                (GenericApplicationListener) delegate : new GenericApplicationListenerAdapter(delegate));
        this.interceptors = interceptors;
    }

    @Override
    public boolean supportsEventType(ResolvableType eventType) {
        return smartListener.supportsEventType(eventType);
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(this.interceptors, this::onEvent);
        chain.intercept(delegate, event);
    }

    private void onEvent(ApplicationListener applicationListener, ApplicationEvent event) {
        applicationListener.onApplicationEvent(event);
    }

    public ApplicationListener<?> getDelegate() {
        return delegate;
    }

}
