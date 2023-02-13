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

import io.github.microsphere.spring.beans.factory.config.NamedBeanHolderComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The composite {@link BeanEventListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BeanEventListeners implements BeanEventListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanEventListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<NamedBeanHolder<BeanEventListener>> namedListeners;

    private final int listenerCount;

    public BeanEventListeners(ConfigurableListableBeanFactory beanFactory) {
        this.namedListeners = getBeanListeners(beanFactory);
        this.listenerCount = namedListeners.size();
    }

    private List<NamedBeanHolder<BeanEventListener>> getBeanListeners(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanEventListener> beanEventListenersMap = beanFactory.getBeansOfType(BeanEventListener.class);
        List<NamedBeanHolder<BeanEventListener>> namedListeners = new ArrayList<>(beanEventListenersMap.size());
        for (Map.Entry<String, BeanEventListener> entry : beanEventListenersMap.entrySet()) {
            NamedBeanHolder<BeanEventListener> namedListener = new NamedBeanHolder<>(entry.getKey(), entry.getValue());
            namedListeners.add(namedListener);
        }
        namedListeners.sort(NamedBeanHolderComparator.INSTANCE);
        return namedListeners;
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(listener -> listener.onBeanDefinitionReady(beanName, mergedBeanDefinition), "onBeanDefinitionReady");
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition), "onBeforeBeanInstantiate");
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        iterate(listener -> listener.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean), "onAfterBeanInstantiated");
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        iterate(listener -> listener.onBeanPropertyValuesReady(beanName, bean, pvs), "onBeanPropertyValuesReady");
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        iterate(listener -> listener.onBeforeBeanInitialize(beanName, bean), "onBeforeBeanInitialize");
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        iterate(listener -> listener.onAfterBeanInitialized(beanName, bean), "onAfterBeanInitialized");
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        iterate(listener -> listener.onBeanReady(beanName, bean), "onBeanReady");
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        iterate(listener -> listener.onBeforeBeanDestroy(beanName, bean), "onBeforeBeanDestroy");
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        iterate(listener -> listener.onAfterBeanDestroy(beanName, bean), "onAfterBeanDestroy");
    }

    private void iterate(Consumer<BeanEventListener> listenerConsumer, String action) {
        for (int i = 0; i < listenerCount; i++) {
            NamedBeanHolder<BeanEventListener> namedListener = namedListeners.get(i);
            String beanName = namedListener.getBeanName();
            BeanEventListener listener = namedListener.getBeanInstance();
            try {
                listenerConsumer.accept(listener);
                logger.trace("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution '{}'", beanName, listener, i, action);
            } catch (Throwable e) {
                logger.error("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution '{}' failed", beanName, listener, i, action, e);
            }
        }
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
