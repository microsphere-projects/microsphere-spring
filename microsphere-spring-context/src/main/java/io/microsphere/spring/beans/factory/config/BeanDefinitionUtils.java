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
package io.microsphere.spring.beans.factory.config;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.util.Utils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanDefinition} Utilities class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractBeanDefinition
 * @see BeanDefinition#ROLE_APPLICATION
 * @see BeanDefinition#ROLE_INFRASTRUCTURE
 * @since 1.0.0
 */
public abstract class BeanDefinitionUtils implements Utils {

    private static final Logger logger = getLogger(BeanDefinitionUtils.class);

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the given bean type.
     *
     * <p>This method is a convenience wrapper that calls
     * {@link #genericBeanDefinition(Class, int, Object[])} with default role
     * ({@link BeanDefinition#ROLE_APPLICATION}) and no constructor arguments.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType);
     * }</pre>
     *
     * @param beanType the type of bean
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType) {
        return genericBeanDefinition(beanType, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition}
     *
     * <p>This method creates a bean definition for the specified bean type with optional constructor arguments.
     * It internally uses {@link BeanDefinitionBuilder} to construct the bean definition and sets the provided
     * constructor arguments via constructor injection.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * Object[] constructorArgs = new Object[]{"arg1", 123};
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, constructorArgs);
     * }</pre>
     *
     * <p>The above example will create a bean definition for the class {@code MyService}, passing in two constructor arguments.
     *
     * @param beanType             the type of bean
     * @param constructorArguments the arguments of Bean Classes' constructor
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, Object... constructorArguments) {
        return genericBeanDefinition(beanType, ROLE_APPLICATION, constructorArguments);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the specified bean type and role.
     *
     * <p>This method is a convenience wrapper that calls
     * {@link #genericBeanDefinition(Class, int, Object[])} with no constructor arguments.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * int role = BeanDefinition.ROLE_APPLICATION;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
     * }</pre>
     *
     * @param beanType the type of bean
     * @param role     the role of the bean definition (e.g., application or infrastructure)
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role) {
        return genericBeanDefinition(beanType, role, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Build a generic instance of {@link AbstractBeanDefinition} with the specified bean type, role, and constructor arguments.
     *
     * <p>This method uses {@link BeanDefinitionBuilder} to construct a bean definition for the given bean type,
     * sets its role, and injects any provided constructor arguments via constructor injection.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Bean Definition</h4>
     * <pre>{@code
     * Class<?> beanType = MyService.class;
     * int role = BeanDefinition.ROLE_APPLICATION;
     * Object[] constructorArgs = new Object[]{"arg1", 123};
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role, constructorArgs);
     * }</pre>
     *
     * <p>The above example creates a bean definition for the class {@code MyService}, assigning it the role of an application bean,
     * and passing in two constructor arguments.
     *
     * <h4>No Constructor Arguments</h4>
     * <pre>{@code
     * Class<?> beanType = MyRepository.class;
     * int role = BeanDefinition.ROLE_INFRASTRUCTURE;
     * AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
     * }</pre>
     *
     * <p>In this case, no constructor arguments are provided, so the default constructor will be used.
     *
     * @param beanType             the type of bean to define
     * @param role                 the role of the bean definition (e.g., application or infrastructure)
     * @param constructorArguments the arguments to pass to the bean's constructor, if any
     * @return an instance of {@link AbstractBeanDefinition}
     */
    @Nonnull
    public static AbstractBeanDefinition genericBeanDefinition(Class<?> beanType, int role, Object[] constructorArguments) {
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(beanType)
                .setRole(role);
        // Add the arguments of constructor if present
        int length = length(constructorArguments);
        for (int i = 0; i < length; i++) {
            Object constructorArgument = constructorArguments[i];
            beanDefinitionBuilder.addConstructorArgValue(constructorArgument);
        }
        AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
        return beanDefinition;
    }

    /**
     * Resolves the bean type from the given {@link RootBeanDefinition} using the default class loader.
     *
     * <p>This method attempts to resolve the bean type via its {@link ResolvableType}. If that fails,
     * it falls back to resolving the bean class using the bean's class name and the default class loader.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * RootBeanDefinition beanDefinition = new RootBeanDefinition();
     * Class<?> beanType = resolveBeanType(beanDefinition);
     * }</pre>
     *
     * @param beanDefinition the {@link RootBeanDefinition} to resolve the bean type from
     * @return the resolved bean class, or {@code null} if it cannot be resolved
     */
    @Nullable
    public static Class<?> resolveBeanType(RootBeanDefinition beanDefinition) {
        return resolveBeanType(beanDefinition, getDefaultClassLoader());
    }

    /**
     * Resolves the bean type from the given {@link RootBeanDefinition} using the specified class loader.
     *
     * <p>This method attempts to resolve the bean type via its {@link ResolvableType}. If that fails,
     * it falls back to resolving the bean class using the bean's class name and the provided class loader.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Resolution</h4>
     * <pre>{@code
     * RootBeanDefinition beanDefinition = new RootBeanDefinition();
     * ClassLoader classLoader = getClass().getClassLoader();
     * Class<?> beanType = resolveBeanType(beanDefinition, classLoader);
     * }</pre>
     *
     * <h4>Resolution with Null Class Loader</h4>
     * <pre>{@code
     * RootBeanDefinition beanDefinition = new RootBeanDefinition();
     * Class<?> beanType = resolveBeanType(beanDefinition, null); // Uses default class loader internally
     * }</pre>
     *
     * @param beanDefinition the {@link RootBeanDefinition} to resolve the bean type from
     * @param classLoader    the class loader to use for resolving the bean class, may be {@code null}
     * @return the resolved bean class, or {@code null} if it cannot be resolved
     */
    @Nullable
    public static Class<?> resolveBeanType(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        ResolvableType resolvableType = beanDefinition.getResolvableType();
        Class<?> beanClass = resolvableType.resolve();
        if (beanClass == null) { // resolving the bean class as fallback
            String beanClassName = beanDefinition.getBeanClassName();
            beanClass = resolveClass(beanClassName, classLoader);
        }
        return beanClass;
    }

    /**
     * Find the names of all infrastructure beans in the given bean factory.
     *
     * <p>An infrastructure bean is typically a bean with the role
     * {@link BeanDefinition#ROLE_INFRASTRUCTURE}. These beans are usually not intended for direct use by application code.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Usage</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = ...; // Obtain from ApplicationContext
     * Set<String> infrastructureBeanNames = findInfrastructureBeanNames(beanFactory);
     * for (String beanName : infrastructureBeanNames) {
     *     System.out.println("Infrastructure Bean: " + beanName);
     * }
     * }</pre>
     *
     * <h4>Integration with Spring Context</h4>
     * <pre>{@code
     * ApplicationContext context = new AnnotationConfigApplicationContext(MyConfiguration.class);
     * ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     * Set<String> infrastructureBeans = findInfrastructureBeanNames(beanFactory);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to search for infrastructure beans
     * @return an unmodifiable set of names of infrastructure beans
     */
    @Nonnull
    public static Set<String> findInfrastructureBeanNames(ConfigurableListableBeanFactory beanFactory) {
        return findBeanNames(beanFactory, BeanDefinitionUtils::isInfrastructureBean);
    }

    /**
     * Find bean names that match the given predicate(s) in the provided {@link ConfigurableListableBeanFactory}.
     *
     * <p>This method combines multiple predicates using logical AND and tests each bean definition
     * against the combined predicate. If a bean definition matches, its name is added to the result set.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Filtering</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = ...; // Obtain from ApplicationContext
     * Set<String> beanNames = findBeanNames(beanFactory, bd -> bd.getRole() == BeanDefinition.ROLE_APPLICATION);
     * for (String name : beanNames) {
     *     System.out.println("Application Bean: " + name);
     * }
     * }</pre>
     *
     * <h4>Combining Predicates</h4>
     * <pre>{@code
     * Predicate<BeanDefinition> isInfrastructure = bd -> bd.getRole() == BeanDefinition.ROLE_INFRASTRUCTURE;
     * Predicate<BeanDefinition> hasAInName = bd -> bd.getBeanClassName() != null && bd.getBeanClassName().contains("A");
     *
     * Set<String> beanNames = findBeanNames(beanFactory, isInfrastructure, hasAInName);
     * }</pre>
     *
     * @param beanFactory the {@link ConfigurableListableBeanFactory} to search beans in
     * @param predicates  one or more predicates used to filter bean definitions
     * @return an unmodifiable set of bean names that match all given predicates
     */
    @Nonnull
    public static Set<String> findBeanNames(@Nullable ConfigurableListableBeanFactory beanFactory, Predicate<? super BeanDefinition>... predicates) {
        if (beanFactory == null) {
            return emptySet();
        }
        Predicate<? super BeanDefinition> predicate = and(predicates);
        Set<String> matchedBeanNames = new LinkedHashSet<>();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            if (predicate.test(beanDefinition)) {
                matchedBeanNames.add(beanDefinitionName);
            }
        }
        return unmodifiableSet(matchedBeanNames);
    }

    /**
     * Determine whether the given bean definition represents an infrastructure bean.
     *
     * <p>An infrastructure bean is defined by having the role
     * {@link BeanDefinition#ROLE_INFRASTRUCTURE}. These beans are typically internal to the framework
     * or libraries and are not intended for direct use by application code.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Check</h4>
     * <pre>{@code
     * BeanDefinition beanDefinition = ...; // Obtain from bean factory
     * if (BeanDefinitionUtils.isInfrastructureBean(beanDefinition)) {
     *     System.out.println("This is an infrastructure bean.");
     * } else {
     *     System.out.println("This is not an infrastructure bean.");
     * }
     * }</pre>
     *
     * <h4>Filtering Infrastructure Beans</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     * Set<String> infrastructureBeans = BeanDefinitionUtils.findBeanNames(beanFactory,
     *     BeanDefinitionUtils::isInfrastructureBean);
     * }</pre>
     *
     * @param beanDefinition the bean definition to check, may be {@code null}
     * @return {@code true} if the bean definition is not null and has the role
     * {@link BeanDefinition#ROLE_INFRASTRUCTURE}, otherwise {@code false}
     */
    public static boolean isInfrastructureBean(@Nullable BeanDefinition beanDefinition) {
        return beanDefinition != null && ROLE_INFRASTRUCTURE == beanDefinition.getRole();
    }
}
