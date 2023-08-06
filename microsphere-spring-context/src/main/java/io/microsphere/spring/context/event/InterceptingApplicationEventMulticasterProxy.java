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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.core.ResolvableType;

import java.util.function.Predicate;

/**
 * Intercepting {@link ApplicationEventMulticaster} Proxy
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventMulticaster
 * @since 1.0.0
 */
public class InterceptingApplicationEventMulticasterProxy implements ApplicationEventMulticaster, InitializingBean, BeanFactoryAware {

    private final ApplicationEventMulticaster delegate;

    private ObjectProvider<ApplicationEventInterceptor> applicationEventInterceptors;

    private ObjectProvider<ApplicationListenerInterceptor> applicationListenerInterceptors;

    private BeanFactory beanFactory;

    public InterceptingApplicationEventMulticasterProxy(ApplicationEventMulticaster delegate) {
        this.delegate = delegate;
    }

    public void init(ObjectProvider<ApplicationEventInterceptor> applicationEventInterceptors,
                     ObjectProvider<ApplicationListenerInterceptor> applicationListenerInterceptors) {


    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        delegate.addApplicationListener(listener);
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        delegate.addApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        delegate.removeApplicationListener(listener);
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        delegate.removeApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {
        delegate.removeApplicationListeners(predicate);
    }

    @Override
    public void removeApplicationListenerBeans(Predicate<String> predicate) {
        delegate.removeApplicationListenerBeans(predicate);
    }

    @Override
    public void removeAllListeners() {
        delegate.removeAllListeners();
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        delegate.multicastEvent(event);
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        delegate.multicastEvent(event, eventType);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanFactory beanFactory = this.beanFactory;
        this.applicationEventInterceptors = beanFactory.getBeanProvider(ApplicationEventInterceptor.class);
        this.applicationListenerInterceptors = beanFactory.getBeanProvider(ApplicationListenerInterceptor.class);
    }
}
