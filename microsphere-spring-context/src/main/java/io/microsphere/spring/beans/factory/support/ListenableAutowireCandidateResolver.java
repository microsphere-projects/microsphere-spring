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
package io.microsphere.spring.beans.factory.support;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.List;

import static io.microsphere.spring.beans.factory.support.AutowireCandidateResolvingListener.loadListeners;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.util.ArrayUtils.combine;

/**
 * The decorator class of {@link AutowireCandidateResolver} to listen to the resolving process of autowire candidate by
 * {@link AutowireCandidateResolvingListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AutowireCandidateResolver
 * @see AutowireCandidateResolvingListener
 * @see CompositeAutowireCandidateResolvingListener
 * @see DefaultListableBeanFactory#setAutowireCandidateResolver(AutowireCandidateResolver)
 * @see BeanFactoryPostProcessor
 * @since 1.0.0
 */
public class ListenableAutowireCandidateResolver implements AutowireCandidateResolver, BeanFactoryPostProcessor {

    private AutowireCandidateResolver delegate;

    private CompositeAutowireCandidateResolvingListener compositeListener;

    private ConfigurableListableBeanFactory beanFactory;

    public ListenableAutowireCandidateResolver() {
    }

    public void addListener(AutowireCandidateResolvingListener one, AutowireCandidateResolvingListener... more) {
        addListeners(combine(one, more));
    }

    public void addListeners(AutowireCandidateResolvingListener[] listeners) {
        addListeners(Arrays.asList(listeners));
    }

    public void addListeners(List<AutowireCandidateResolvingListener> listeners) {
        compositeListener.addListeners(listeners);
    }

    @Override
    public boolean isAutowireCandidate(BeanDefinitionHolder bdHolder, DependencyDescriptor descriptor) {
        return delegate.isAutowireCandidate(bdHolder, descriptor);
    }

    @Override
    public boolean isRequired(DependencyDescriptor descriptor) {
        return delegate.isRequired(descriptor);
    }

    @Override
    public boolean hasQualifier(DependencyDescriptor descriptor) {
        return delegate.hasQualifier(descriptor);
    }

    @Nullable
    @Override
    public Object getSuggestedValue(DependencyDescriptor descriptor) {
        Object suggestedValue = delegate.getSuggestedValue(descriptor);
        compositeListener.suggestedValueResolved(descriptor, suggestedValue);
        return suggestedValue;
    }

    @Nullable
    @Override
    public Object getLazyResolutionProxyIfNecessary(DependencyDescriptor descriptor, String beanName) {
        Object proxy = delegate.getLazyResolutionProxyIfNecessary(descriptor, beanName);
        compositeListener.lazyProxyResolved(descriptor, beanName, proxy);
        return proxy;
    }

    @Nullable
    @Override
    public Class<?> getLazyResolutionProxyClass(DependencyDescriptor descriptor, String beanName) {
        Class<?> proxyClass = delegate.getLazyResolutionProxyClass(descriptor, beanName);
        compositeListener.lazyProxyClassResolved(descriptor, beanName, proxyClass);
        return proxyClass;
    }

    @Override
    public AutowireCandidateResolver cloneIfNecessary() {
        return delegate.cloneIfNecessary();
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        wrap(beanFactory);
    }

    /**
     * Wraps {@link AutowireCandidateResolver} as the {@link ListenableAutowireCandidateResolver} and then register to
     * the given {@link DefaultListableBeanFactory}
     *
     * @param beanFactory {@link DefaultListableBeanFactory}
     */
    public void wrap(BeanFactory beanFactory) {
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        AutowireCandidateResolver autowireCandidateResolver = dbf.getAutowireCandidateResolver();
        if (autowireCandidateResolver != null) {
            List<AutowireCandidateResolvingListener> listeners = loadListeners(beanFactory);
            CompositeAutowireCandidateResolvingListener compositeListener = new CompositeAutowireCandidateResolvingListener(listeners);
            this.delegate = autowireCandidateResolver;
            this.compositeListener = compositeListener;
            dbf.setAutowireCandidateResolver(this);
        }
    }
}
