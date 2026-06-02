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
package io.microsphere.spring.core.io.support;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.SetUtils.newFixedHashSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.spring.beans.BeanUtils.invokeBeanInterfaces;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.nullSafeBeanClassLoader;
import static io.microsphere.spring.context.ApplicationContextUtils.asApplicationContext;
import static io.microsphere.spring.context.ApplicationContextUtils.asConfigurableApplicationContext;
import static io.microsphere.spring.core.io.ResourceLoaderUtils.nullSafeClassLoader;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.beans.BeanUtils.instantiateClass;
import static org.springframework.core.io.support.SpringFactoriesLoader.FACTORIES_RESOURCE_LOCATION;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.StringUtils.arrayToCommaDelimitedString;

/**
 * The utilities class for {@link SpringFactoriesLoader}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
public abstract class SpringFactoriesLoaderUtils implements Utils {

    private static final Logger logger = getLogger(SpringFactoriesLoaderUtils.class);

    /**
     * Load the fully qualified classes of factory implementations of the
     * given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION}, using the default class loader.
     * <p>If a particular implementation class name is discovered more than once
     * for the given factory type, duplicates will be ignored.
     *
     * @param factoryType the interface or abstract class representing the factory
     * @throws IllegalArgumentException if an error occurs while loading factory names
     */
    public static <T> Set<Class<T>> loadFactoryClasses(Class<T> factoryType) {
        return loadFactoryClasses(factoryType, null);
    }

    /**
     * Load the fully qualified classes of factory implementations of the
     * given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION}, using the given class loader.
     * <p>If a particular implementation class name is discovered more than once
     * for the given factory type, duplicates will be ignored.
     *
     * @param factoryType the interface or abstract class representing the factory
     * @param classLoader the ClassLoader to use for loading resources; can be
     *                    {@code null} to use the default
     * @throws IllegalArgumentException if an error occurs while loading factory names
     */
    public static <T> Set<Class<T>> loadFactoryClasses(Class<?> factoryType, @Nullable ClassLoader classLoader) {
        List<String> factoryClassNames = loadFactoryNames(factoryType, classLoader);
        Set<Class<T>> factoryClasses = newFixedHashSet(factoryClassNames.size());
        for (String factoryClassName : factoryClassNames) {
            Class<T> factoryClass = (Class<T>) resolveClass(factoryClassName, classLoader);
            factoryClasses.add(factoryClass);
        }
        return unmodifiableSet(factoryClasses);
    }

    /**
     * Load the fully qualified class names of factory implementations of the
     * given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION}, using the default class loader.
     * <p>If a particular implementation class name is discovered more than once
     * for the given factory type, duplicates will be ignored.
     *
     * @param factoryType the interface or abstract class representing the factory
     * @throws IllegalArgumentException if an error occurs while loading factory names
     */
    public static List<String> loadFactoryNames(Class<?> factoryType) {
        return loadFactoryNames(factoryType, null);
    }

    /**
     * Load the fully qualified class names of factory implementations of the
     * given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION}, using the given class loader.
     * <p>If a particular implementation class name is discovered more than once
     * for the given factory type, duplicates will be ignored.
     *
     * @param factoryType the interface or abstract class representing the factory
     * @param classLoader the ClassLoader to use for loading resources; can be
     *                    {@code null} to use the default
     * @throws IllegalArgumentException if an error occurs while loading factory names
     */
    public static List<String> loadFactoryNames(Class<?> factoryType, @Nullable ClassLoader classLoader) {
        List<String> factoryClassNames = SpringFactoriesLoader.loadFactoryNames(factoryType, classLoader);
        if (logger.isTraceEnabled()) {
            logger.trace("Loaded factory class names of type {} : {}", factoryType.getName(), factoryClassNames);
        }
        return factoryClassNames;
    }

    /**
     * Load the factory implementations of the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION},
     * using the given {@link ApplicationContext}'s class loader.
     * <p>The returned factories will be invoked with {@link org.springframework.beans.factory.Aware} interfaces
     * and other bean lifecycle callbacks if the context is a {@link ConfigurableApplicationContext}.
     *
     * @param context     the {@link ApplicationContext} to use for loading resources and invoking callbacks; can be {@code null}
     * @param factoryType the interface or abstract class representing the factory
     * @param <T>         the factory type
     * @return the list of instantiated factory objects
     * @throws IllegalArgumentException if an error occurs while loading or instantiating factory classes
     * @see SpringFactoriesLoader#loadFactories(Class, ClassLoader)
     */
    public static <T> List<T> loadFactories(@Nullable ApplicationContext context, Class<T> factoryType) {
        return loadFactories(asConfigurableApplicationContext(context), factoryType);
    }

    /**
     * Load the factory implementations of the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION},
     * using the given {@link ConfigurableApplicationContext}'s class loader.
     * <p>The returned factories will be invoked with {@link org.springframework.beans.factory.Aware} interfaces
     * and other bean lifecycle callbacks.
     *
     * @param context     the {@link ConfigurableApplicationContext} to use for loading resources and invoking callbacks; can be {@code null}
     * @param factoryType the interface or abstract class representing the factory
     * @param <T>         the factory type
     * @return the list of instantiated factory objects
     * @throws IllegalArgumentException if an error occurs while loading or instantiating factory classes
     * @see SpringFactoriesLoader#loadFactories(Class, ClassLoader)
     */
    public static <T> List<T> loadFactories(@Nullable ConfigurableApplicationContext context, Class<T> factoryType) {
        ClassLoader classLoader = nullSafeClassLoader(context);
        List<T> factories = SpringFactoriesLoader.loadFactories(factoryType, classLoader);
        for (int i = 0; i < factories.size(); i++) {
            T factory = factories.get(i);
            invokeBeanInterfaces(factory, context);
        }
        return factories;
    }

    /**
     * Load the factory implementations of the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION},
     * using the given {@link ConfigurableApplicationContext}'s class loader and constructor arguments.
     * <p>The returned factories will be invoked with {@link org.springframework.beans.factory.Aware} interfaces
     * and other bean lifecycle callbacks.
     * <p>This method attempts to find a matching constructor for each factory implementation class based on the
     * provided arguments. If no arguments are provided, it delegates to {@link #loadFactories(ConfigurableApplicationContext, Class)}.
     *
     * @param context     the {@link ConfigurableApplicationContext} to use for loading resources and invoking callbacks; can be {@code null}
     * @param factoryType the interface or abstract class representing the factory
     * @param args        the constructor arguments to use when instantiating the factory implementations
     * @param <T>         the factory type
     * @return the list of instantiated factory objects
     * @throws IllegalArgumentException if an error occurs while loading or instantiating factory classes,
     *                                  or if no suitable constructor is found for the provided arguments
     * @see SpringFactoriesLoader#loadFactories(Class, ClassLoader)
     */
    public static <T> List<T> loadFactories(@Nullable ConfigurableApplicationContext context, Class<T> factoryType, Object... args) {
        int argsLength = length(args);
        if (argsLength < 1) {
            return loadFactories(context, factoryType);
        }

        ClassLoader classLoader = nullSafeClassLoader(context);
        List<String> factoryClassNames = loadFactoryNames(factoryType, classLoader);

        int factorySize = factoryClassNames.size();

        if (factorySize < 1) {
            if (logger.isTraceEnabled()) {
                logger.trace("No factory class {} were loaded from SpringFactoriesLoader[{}]", factoryType.getName(),
                        FACTORIES_RESOURCE_LOCATION);
            }
            return emptyList();
        }

        List<T> factories = newArrayList(factorySize);

        for (String factoryClassName : factoryClassNames) {
            Class<?> factoryImplClass = resolveClassName(factoryClassName, classLoader);
            Constructor<T> constructor = findConstructor(factoryImplClass, args, argsLength);
            T factory = instantiateClass(constructor, args);
            invokeBeanInterfaces(factory, context);
            factories.add(factory);
        }

        return unmodifiableList(factories);
    }

    /**
     * Load the factory implementations of the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION},
     * using the given {@link BeanFactory}'s class loader.
     * <p>If the {@link BeanFactory} is an {@link ApplicationContext}, it delegates to {@link #loadFactories(ApplicationContext, Class)}.
     * Otherwise, it uses the {@link BeanFactory}'s bean class loader to load factories and invokes
     * {@link org.springframework.beans.factory.Aware} interfaces on the instantiated factories.
     *
     * @param beanFactory the {@link BeanFactory} to use for loading resources and invoking callbacks; can be {@code null}
     * @param factoryType the interface or abstract class representing the factory
     * @param <T>         the factory type
     * @return the list of instantiated factory objects
     * @throws IllegalArgumentException if an error occurs while loading or instantiating factory classes
     * @see SpringFactoriesLoader#loadFactories(Class, ClassLoader)
     */
    public static <T> List<T> loadFactories(@Nullable BeanFactory beanFactory, Class<T> factoryType) {
        ApplicationContext context = asApplicationContext(beanFactory);
        if (context != null) {
            return loadFactories(context, factoryType);
        }

        ConfigurableBeanFactory configurableBeanFactory = asConfigurableBeanFactory(beanFactory);

        ClassLoader classLoader = nullSafeBeanClassLoader(configurableBeanFactory);
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
        boolean matched = false;
        for (Constructor constructor : constructors) {
            Class<?>[] parameterTypes = constructor.getParameterTypes();
            int parameterCount = parameterTypes.length;

            if (parameterCount != argsLength) {
                continue;
            }

            for (int i = 0; i < argsLength; i++) {
                Class<?> parameterType = parameterTypes[i];
                Object arg = args[i];
                if (parameterType.isInstance(arg)) {
                    matched = true;
                    break;
                }
            }

            if (matched) {
                targetConstructor = constructor;
                break;
            }
        }

        if (targetConstructor == null) {
            throw new IllegalArgumentException(format("No Constructor of Factory class[name : %s] was found for arguments : %s",
                    factoryImplClass.getName(), arrayToCommaDelimitedString(args)));
        }

        return targetConstructor;
    }

    private SpringFactoriesLoaderUtils() {
    }
}