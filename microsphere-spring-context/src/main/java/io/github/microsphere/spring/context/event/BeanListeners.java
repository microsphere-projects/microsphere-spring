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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The composite {@link BeanListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BeanListeners implements BeanListener {

    private static final Logger logger = LoggerFactory.getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<NamedBeanHolder<BeanListener>> namedListeners;

    private final int listenerCount;

    public BeanListeners(ConfigurableListableBeanFactory beanFactory) {
        this.namedListeners = getBeanListeners(beanFactory);
        this.listenerCount = namedListeners.size();
    }

    private List<NamedBeanHolder<BeanListener>> getBeanListeners(ConfigurableListableBeanFactory beanFactory) {
        Map<String, BeanListener> beanEventListenersMap = beanFactory.getBeansOfType(BeanListener.class);
        List<NamedBeanHolder<BeanListener>> namedListeners = new ArrayList<>(beanEventListenersMap.size());
        for (Map.Entry<String, BeanListener> entry : beanEventListenersMap.entrySet()) {
            NamedBeanHolder<BeanListener> namedListener = new NamedBeanHolder<>(entry.getKey(), entry.getValue());
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
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
        iterate(listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, constructor, args), "onBeforeBeanInstantiate");
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
        iterate(listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, factoryBean, factoryMethod, args), "onBeforeBeanInstantiate");
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

    private void iterate(Consumer<BeanListener> listenerConsumer, String action) {
        for (int i = 0; i < listenerCount; i++) {
            NamedBeanHolder<BeanListener> namedListener = namedListeners.get(i);
            String beanName = namedListener.getBeanName();
            BeanListener listener = namedListener.getBeanInstance();
            try {
                listenerConsumer.accept(listener);
                logger.trace("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution '{}'", beanName, listener, i, action);
            } catch (Throwable e) {
                logger.error("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution '{}' failed", beanName, listener, i, action, e);
            }
        }
    }

    public void registerBean(BeanDefinitionRegistry registry) {
        BeanDefinitionBuilder beanDefinitionBuilder = rootBeanDefinition(BeanListeners.class, () -> this);
        beanDefinitionBuilder.setPrimary(true);
        registry.registerBeanDefinition(BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
    }

    public static BeanListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanListeners.class);
    }
}
