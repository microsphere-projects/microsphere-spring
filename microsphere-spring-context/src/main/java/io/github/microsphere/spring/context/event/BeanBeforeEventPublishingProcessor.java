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

import io.github.microsphere.spring.util.BeanRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DestructionAwareBeanPostProcessor;
import org.springframework.beans.factory.config.InstantiationAwareBeanPostProcessorAdapter;
import org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.InstantiationStrategy;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Bean Before-Event Publishing Processor
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class BeanBeforeEventPublishingProcessor extends InstantiationAwareBeanPostProcessorAdapter implements BeanDefinitionRegistryPostProcessor, DestructionAwareBeanPostProcessor, InstantiationStrategy {

    private BeanDefinitionRegistry registry;

    private InstantiationStrategy instantiationStrategyDelegate;

    private BeanEventListeners beanEventListeners;

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        this.registry = registry;
        prepareBeanDefinitions(registry);
    }

    private void prepareBeanDefinitions(BeanDefinitionRegistry registry) {
        String[] beanNames = registry.getBeanDefinitionNames();
        int length = beanNames.length;
        List<BeanDefinitionHolder> beanDefinitionHolders = new ArrayList<>(length);
        for (int i = 0; i < length; i++) {
            String beanName = beanNames[i];
            // add current bean definition with name into holders that will be registered again
            BeanDefinition beanDefinition = registry.getBeanDefinition(beanName);
            beanDefinitionHolders.add(new BeanDefinitionHolder(beanDefinition, beanName));
            // remove current bean definition
            registry.removeBeanDefinition(beanName);
        }

        // register BeanAfterEventPublishingProcessor.Installer ensuring it's the first bean definition
        BeanRegistrar.registerBeanDefinition(registry, BeanAfterEventPublishingProcessor.Initializer.class);

        // re-register previous bean definitions
        beanDefinitionHolders.forEach(beanDefinitionHolder -> {
            BeanDefinitionReaderUtils.registerBeanDefinition(beanDefinitionHolder, registry);
        });
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        registerBeanEventListeners(beanFactory);
        decorateInstantiationStrategy(beanFactory);
        beanFactory.addBeanPostProcessor(this);
    }

    private void registerBeanEventListeners(ConfigurableListableBeanFactory context) {
        BeanEventListeners beanEventListeners = new BeanEventListeners(context);
        beanEventListeners.registerBean(registry);
        this.beanEventListeners = beanEventListeners;
    }

    private void decorateInstantiationStrategy(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof AbstractAutowireCapableBeanFactory) {
            this.instantiationStrategyDelegate = getInstantiationStrategyDelegate(beanFactory);
            if (instantiationStrategyDelegate != this) {
                AbstractAutowireCapableBeanFactory autowireCapableBeanFactory = (AbstractAutowireCapableBeanFactory) beanFactory;
                autowireCapableBeanFactory.setInstantiationStrategy(this);
            }
        }
    }

    private InstantiationStrategy getInstantiationStrategyDelegate(ConfigurableListableBeanFactory beanFactory) {
        InstantiationStrategy instantiationStrategy = null;
        try {
            Method method = AbstractAutowireCapableBeanFactory.class.getDeclaredMethod("getInstantiationStrategy");
            method.setAccessible(true);
            instantiationStrategy = (InstantiationStrategy) method.invoke(beanFactory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return instantiationStrategy;
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner) throws BeansException {
        return aroundInstantiate(beanName, bd, () -> instantiationStrategyDelegate.instantiate(bd, beanName, owner));
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Constructor<?> ctor, Object... args) throws BeansException {
        return aroundInstantiate(beanName, bd, () -> instantiationStrategyDelegate.instantiate(bd, beanName, owner, ctor, args));
    }

    @Override
    public Object instantiate(RootBeanDefinition bd, String beanName, BeanFactory owner, Object factoryBean, Method factoryMethod, Object... args) throws BeansException {
        return aroundInstantiate(beanName, bd, () -> instantiationStrategyDelegate.instantiate(bd, beanName, owner, factoryBean, factoryMethod, args));
    }

    private Object aroundInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Supplier<Object> beanSupplier) {
        this.beanEventListeners.onBeforeBeanInstantiate(beanName, mergedBeanDefinition);
        Object bean = beanSupplier.get();
        this.beanEventListeners.onAfterBeanInstantiated(beanName, mergedBeanDefinition, bean);
        return bean;
    }

    public PropertyValues postProcessPropertyValues(PropertyValues pvs, PropertyDescriptor[] pds, Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onBeanPropertyValuesReady(beanName, bean, pvs);
        return pvs;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onBeforeBeanInitialize(beanName, bean);
        return bean;
    }

    @Override
    public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
        this.beanEventListeners.onBeforeBeanDestroy(beanName, bean);
    }
}
