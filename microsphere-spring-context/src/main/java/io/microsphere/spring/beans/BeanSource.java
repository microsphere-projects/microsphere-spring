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

package io.microsphere.spring.beans;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.beans.factory.BeanFactoryUtils;
import io.microsphere.spring.beans.factory.support.BeanRegistrar;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Collection;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.getBeanClass;
import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerGenericBeans;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactoryClasses;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ObjectUtils.defaultIfNull;
import static io.microsphere.util.ServiceLoaderUtils.getServiceClasses;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;

/**
 * The enumeration of Bean Sources
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanFactory
 * @see SpringFactoriesLoader
 * @see ServiceLoader
 * @since 1.0.0
 */
public enum BeanSource {

    /**
     * Bean from {@link BeanFactory Bean Factory}.
     *
     * @see BeanFactory
     * @see ConfigurableListableBeanFactory
     */
    BEAN_FACTORY {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return BeanFactoryUtils.getBeanTypes(beanFactory, beanType, true, false);
        }

        @Override
        Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                            Set<Class<?>> beanClasses) {
            // Only to query, do not register, since the beans are already defined in Bean Factory
            Map<Class<?>, String> beanTypesAndNames = newHashMap(beanClasses.size());
            for (Class<?> beanClass : beanClasses) {
                String[] beanNames = beanFactory.getBeanNamesForType(beanClass, true, false);
                for (String beanName : beanNames) {
                    Class<?> beanType = getBeanClass(beanFactory, beanName);
                    beanType = defaultIfNull(beanType, beanClass);
                    beanTypesAndNames.put(beanType, beanName);
                }
            }
            return beanTypesAndNames;
        }
    },

    /**
     * Bean from {@link SpringFactoriesLoader Spring Factories},
     * the given type from {@value SpringFactoriesLoader#FACTORIES_RESOURCE_LOCATION} files.
     *
     * @see SpringFactoriesLoader
     */
    SPRING_FACTORIES {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return loadFactoryClasses(beanType, beanFactory.getBeanClassLoader());
        }
    },

    /**
     * Bean from {@link ServiceLoader Java Service Provider},
     * the given type from {@code META-INF/services} files.
     *
     * @see ServiceLoader
     */
    JAVA_SERVICE_PROVIDER {
        @Override
        public <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType) {
            return getServiceClasses(beanType, beanFactory.getBeanClassLoader());
        }
    };

    /**
     * Gets the set of bean types that are assignable to the given {@code beanType} from this source.
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to use for resolution
     * @param beanType    the target bean type to search for
     * @param <T>         the type of the bean
     * @return a {@link Set} of {@link Class} objects representing the bean types found, never {@code null}
     */
    @Nonnull
    @Immutable
    public abstract <T> Set<Class<T>> getBeanTypes(ConfigurableListableBeanFactory beanFactory, Class<T> beanType);

    /**
     * Registers beans into the given {@link BeanDefinitionRegistry} from this source.
     * <p>
     * This method discovers bean classes for the specified {@code beanTypes} using this {@link BeanSource},
     * and registers them as generic beans. The underlying {@link ConfigurableListableBeanFactory} is derived
     * from the provided registry for type resolution if necessary.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories source
     * BeanDefinitionRegistry registry = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.SPRING_FACTORIES.registerBeans(
     *     registry,
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param registry  the {@link BeanDefinitionRegistry} to register beans into
     * @param beanTypes the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @see #registerBeans(ConfigurableListableBeanFactory, BeanDefinitionRegistry, Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    @Nonnull
    @Immutable
    public Map<Class<?>, String> registerBeans(BeanDefinitionRegistry registry, Class<?>... beanTypes) {
        return registerBeans(asConfigurableListableBeanFactory(registry), registry, beanTypes);
    }

    /**
     * Registers beans into the {@link BeanDefinitionRegistry} derived from the given {@link ConfigurableListableBeanFactory}
     * from this source.
     * <p>
     * This method discovers bean classes for the specified {@code beanTypes} using this {@link BeanSource},
     * and registers them as generic beans. The underlying {@link BeanDefinitionRegistry} is derived
     * from the provided {@link ConfigurableListableBeanFactory}.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories source
     * ConfigurableListableBeanFactory beanFactory = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.SPRING_FACTORIES.registerBeans(
     *     beanFactory,
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to use for resolution and to derive the registry
     * @param beanTypes   the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @see #registerBeans(ConfigurableListableBeanFactory, BeanDefinitionRegistry, Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    @Nonnull
    @Immutable
    public Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, Class<?>... beanTypes) {
        return registerBeans(beanFactory, asBeanDefinitionRegistry(beanFactory), beanTypes);
    }

    /**
     * Registers beans into the given {@link BeanDefinitionRegistry} from this source.
     * <p>
     * This method discovers bean classes for the specified {@code beanTypes} using this {@link BeanSource},
     * and registers them as generic beans.
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories source
     * ConfigurableListableBeanFactory beanFactory = ...;
     * BeanDefinitionRegistry registry = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.SPRING_FACTORIES.registerBeans(
     *     beanFactory,
     *     registry,
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to use for resolution
     * @param registry    the {@link BeanDefinitionRegistry} to register beans into
     * @param beanTypes   the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @see #registerBeans(ConfigurableListableBeanFactory, Class...)
     * @see #registerBeans(BeanDefinitionRegistry, Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    @Nonnull
    @Immutable
    public Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                               Class<?>... beanTypes) {
        int length = length(beanTypes);
        if (length == 0) {
            return emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = registerBeans(beanFactory, registry, beanTypes, length);
        return unmodifiableMap(beanTypesAndNames);
    }

    Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                        Class<?>[] beanTypes, int length) {
        Map<Class<?>, String> beanTypesAndNames = newHashMap(length * 2);
        for (int i = 0; i < length; i++) {
            Class<?> beanType = beanTypes[i];
            beanTypesAndNames.putAll(registerBean(beanFactory, registry, beanType));
        }
        return beanTypesAndNames;
    }

    Map<Class<?>, String> registerBean(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                       Class<?> beanType) {
        Set<Class<?>> beanClasses = (Set) getBeanTypes(beanFactory, beanType);
        return registerBeans(beanFactory, registry, beanClasses);
    }

    Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                        Set<Class<?>> beanClasses) {
        return registerGenericBeans(registry, beanClasses);
    }

    /**
     * Registers beans into the {@link BeanDefinitionRegistry} derived from the given {@link ConfigurableListableBeanFactory}
     * using the specified {@link BeanSource}s.
     * <p>
     * This method iterates over the provided {@code beanSources}, retrieves the bean classes for each {@code beanType}
     * from each source, and registers them as generic beans. If multiple sources provide beans for the same type,
     * the behavior depends on the underlying {@link BeanDefinitionRegistry} implementation (typically, later registrations
     * may override earlier ones if the bean name conflicts).
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories and Java Service Provider sources
     * ConfigurableListableBeanFactory beanFactory = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.registerBeans(
     *     beanFactory,
     *     new BeanSource[]{BeanSource.SPRING_FACTORIES, BeanSource.JAVA_SERVICE_PROVIDER},
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to use for resolution and to derive the registry
     * @param beanSources the array of {@link BeanSource}s to use for discovering bean classes
     * @param beanTypes   the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @throws IllegalArgumentException if {@code beanSources} is {@code null} or empty
     * @see #registerBeans(BeanDefinitionRegistry, BeanSource[], Class...)
     * @see #registerBeans(ConfigurableListableBeanFactory, BeanDefinitionRegistry, BeanSource[], Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    @Nonnull
    @Immutable
    public static Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanSource[] beanSources, Class<?>... beanTypes) {
        return registerBeans(beanFactory, asBeanDefinitionRegistry(beanFactory), beanSources, beanTypes);
    }

    /**
     * Registers beans into the given {@link BeanDefinitionRegistry} from the specified {@link BeanSource}s.
     * <p>
     * This method iterates over the provided {@code beanSources}, retrieves the bean classes for each {@code beanType}
     * from each source, and registers them as generic beans. If multiple sources provide beans for the same type,
     * the behavior depends on the underlying {@link BeanDefinitionRegistry} implementation (typically, later registrations
     * may override earlier ones if the bean name conflicts).
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories and Java Service Provider sources
     * BeanDefinitionRegistry registry = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.registerBeans(
     *     registry,
     *     new BeanSource[]{BeanSource.SPRING_FACTORIES, BeanSource.JAVA_SERVICE_PROVIDER},
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param registry    the {@link BeanDefinitionRegistry} to register beans into
     * @param beanSources the array of {@link BeanSource}s to use for discovering bean classes
     * @param beanTypes   the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @throws IllegalArgumentException if {@code beanSources} is {@code null} or empty
     * @see #registerBeans(ConfigurableListableBeanFactory, BeanSource[], Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    @Nonnull
    @Immutable
    public static Map<Class<?>, String> registerBeans(BeanDefinitionRegistry registry, BeanSource[] beanSources, Class<?>... beanTypes) {
        return registerBeans(asConfigurableListableBeanFactory(registry), registry, beanSources, beanTypes);
    }

    /**
     * Registers beans into the given {@link BeanDefinitionRegistry} from the specified {@link BeanSource}s.
     * <p>
     * This method iterates over the provided {@code beanSources}, retrieves the bean classes for each {@code beanType}
     * from each source, and registers them as generic beans. If multiple sources provide beans for the same type,
     * the behavior depends on the underlying {@link BeanDefinitionRegistry} implementation (typically, later registrations
     * may override earlier ones if the bean name conflicts).
     *
     * <h3>Example Usage:</h3>
     * <pre>{@code
     * // Register beans from Spring Factories and Java Service Provider sources
     * ConfigurableListableBeanFactory beanFactory = ...;
     * BeanDefinitionRegistry registry = ...;
     *
     * Map<Class<?>, String> registeredBeans = BeanSource.registerBeans(
     *     beanFactory,
     *     registry,
     *     new BeanSource[]{BeanSource.SPRING_FACTORIES, BeanSource.JAVA_SERVICE_PROVIDER},
     *     MyService.class,
     *     AnotherService.class
     * );
     *
     * // The map contains the registered bean classes as keys and their bean names as values
     * registeredBeans.forEach((beanClass, beanName) -> {
     *     System.out.println("Registered: " + beanClass.getName() + " with name: " + beanName);
     * });
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to use for resolution and bean class detection
     * @param registry    the {@link BeanDefinitionRegistry} to register beans into
     * @param beanSources the array of {@link BeanSource}s to use for discovering bean classes
     * @param beanTypes   the target bean types to search for and register
     * @return an unmodifiable {@link Map} where keys are the registered bean classes and values are their corresponding bean names
     * @throws IllegalArgumentException if {@code beanSources} is {@code null} or empty
     * @see #registerBeans(ConfigurableListableBeanFactory, BeanSource[], Class...)
     * @see BeanRegistrar#registerGenericBeans(BeanDefinitionRegistry, Collection)
     */
    public static Map<Class<?>, String> registerBeans(ConfigurableListableBeanFactory beanFactory, BeanDefinitionRegistry registry,
                                                      BeanSource[] beanSources, Class<?>... beanTypes) {
        int length = length(beanTypes);
        if (length == 0) {
            return emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = newHashMap(length);
        for (BeanSource beanSource : beanSources) {
            beanTypesAndNames.putAll(beanSource.registerBeans(beanFactory, registry, beanTypes));
        }
        return unmodifiableMap(beanTypesAndNames);
    }
}