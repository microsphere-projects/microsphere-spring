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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.util.List;

import static io.microsphere.spring.util.BeanUtils.getSortedBeans;

/**
 * Intercepting {@link ApplicationEventMulticaster} based on {@link SimpleApplicationEventMulticaster}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventMulticaster
 * @see SimpleApplicationEventMulticaster
 * @since 1.0.0
 */
public class InterceptingApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private List<ApplicationEventInterceptor> applicationEventInterceptors;

    private List<ApplicationListenerInterceptor> applicationListenerInterceptors;

    @Override
    public final void multicastEvent(ApplicationEvent event, @Nullable ResolvableType eventType) {
        ResolvableType type = resolveEventType(event, eventType);
        DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(this.applicationEventInterceptors, this::doMulticastEvent);
        chain.intercept(event, type);
    }

    static ResolvableType resolveEventType(ApplicationEvent event, ResolvableType eventType) {
        return eventType != null ? eventType : resolveDefaultEventType(event);
    }

    static ResolvableType resolveDefaultEventType(ApplicationEvent event) {
        return ResolvableType.forInstance(event);
    }

    @Override
    protected final void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        DefaultApplicationListenerInterceptorChain chain = new DefaultApplicationListenerInterceptorChain(this.applicationListenerInterceptors, this::doInvokeListener);
        chain.intercept(listener, event);
    }

    protected void doMulticastEvent(ApplicationEvent event, ResolvableType eventType) {
        super.multicastEvent(event, eventType);
    }

    protected void doInvokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        super.invokeListener(listener, event);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        super.setBeanFactory(beanFactory);
        ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
        this.applicationEventInterceptors = getSortedBeans(listableBeanFactory, ApplicationEventInterceptor.class);
        this.applicationListenerInterceptors = getSortedBeans(listableBeanFactory, ApplicationListenerInterceptor.class);
    }
}
