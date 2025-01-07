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

import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.config.NamedBeanHolderComparator;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.NamedBeanHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerFactoryBean;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerSpringFactoriesBeans;

/**
 * The composite {@link BeanListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BeanListeners implements BeanListener {

    private static final Logger logger = getLogger(BeanListeners.class);

    private static final String BEAN_NAME = "beanEventListeners";

    private final List<NamedBeanHolder<BeanListener>> namedListeners;

    private final int listenerCount;

    private final Set<String> readyBeanNames;

    public BeanListeners(ConfigurableListableBeanFactory beanFactory) {
        this.namedListeners = getBeanListeners(beanFactory);
        this.listenerCount = namedListeners.size();
        this.readyBeanNames = getReadyBeanNames(beanFactory);
    }

    static Set<String> getReadyBeanNames(ConfigurableListableBeanFactory beanFactory) {
        Set<String> readyBeanNames = new LinkedHashSet<>();
        String[] singletonNames = beanFactory.getSingletonNames();
        readyBeanNames.addAll(Arrays.asList(singletonNames));
        return readyBeanNames;
    }

    void setReadyBeanNames(Set<String> readyBeanNames) {
        this.readyBeanNames.clear();
        this.readyBeanNames.addAll(readyBeanNames);
    }

    private List<NamedBeanHolder<BeanListener>> getBeanListeners(ConfigurableListableBeanFactory beanFactory) {
        registerSpringFactoriesBeans(beanFactory, BeanListener.class);
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
    public boolean supports(String beanName) {
        return true;
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(beanName, listener -> listener.onBeanDefinitionReady(beanName, mergedBeanDefinition), "onBeanDefinitionReady");
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        iterate(beanName, listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition), "onBeforeBeanInstantiate");
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
        iterate(beanName, listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, constructor, args), "onBeforeBeanInstantiate");
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
        iterate(beanName, listener -> listener.onBeforeBeanInstantiate(beanName, mergedBeanDefinition, factoryBean, factoryMethod, args), "onBeforeBeanInstantiate");
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        iterate(beanName, listener -> listener.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean), "onAfterBeanInstantiated");
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        iterate(beanName, listener -> listener.onBeanPropertyValuesReady(beanName, bean, pvs), "onBeanPropertyValuesReady");
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        iterate(beanName, listener -> listener.onBeforeBeanInitialize(beanName, bean), "onBeforeBeanInitialize");
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        iterate(beanName, listener -> listener.onAfterBeanInitialized(beanName, bean), "onAfterBeanInitialized");
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        iterate(beanName, listener -> listener.onBeanReady(beanName, bean), "onBeanReady");
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        iterate(beanName, listener -> listener.onBeforeBeanDestroy(beanName, bean), "onBeforeBeanDestroy");
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        iterate(beanName, listener -> listener.onAfterBeanDestroy(beanName, bean), "onAfterBeanDestroy");
    }

    private void iterate(String beanName, Consumer<BeanListener> listenerConsumer, String action) {
        if (isIgnored(beanName)) {
            return;
        }
        for (int i = 0; i < listenerCount; i++) {
            NamedBeanHolder<BeanListener> namedListener = namedListeners.get(i);
            BeanListener listener = namedListener.getBeanInstance();
            String listenerBeanName = namedListener.getBeanName();
            try {
                if (listener.supports(beanName)) {
                    listenerConsumer.accept(listener);
                    logger.trace("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution {} -> '{}'", listenerBeanName, listener, i, beanName, action);
                }
            } catch (Throwable e) {
                logger.error("BeanEventListener[name : '{}' , bean : '{}', order : {}] execution {} -> '{}' failed", listenerBeanName, listener, i, beanName, action, e);
            }
        }
    }

    private boolean isIgnored(String beanName) {
        return this.readyBeanNames.contains(beanName) || BEAN_NAME.equals(beanName);
    }

    void registerBean(BeanDefinitionRegistry registry) {
//        BeanDefinitionBuilder beanDefinitionBuilder = rootBeanDefinition(BeanListeners.class, () -> this);
//        beanDefinitionBuilder.setPrimary(true);
//        registry.registerBeanDefinition(BEAN_NAME, beanDefinitionBuilder.getBeanDefinition());
        registerFactoryBean(registry, BEAN_NAME, this);
    }

    static BeanListeners getBean(BeanFactory beanFactory) {
        return beanFactory.getBean(BEAN_NAME, BeanListeners.class);
    }
}
