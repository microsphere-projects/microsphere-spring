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

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static io.microsphere.invoke.MethodHandleUtils.findVirtual;
import static io.microsphere.lang.function.Predicates.and;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.util.MethodHandleUtils.handleInvokeExactFailure;
import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.ClassLoaderUtils.getDefaultClassLoader;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableSet;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.core.ResolvableType.NONE;
import static org.springframework.core.ResolvableType.forClass;
import static org.springframework.core.ResolvableType.forMethodReturnType;

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
     * The name of getResolvableType() method.
     *
     * <ul>
     *     <li>{@link RootBeanDefinition#getResolvableType()} since Spring Framework 5.1</li>
     *     <li>{@link AbstractBeanDefinition#getResolvableType()} Spring Framework 5.2</li>
     * </ul>
     */
    private static final String GET_RESOLVABLE_TYPE_METHOD_NAME = "getResolvableType";

    /**
     * The method name of {@linkplain AbstractBeanDefinition#setInstanceSupplier(Supplier)}
     *
     * @since Spring Framework 5.0
     */
    private static final String SET_INSTANCE_SUPPLIER_METHOD_NAME = "setInstanceSupplier";

    /**
     * The method name of {@linkplain AbstractBeanDefinition#getInstanceSupplier()}
     *
     * @since Spring Framework 5.0
     */
    private static final String GET_INSTANCE_SUPPLIER_METHOD_NAME = "getInstanceSupplier";

    /**
     * The {@link MethodHandle} of {@linkplain RootBeanDefinition#getResolvableType()}
     *
     * @since Spring Framework 5.1
     */
    private static final MethodHandle GET_RESOLVABLE_TYPE_METHOD_HANDLE = findVirtual(RootBeanDefinition.class, GET_RESOLVABLE_TYPE_METHOD_NAME);

    /**
     * The {@link MethodHandle} of {@linkplain AbstractBeanDefinition#setInstanceSupplier(Supplier)}
     *
     * @since Spring Framework 5.0
     */
    private static final MethodHandle SET_INSTANCE_SUPPLIER_METHOD_HANDLE = findVirtual(AbstractBeanDefinition.class, SET_INSTANCE_SUPPLIER_METHOD_NAME, Supplier.class);

    /**
     * The {@link MethodHandle} of {@linkplain AbstractBeanDefinition#getInstanceSupplier()}
     *
     * @since Spring Framework 5.0
     */
    private static final MethodHandle GET_INSTANCE_SUPPLIER_METHOD_HANDLE = findVirtual(AbstractBeanDefinition.class, GET_INSTANCE_SUPPLIER_METHOD_NAME);

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
        ResolvableType resolvableType = getResolvableType(beanDefinition);
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

    /**
     * Determine whether the {@link AbstractBeanDefinition#setInstanceSupplier(Supplier)} method is present
     *
     * @return <code>true</code> if the {@link AbstractBeanDefinition#setInstanceSupplier(Supplier)} method is present,
     * <code>false</code> otherwise
     * @see #SET_INSTANCE_SUPPLIER_METHOD_HANDLE
     */
    public static boolean isSetInstanceSupplierMethodPresent() {
        return SET_INSTANCE_SUPPLIER_METHOD_HANDLE != null;
    }

    /**
     * Determine whether the {@link AbstractBeanDefinition#getInstanceSupplier()} method is present
     *
     * @return <code>true</code> if the {@link AbstractBeanDefinition#getInstanceSupplier()} method is present,
     * <code>false</code> otherwise
     * @see #GET_INSTANCE_SUPPLIER_METHOD_HANDLE
     */
    public static boolean isGetInstanceSupplierMethodPresent() {
        return GET_INSTANCE_SUPPLIER_METHOD_HANDLE != null;
    }

    /**
     * Determine whether the {@link AbstractBeanDefinition#getResolvableType()} method is present
     *
     * @return <code>true</code> if the {@link AbstractBeanDefinition#getResolvableType()} method is present,
     * <code>false</code> otherwise
     * @see #GET_RESOLVABLE_TYPE_METHOD_HANDLE
     */
    public static boolean isGetResolvableTypeMethodPresent() {
        return GET_RESOLVABLE_TYPE_METHOD_HANDLE != null;
    }

    /**
     * Obtain the {@link ResolvableType} from the given {@link AbstractBeanDefinition}.
     *
     * <p>This method attempts to retrieve the resolvable type directly if the bean definition
     * supports it (e.g., via a method handle). If not, it falls back to resolving based on the bean class.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Usage</h4>
     * <pre>{@code
     * AbstractBeanDefinition beanDefinition = ...; // Obtain from bean factory
     * ResolvableType resolvableType = getResolvableType(beanDefinition);
     * if (resolvableType != ResolvableType.NONE) {
     *     System.out.println("Resolvable Type: " + resolvableType);
     * } else {
     *     System.out.println("Could not resolve type");
     * }
     * }</pre>
     *
     * <h4>Integration with Spring Context</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     * String[] beanNames = beanFactory.getBeanDefinitionNames();
     * for (String beanName : beanNames) {
     *     BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
     *     if (beanDefinition instanceof AbstractBeanDefinition) {
     *         ResolvableType resolvableType = getResolvableType((AbstractBeanDefinition) beanDefinition);
     *         System.out.println("Bean: " + beanName + ", Resolvable Type: " + resolvableType);
     *     }
     * }
     * }</pre>
     *
     * @param beanDefinition the bean definition to resolve the type from, may be {@code null}
     * @return the resolved type, or {@link ResolvableType#NONE} if it cannot be resolved
     */
    @Nonnull
    public static ResolvableType getResolvableType(AbstractBeanDefinition beanDefinition) {
        if (beanDefinition instanceof RootBeanDefinition) {
            return getResolvableType((RootBeanDefinition) beanDefinition);
        }
        return doGetResolvableType(beanDefinition);
    }

    /**
     * Obtain the {@link ResolvableType} from the given {@link RootBeanDefinition}.
     *
     * <p>This method attempts to retrieve the resolvable type directly using a method handle if available.
     * If this fails or the method is not present, it falls back to resolving based on the bean class.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Usage</h4>
     * <pre>{@code
     * RootBeanDefinition rootBeanDefinition = ...; // Obtain from bean factory
     * ResolvableType resolvableType = getResolvableType(rootBeanDefinition);
     * if (resolvableType != ResolvableType.NONE) {
     *     System.out.println("Resolvable Type: " + resolvableType);
     * } else {
     *     System.out.println("Could not resolve type");
     * }
     * }</pre>
     *
     * <h4>Integration with Spring Context</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     * String[] beanNames = beanFactory.getBeanDefinitionNames();
     * for (String beanName : beanNames) {
     *     BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
     *     if (beanDefinition instanceof RootBeanDefinition) {
     *         ResolvableType resolvableType = getResolvableType((RootBeanDefinition) beanDefinition);
     *         System.out.println("Bean: " + beanName + ", Resolvable Type: " + resolvableType);
     *     }
     * }
     * }</pre>
     *
     * @param rootBeanDefinition the bean definition to resolve the type from, may be {@code null}
     * @return the resolved type, or {@link ResolvableType#NONE} if it cannot be resolved
     */
    @Nonnull
    public static ResolvableType getResolvableType(RootBeanDefinition rootBeanDefinition) {
        MethodHandle methodHandle = GET_RESOLVABLE_TYPE_METHOD_HANDLE;
        if (methodHandle == null) {
            return doGetResolvableType(rootBeanDefinition);
        }
        ResolvableType resolvableType = null;
        try {
            resolvableType = (ResolvableType) methodHandle.invokeExact(rootBeanDefinition);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, rootBeanDefinition);
            resolvableType = doGetResolvableType(rootBeanDefinition);
        }
        return resolvableType;
    }

    /**
     * Sets the instance supplier for the given {@link AbstractBeanDefinition} using reflection if the method is available.
     *
     * <p>This method attempts to invoke {@link AbstractBeanDefinition#setInstanceSupplier(Supplier)} via a pre-resolved
     * {@link MethodHandle}. If the method handle is not available or the instance supplier is null, this method returns false.
     * Any exceptions thrown during the invocation are handled gracefully using
     * {@link io.microsphere.invoke.MethodHandleUtils#handleInvokeExactFailure(Throwable, MethodHandle, Object...)}.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Usage</h4>
     * <pre>{@code
     * AbstractBeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(MyService.class).getBeanDefinition();
     * Supplier<MyService> supplier = MyService::new;
     * boolean success = setInstanceSupplier(beanDefinition, supplier);
     * }</pre>
     *
     * <h4>Null Supplier Case</h4>
     * <pre>{@code
     * boolean success = setInstanceSupplier(beanDefinition, null); // Returns false
     * }</pre>
     *
     * @param beanDefinition   the target bean definition, must not be null
     * @param instanceSupplier the supplier to provide instances of the bean, may be null
     * @return true if the instance supplier was successfully set; false otherwise
     */
    public static boolean setInstanceSupplier(AbstractBeanDefinition beanDefinition, @Nullable Supplier<?> instanceSupplier) {
        MethodHandle methodHandle = SET_INSTANCE_SUPPLIER_METHOD_HANDLE;
        if (methodHandle == null || instanceSupplier == null) {
            return false;
        }
        try {
            methodHandle.invokeExact(beanDefinition, instanceSupplier);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, beanDefinition, instanceSupplier);
        }
        return true;
    }

    /**
     * Retrieve the instance supplier associated with the given {@link AbstractBeanDefinition}.
     *
     * <p>This method uses a pre-resolved {@link MethodHandle} to invoke
     * {@link AbstractBeanDefinition#getInstanceSupplier()} if available. If the method handle is not present
     * or an error occurs during invocation, it gracefully returns {@code null}.
     *
     * <h3>Example Usage</h3>
     *
     * <h4>Basic Retrieval</h4>
     * <pre>{@code
     * AbstractBeanDefinition beanDefinition = ...; // Obtain from bean factory
     * Supplier<?> supplier = getInstanceSupplier(beanDefinition);
     * if (supplier != null) {
     *     System.out.println("Instance supplier found");
     * } else {
     *     System.out.println("No instance supplier found");
     * }
     * }</pre>
     *
     * <h4>Integration with Spring Context</h4>
     * <pre>{@code
     * ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
     * String[] beanNames = beanFactory.getBeanDefinitionNames();
     * for (String beanName : beanNames) {
     *     BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName);
     *     if (beanDefinition instanceof AbstractBeanDefinition) {
     *         Supplier<?> supplier = getInstanceSupplier((AbstractBeanDefinition) beanDefinition);
     *         if (supplier != null) {
     *             System.out.println("Bean: " + beanName + " has an instance supplier");
     *         }
     *     }
     * }
     * }</pre>
     *
     * @param beanDefinition the target bean definition, must not be null
     * @return the instance supplier if available; otherwise, {@code null}
     */
    @Nullable
    public static Supplier<?> getInstanceSupplier(AbstractBeanDefinition beanDefinition) {
        MethodHandle methodHandle = GET_INSTANCE_SUPPLIER_METHOD_HANDLE;
        if (methodHandle == null) {
            return null;
        }
        Supplier<?> supplier = null;
        try {
            supplier = (Supplier<?>) methodHandle.invokeExact(beanDefinition);
        } catch (Throwable e) {
            handleInvokeExactFailure(e, methodHandle, beanDefinition);
        }
        return supplier;
    }

    /**
     * Compatible with {@link RootBeanDefinition#getResolvableType()) since Spring Framework 5.1
     *
     * @param rootBeanDefinition {@link RootBeanDefinition}
     * @return
     */
    protected static ResolvableType doGetResolvableType(RootBeanDefinition rootBeanDefinition) {
        if (rootBeanDefinition == null) {
            if (logger.isTraceEnabled()) {
                logger.trace("The argument of RootBeanDefinition is null");
            }
            return NONE;
        }
        Method factoryMethod = rootBeanDefinition.getResolvedFactoryMethod();
        if (factoryMethod != null) {
            return forMethodReturnType(factoryMethod);
        }
        return doGetResolvableType((AbstractBeanDefinition) rootBeanDefinition);
    }

    /**
     * Compatible with {@link AbstractBeanDefinition#getResolvableType()) since Spring Framework 5.2
     *
     * @param beanDefinition {@link AbstractBeanDefinition}
     * @return {@link ResolvableType#NONE} if can't be resolved
     */
    protected static ResolvableType doGetResolvableType(AbstractBeanDefinition beanDefinition) {
        return beanDefinition.hasBeanClass() ? forClass(beanDefinition.getBeanClass()) : NONE;
    }

    private BeanDefinitionUtils() {
    }
}
