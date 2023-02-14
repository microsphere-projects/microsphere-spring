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
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * {@link BeanEventListener} Adapter
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class BeanEventListenerAdapter implements BeanEventListener {

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
    }
}
