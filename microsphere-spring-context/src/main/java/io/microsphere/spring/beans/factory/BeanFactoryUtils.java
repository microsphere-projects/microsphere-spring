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
package io.microsphere.spring.beans.factory;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.HierarchicalBeanFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.reflect.FieldUtils.getFieldValue;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.ArrayUtils.size;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableList;
import static org.springframework.beans.factory.BeanFactoryUtils.beanNamesForTypeIncludingAncestors;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.ObjectUtils.containsElement;
import static org.springframework.util.StringUtils.hasText;

/**
 * {@link BeanFactory} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class BeanFactoryUtils implements Utils {

    /**
     * Retrieves a bean of the specified type and name from the given {@link ListableBeanFactory} if it exists.
     * <p>
     * This method checks whether the provided bean name is valid and then attempts to retrieve the corresponding bean.
     * If no such bean exists or the name is invalid, this method returns <code>null</code>.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ListableBeanFactory beanFactory = ...; // Obtain the bean factory
     * String beanName = "myBean";
     * Class<MyBeanType> beanType = MyBeanType.class;
     *
     * MyBeanType myBean = BeanFactoryUtils.getOptionalBean(beanFactory, beanName, beanType);
     * if (myBean != null) {
     *     // Use the bean
     * } else {
     *     // Handle absence of the bean
     * }
     * }</pre>
     *
     * @param <T>         The type of the bean to retrieve.
     * @param beanFactory The target bean factory to retrieve the bean from. Must not be <code>null</code>.
     * @param beanName    The name of the bean to retrieve. If empty or blank, this method will return <code>null</code>.
     * @param beanType    The type of the bean to retrieve. Must not be <code>null</code>.
     * @return The bean instance if present in the bean factory; otherwise, <code>null</code>.
     */
    @Nullable
    public static <T> T getOptionalBean(ListableBeanFactory beanFactory, String beanName, Class<T> beanType) {
        if (!hasText(beanName)) {
            return null;
        }
        String[] beanNames = ofArray(beanName);
        List<T> beans = getBeans(beanFactory, beanNames, beanType);
        return isEmpty(beans) ? null : beans.get(0);
    }

    /**
     * Retrieves beans of the specified type that match the given bean names from the {@link ListableBeanFactory}.
     *
     * <p>This method filters and returns only those beans whose names are present in the provided array of bean names.
     * It ensures that all beans returned are of the specified type. If no matching beans are found, an empty list is returned.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ListableBeanFactory beanFactory = ...; // Obtain or inject the bean factory
     * String[] beanNames = {"bean1", "bean2"};
     * Class<MyBeanType> beanType = MyBeanType.class;
     *
     * List<MyBeanType> beans = BeanFactoryUtils.getBeans(beanFactory, beanNames, beanType);
     *
     * if (!beans.isEmpty()) {
     *     for (MyBeanType bean : beans) {
     *         // Use each bean instance
     *     }
     * } else {
     *     // Handle case where no beans were found
     * }
     * }</pre>
     *
     * @param <T>         The type of the beans to retrieve.
     * @param beanFactory The target bean factory to retrieve beans from. Must not be {@code null}.
     * @param beanNames   An array of bean names to filter by. If empty or null, this method will return an empty list.
     * @param beanType    The type of the beans to retrieve. Must not be {@code null}.
     * @return A read-only, non-null list of bean instances that match the specified names and type. Returns an empty list if no matches are found.
     */
    @Nonnull
    public static <T> List<T> getBeans(ListableBeanFactory beanFactory, String[] beanNames, Class<T> beanType) {
        int size = size(beanNames);
        if (size < 1) {
            return emptyList();
        }

        String[] allBeanNames = beanNamesForTypeIncludingAncestors(beanFactory, beanType, true, false);
        List<T> beans = new ArrayList<T>(size);
        for (int i = 0; i < size; i++) {
            String beanName = beanNames[i];
            if (containsElement(allBeanNames, beanName)) {
                beans.add(beanFactory.getBean(beanName, beanType));
            }
        }
        return unmodifiableList(beans);
    }

    /**
     * Checks whether the provided object is an instance of {@link DefaultListableBeanFactory}.
     *
     * <p>This method is useful when you need to verify if a given bean factory supports bean definition
     * registration and listing capabilities, typically available in a standard Spring container setup.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isDefaultListableBeanFactory(beanFactory)) {
     *     DefaultListableBeanFactory dlbf = BeanFactoryUtils.asDefaultListableBeanFactory(beanFactory);
     *     // Use dlbf for advanced operations like registering new bean definitions
     * } else {
     *     // Handle case where bean factory is not a DefaultListableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to check. May be {@code null} or any type.
     * @return <code>true</code> if the object is an instance of {@link DefaultListableBeanFactory};
     * <code>false</code> otherwise.
     */
    public static boolean isDefaultListableBeanFactory(Object beanFactory) {
        return beanFactory instanceof DefaultListableBeanFactory;
    }

    /**
     * Checks whether the provided object is an instance of {@link BeanDefinitionRegistry}.
     *
     * <p>
     * This method is useful when you need to verify if a given bean factory supports dynamic registration
     * of bean definitions, which is typically required during advanced configuration or post-processing phases.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isBeanDefinitionRegistry(beanFactory)) {
     *     BeanDefinitionRegistry registry = BeanFactoryUtils.asBeanDefinitionRegistry(beanFactory);
     *     // Register or manipulate bean definitions as needed
     * } else {
     *     // Handle case where bean factory does not support BeanDefinitionRegistry
     * }
     * }</pre>
     *
     * @param beanFactory The object to check. May be {@code null} or any type.
     * @return <code>true</code> if the object is an instance of {@link BeanDefinitionRegistry};
     * <code>false</code> otherwise.
     */
    public static boolean isBeanDefinitionRegistry(Object beanFactory) {
        return beanFactory instanceof BeanDefinitionRegistry;
    }

    /**
     * Converts the given {@link Object} into an instance of {@link BeanDefinitionRegistry}.
     *
     * <p>
     * This method is typically used when you need to perform operations that require a bean definition registry,
     * such as registering or modifying bean definitions in advanced configuration scenarios.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isBeanDefinitionRegistry(beanFactory)) {
     *     BeanDefinitionRegistry registry = BeanFactoryUtils.asBeanDefinitionRegistry(beanFactory);
     *     // Register or manipulate bean definitions as needed
     *     registry.registerBeanDefinition("myBean", myBeanDefinition);
     * } else {
     *     // Handle case where bean factory does not support BeanDefinitionRegistry
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link BeanDefinitionRegistry}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link BeanDefinitionRegistry}
     *                                  and cannot be converted.
     */
    @Nullable
    public static BeanDefinitionRegistry asBeanDefinitionRegistry(Object beanFactory) {
        return cast(beanFactory, BeanDefinitionRegistry.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link ListableBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to a listable bean factory,
     * such as retrieving beans by type or getting bean names. If the provided object is a Spring
     * {@link ApplicationContext}, it will be adapted to its underlying autowire-capable bean factory.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isListableBeanFactory(beanFactory)) {
     *     ListableBeanFactory lbf = BeanFactoryUtils.asListableBeanFactory(beanFactory);
     *     String[] beanNames = lbf.getBeanDefinitionNames();
     *     for (String beanName : beanNames) {
     *         // Process each bean name
     *     }
     * } else {
     *     // Handle case where bean factory does not support ListableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link ListableBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link ListableBeanFactory}
     *                                  and cannot be converted.
     */
    @Nullable
    public static ListableBeanFactory asListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ListableBeanFactory.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link HierarchicalBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to a hierarchical bean factory,
     * such as accessing parent bean factories or managing bean definitions in a hierarchical structure.
     * If the provided object is a Spring {@link ApplicationContext}, it will be adapted to its underlying
     * autowire-capable bean factory before conversion.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isHierarchicalBeanFactory(beanFactory)) {
     *     HierarchicalBeanFactory hbf = BeanFactoryUtils.asHierarchicalBeanFactory(beanFactory);
     *     BeanFactory parentFactory = hbf.getParentBeanFactory();
     *     // Use the hierarchical bean factory and its parent if available
     * } else {
     *     // Handle case where bean factory does not support HierarchicalBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link HierarchicalBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link HierarchicalBeanFactory}
     *                                  and cannot be converted.
     */
    @Nullable
    public static HierarchicalBeanFactory asHierarchicalBeanFactory(Object beanFactory) {
        return cast(beanFactory, HierarchicalBeanFactory.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link ConfigurableBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to a configurable bean factory,
     * such as modifying bean definitions or customizing the configuration process. If the provided object is a Spring
     * {@link ApplicationContext}, it will be adapted to its underlying autowire-capable bean factory before conversion.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isConfigurableBeanFactory(beanFactory)) {
     *     ConfigurableBeanFactory cbf = BeanFactoryUtils.asConfigurableBeanFactory(beanFactory);
     *     // Customize the bean factory configuration as needed
     *     cbf.setAllowBeanDefinitionOverriding(true);
     * } else {
     *     // Handle case where bean factory does not support ConfigurableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link ConfigurableBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link ConfigurableBeanFactory}
     *                                  and cannot be converted.
     */
    @Nullable
    public static ConfigurableBeanFactory asConfigurableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableBeanFactory.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link AutowireCapableBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to an autowire-capable bean factory,
     * such as autowiring beans or managing bean definitions in advanced configuration scenarios.
     * If the provided object is a Spring {@link ApplicationContext}, it will be adapted to its underlying
     * autowire-capable bean factory before conversion.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isAutowireCapableBeanFactory(beanFactory)) {
     *     AutowireCapableBeanFactory acbf = BeanFactoryUtils.asAutowireCapableBeanFactory(beanFactory);
     *     // Use the autowire-capable bean factory for autowiring or bean creation
     *     MyBean myBean = acbf.createBean(MyBean.class);
     * } else {
     *     // Handle case where bean factory does not support AutowireCapableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link AutowireCapableBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link AutowireCapableBeanFactory}
     *                                  and cannot be converted.
     */
    @Nullable
    public static AutowireCapableBeanFactory asAutowireCapableBeanFactory(Object beanFactory) {
        return cast(beanFactory, AutowireCapableBeanFactory.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link ConfigurableListableBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to a configurable listable bean factory,
     * such as retrieving beans by type or managing bean definitions with configurability and listability.
     * If the provided object is a Spring {@link ApplicationContext}, it will be adapted to its underlying
     * autowire-capable bean factory before conversion.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isConfigurableListableBeanFactory(beanFactory)) {
     *     ConfigurableListableBeanFactory clbf = BeanFactoryUtils.asConfigurableListableBeanFactory(beanFactory);
     *     // Retrieve beans by type or configure bean definitions
     *     MyBean myBean = clbf.getBean(MyBean.class);
     * } else {
     *     // Handle case where bean factory does not support ConfigurableListableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link ConfigurableListableBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of {@link ConfigurableListableBeanFactory}
     *                                  and cannot be converted.
     */
    @Nullable
    public static ConfigurableListableBeanFactory asConfigurableListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, ConfigurableListableBeanFactory.class);
    }

    /**
     * Converts the given {@link Object} into an instance of {@link DefaultListableBeanFactory}.
     *
     * <p>
     * This method is typically used when you need to perform operations specific to a
     * {@link DefaultListableBeanFactory}, such as registering or retrieving bean definitions.
     * If the provided object is a Spring {@link ApplicationContext}, it will be adapted to its
     * underlying autowire-capable bean factory before conversion.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Object beanFactory = ...; // Obtain or inject a bean factory
     *
     * if (BeanFactoryUtils.isDefaultListableBeanFactory(beanFactory)) {
     *     DefaultListableBeanFactory dlbf = BeanFactoryUtils.asDefaultListableBeanFactory(beanFactory);
     *     // Register or manipulate bean definitions as needed
     *     dlbf.registerBeanDefinition("myBean", myBeanDefinition);
     * } else {
     *     // Handle case where bean factory is not a DefaultListableBeanFactory
     * }
     * }</pre>
     *
     * @param beanFactory The object to convert. May be {@code null} or any type.
     * @return An instance of {@link DefaultListableBeanFactory}, or {@code null} if the input is {@code null}.
     * @throws IllegalArgumentException if the provided object is not an instance of
     *                                  {@link DefaultListableBeanFactory} and cannot be converted.
     */
    @Nullable
    public static DefaultListableBeanFactory asDefaultListableBeanFactory(Object beanFactory) {
        return cast(beanFactory, DefaultListableBeanFactory.class);
    }

    /**
     * Retrieves the set of resolvable dependency types that have been registered with the given {@link ConfigurableListableBeanFactory}.
     *
     * <p>
     * This method provides access to the types that the bean factory has been configured to resolve automatically when needed during bean creation.
     * It is useful for inspecting or debugging which dependencies are available for autowiring resolution.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = ...; // Obtain or inject the bean factory
     *
     * Set<Class<?>> resolvableDependencies = BeanFactoryUtils.getResolvableDependencyTypes(beanFactory);
     *
     * if (!resolvableDependencies.isEmpty()) {
     *     for (Class<?> dependencyType : resolvableDependencies) {
     *         System.out.println("Registered resolvable dependency type: " + dependencyType.getName());
     *     }
     * } else {
     *     System.out.println("No resolvable dependency types found.");
     * }
     * }</pre>
     *
     * @param beanFactory The target bean factory from which to retrieve the resolvable dependency types. Must not be {@code null}.
     * @return A non-null, read-only set of resolvable dependency types. Returns an empty set if no resolvable dependencies are registered.
     */
    @Nonnull
    public static Set<Class<?>> getResolvableDependencyTypes(ConfigurableListableBeanFactory beanFactory) {
        return getResolvableDependencyTypes(asDefaultListableBeanFactory(beanFactory));
    }

    /**
     * Retrieves the set of resolvable dependency types that have been registered with the given {@link DefaultListableBeanFactory}.
     *
     * <p>
     * This method accesses the internal registry of resolvable dependencies maintained by the bean factory.
     * These are typically used during autowiring to resolve and inject dependencies that are not directly defined as beans.
     * If no resolvable dependencies are registered, an empty set is returned.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * DefaultListableBeanFactory beanFactory = ...; // Obtain or create a bean factory instance
     *
     * Set<Class<?>> resolvableDependencies = BeanFactoryUtils.getResolvableDependencyTypes(beanFactory);
     *
     * if (!resolvableDependencies.isEmpty()) {
     *     for (Class<?> dependencyType : resolvableDependencies) {
     *         System.out.println("Registered resolvable dependency: " + dependencyType.getName());
     *     }
     * } else {
     *     System.out.println("No resolvable dependencies found.");
     * }
     * }</pre>
     *
     * @param beanFactory The target bean factory from which to retrieve the resolvable dependency types. Must not be {@code null}.
     * @return A non-null, read-only set of resolvable dependency types. Returns an empty set if no resolvable dependencies are registered.
     */
    @Nonnull
    public static Set<Class<?>> getResolvableDependencyTypes(DefaultListableBeanFactory beanFactory) {
        Map resolvableDependencies = getFieldValue(beanFactory, "resolvableDependencies", Map.class);
        return resolvableDependencies == null ? emptySet() : resolvableDependencies.keySet();
    }

    /**
     * Retrieves all instances of {@link BeanPostProcessor} from the given {@link BeanFactory}.
     *
     * <p>
     * This method checks if the provided bean factory is an instance of {@link AbstractBeanFactory},
     * which maintains a list of registered bean post processors. If it is, this method returns an
     * unmodifiable list of those post processors. Otherwise, it returns an empty list.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * BeanFactory beanFactory = ...; // Obtain or inject the bean factory
     *
     * List<BeanPostProcessor> postProcessors = BeanFactoryUtils.getBeanPostProcessors(beanFactory);
     *
     * if (!postProcessors.isEmpty()) {
     *     for (BeanPostProcessor processor : postProcessors) {
     *         // Use or inspect each bean post processor
     *         System.out.println("Found BeanPostProcessor: " + processor.getClass().getName());
     *     }
     * } else {
     *     // Handle case where no bean post processors are available
     *     System.out.println("No BeanPostProcessors found.");
     * }
     * }</pre>
     *
     * @param beanFactory The target bean factory to retrieve bean post processors from. May be {@code null}.
     * @return A non-null, unmodifiable list of {@link BeanPostProcessor} instances if available;
     * otherwise, an empty list.
     */
    @Nonnull
    public static List<BeanPostProcessor> getBeanPostProcessors(@Nullable BeanFactory beanFactory) {
        final List<BeanPostProcessor> beanPostProcessors;
        if (beanFactory instanceof AbstractBeanFactory abf) {
            beanPostProcessors = unmodifiableList(abf.getBeanPostProcessors());
        } else {
            beanPostProcessors = emptyList();
        }
        return beanPostProcessors;
    }

    private static <T> T cast(@Nullable Object beanFactory, Class<T> extendedBeanFactoryType) {
        if (beanFactory == null) {
            return null;
        }
        if (beanFactory instanceof ApplicationContext context) {
            beanFactory = context.getAutowireCapableBeanFactory();
        }
        isInstanceOf(extendedBeanFactoryType, beanFactory,
                "The 'beanFactory' argument is not a instance of " + extendedBeanFactoryType +
                        ", is it running in Spring container?");
        return extendedBeanFactoryType.cast(beanFactory);
    }

    private BeanFactoryUtils() {
    }
}
