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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static io.microsphere.spring.util.ApplicationContextUtils.asApplicationContext;
import static io.microsphere.spring.util.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.util.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.util.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.util.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.util.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.spring.util.BeanUtils.invokeBeanInterfaces;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * The utilities class for {@link SpringFactoriesLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
public abstract class SpringFactoriesLoaderUtils {

    private static final Logger logger = LoggerFactory.getLogger(SpringFactoriesLoaderUtils.class);

    public static void registerFactories(@Nullable BeanFactory bf, Class<?> factoryType) {
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

    public static <T> List<T> loadFactories(@Nullable ApplicationContext context, Class<T> factoryType) {
        return loadFactories(asConfigurableApplicationContext(context), factoryType);
    }

    public static <T> List<T> loadFactories(@Nullable ConfigurableApplicationContext context, Class<T> factoryType) {
        ClassLoader classLoader = context == null ? getDefaultClassLoader() : context.getClassLoader();
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeBeanInterfaces(factory, context);
        }
        return factories;
    }

    public static <T> List<T> loadFactories(@Nullable ConfigurableApplicationContext context, Class<T> factoryClass, Object... args) {
        int argsLength = args == null ? 0 : args.length;
        if (argsLength < 1) {
            return loadFactories(context, factoryClass);
        }

        ClassLoader classLoader = context == null ? getDefaultClassLoader() : context.getClassLoader();
        List<String> factoryClassNames = loadFactoryNames(factoryClass, classLoader);

        int factorySize = factoryClassNames.size();

        if (factorySize < 1) {
            logger.debug("No factory class {} were loaded from SpringFactoriesLoader[{}]", factoryClass.getName(),
                    SpringFactoriesLoader.FACTORIES_RESOURCE_LOCATION);
            return emptyList();
        }

        List<T> factories = new ArrayList<>(factorySize);

        for (String factoryClassName : factoryClassNames) {
            Class<?> factoryImplClass = resolveClassName(factoryClassName, classLoader);
            Constructor<T> constructor = findConstructor(factoryImplClass, args, argsLength);
            T factory = instantiateClass(constructor, args);
            invokeBeanInterfaces(factory, context);
            factories.add(factory);
        }

        return unmodifiableList(factories);
    }

    public static <T> List<T> loadFactories(@Nullable BeanFactory beanFactory, Class<T> factoryType) {
        ApplicationContext context = asApplicationContext(beanFactory);
        if (context != null) {
            return loadFactories(context, factoryType);
        }

        ConfigurableBeanFactory configurableBeanFactory = asConfigurableBeanFactory(beanFactory);

        ClassLoader classLoader = configurableBeanFactory == null ? getDefaultClassLoader() : configurableBeanFactory.getBeanClassLoader();
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeAwareInterfaces(factory, beanFactory, configurableBeanFactory);
        }
        return factories;
    }

    private static Constructor findConstructor(Class<?> factoryImplClass, Object[] args, int argsLength) {
        Constructor targetConstructor = null;
        Constructor[] constructors = factoryImplClass.getConstructors();
        boolean matched = true;
        for (Constructor constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            int parameterCount = parameterTypes.length;

            if (parameterCount != argsLength) {
                continue;
            }

            for (int i = 0; i < argsLength; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object arg = args[i];
                if (!parameterType.isInstance(arg)) {
                    matched = false;
                    continue;
                }
            }

            if (matched) {
                targetConstructor = constructor;
            }
        }

        if (targetConstructor == null) {
            throw new IllegalArgumentException(String.format("No Constructor of Factory class[name : %s] was found for arguments : %s",
                    factoryImplClass.getName(), arrayToCommaDelimitedString(args)));
        }

        return targetConstructor;
    }

}
