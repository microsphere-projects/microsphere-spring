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
package io.microsphere.spring.util;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

import static io.microsphere.spring.util.ApplicationContextUtils.asApplicationContext;
import static io.microsphere.spring.util.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.util.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.util.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.util.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.util.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassUtils.resolveClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;

/**
 * The utilities class for {@link SpringFactoriesLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
public abstract class SpringFactoriesLoaderUtils {

    public static void registerFactories(Class<?> factoryType, BeanFactory bf) {
        BeanDefinitionRegistry registry = asBeanDefinitionRegistry(bf);
        if (registry == null) {
            return;
        }

        ConfigurableListableBeanFactory beanFactory = asConfigurableListableBeanFactory(bf);
        if (beanFactory == null) {
            return;
        }

        ClassLoader beanClassLoader = beanFactory.getBeanClassLoader();
        ClassLoader classLoader = beanClassLoader == null ? getDefaultClassLoader() : beanClassLoader;
        List<String> factoryNames = loadFactoryNames(factoryType, classLoader);
        for (String factoryName : factoryNames) {
            Class<?> beanClass = resolveClass(factoryName, classLoader);
            registerBeanDefinition(registry, beanClass);
        }
    }

    public static <T> List<T> loadFactories(Class<T> factoryType, ApplicationContext context) {
        ConfigurableApplicationContext applicationContext = asConfigurableApplicationContext(context);
        ClassLoader classLoader = applicationContext == null ? getDefaultClassLoader() : applicationContext.getClassLoader();
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeAwareInterfaces(factory, context, applicationContext);
        }
        return factories;
    }

    public static <T> List<T> loadFactories(Class<T> factoryType, BeanFactory beanFactory) {
        ApplicationContext context = asApplicationContext(beanFactory);
        if (context != null) {
            return loadFactories(factoryType, context);
        }
        ConfigurableBeanFactory cbf = asConfigurableBeanFactory(beanFactory);
        if (cbf != null) {
            return loadFactories(factoryType, cbf);
        }
        ClassLoader classLoader = getDefaultClassLoader();
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeAwareInterfaces(factory, beanFactory);
        }
        return factories;
    }

    public static <T> List<T> loadFactories(Class<T> factoryType, ConfigurableBeanFactory beanFactory) {
        ApplicationContext context = asApplicationContext(beanFactory);
        if (context != null) {
            return loadFactories(factoryType, context);
        }
        ClassLoader classLoader = beanFactory == null ? getDefaultClassLoader() : beanFactory.getBeanClassLoader();
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeAwareInterfaces(factory, beanFactory, beanFactory);
        }
        return factories;
    }
}
