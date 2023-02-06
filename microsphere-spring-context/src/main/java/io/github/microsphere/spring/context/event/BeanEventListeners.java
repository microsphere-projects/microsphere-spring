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
package io.github.microsphere.spring.context.event;

import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The composite {@link BeanEventListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BeanEventListeners implements BeanEventListener {

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<BeanEventListener> listeners;

    public BeanEventListeners(ConfigurableListableBeanFactory beanFactory) {
        this.listeners = new ArrayList<>(beanFactory.getBeansOfType(BeanEventListener.class).values());
        AnnotationAwareOrderComparator.sort(listeners);
    }

    @Override
    public void onBeanDefinitionReady(String beanName, BeanDefinition beanDefinition) {
        iterate(listener -> listener.onBeanDefinitionReady(beanName, beanDefinition));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, Class<?> beanClass) {
        iterate(listener -> listener.onBeforeBeanInstantiate(beanName, beanClass));
    }

    @Override
    public void onBeanInstantiating(String beanName, Object bean) {
        iterate(listener -> listener.onBeanInstantiating(beanName, bean));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, Object bean) {
        iterate(listener -> listener.onAfterBeanInstantiated(beanName, bean));
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        iterate(listener -> listener.onBeanPropertyValuesReady(beanName, bean, pvs));
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        iterate(listener -> listener.onBeforeBeanInitialize(beanName, bean));
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        iterate(listener -> listener.onAfterBeanInitialized(beanName, bean));
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        iterate(listener -> listener.onBeanReady(beanName, bean));
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        iterate(listener -> listener.onBeforeBeanDestroy(beanName, bean));
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        iterate(listener -> listener.onAfterBeanDestroy(beanName, bean));
    }

    private void iterate(Consumer<BeanEventListener> listenerConsumer) {
        listeners.forEach(listenerConsumer);
    }

    public void registerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = rootBeanDefinition(BeanEventListeners.class, () -> this);
        beanDefinitionBuilder.setPrimary(true);
        registry.registerBeanDefinition(BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
    }

    public static BeanEventListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanEventListeners.class);
    }
}
