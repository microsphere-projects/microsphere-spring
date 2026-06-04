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
package io.microsphere.spring.beans.factory.support;

import io.microsphere.annotation.Nullable;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.DelegatingFactoryBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.AliasRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.MapUtils.immutableEntry;
import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asConfigurableListableBeanFactory;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.support.GenericBeanNameGenerator.INSTANCE;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactoryClasses;
import static io.microsphere.util.ArrayUtils.EMPTY_CLASS_ARRAY;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.util.ObjectUtils.containsElement;
import static org.springframework.util.StringUtils.hasText;

/**
 * BeanRegistrar provides utility methods for registering beans within a Spring {@link BeanDefinitionRegistry}.
 * <p>
 * This abstract class offers various static methods to register bean definitions, singleton beans, and factory beans.
 * It supports functionalities such as bean overriding control, role-based registration, and custom naming strategies.
 * </p>
 *
 * <h3>Key Features</h3>
 * <ul>
 *     <li>Register infrastructure or application beans with auto-generated or explicit names.</li>
 *     <li>Control bean overriding behavior during registration.</li>
 *     <li>Supports registration from Spring factories using {@code spring.factories} resources.</li>
 *     <li>Provides logging via the Microsphere Logger for trace, debug, warn, and error levels.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Registering an Infrastructure Bean</h4>
 * <pre>{@code
 * boolean registered = BeanRegistrar.registerInfrastructureBean(registry, MyService.class);
 * if (registered) {
 *     logger.info("Infrastructure bean registered successfully.");
 * } else {
 *     logger.warn("Infrastructure bean was already registered.");
 * }
 * }</pre>
 *
 * <h4>Registering a Bean with Explicit Name</h4>
 * <pre>{@code
 * boolean registered = BeanRegistrar.registerBeanDefinition(registry, "customName", MyService.class);
 * if (registered) {
 *     logger.info("Bean registered under name 'customName'.");
 * }
 * }</pre>
 *
 * <h4>Registering a Singleton Bean</h4>
 * <pre>{@code
 * MySingleton mySingleton = new MySingleton();
 * BeanRegistrar.registerSingleton(registry, "mySingleton", mySingleton);
 * }</pre>
 *
 * <h4>Registering Beans from Spring Factories</h4>
 * <pre>{@code
 * int count = BeanRegistrar.registerSpringFactoriesBeans(registry, MyFactory.class);
 * logger.info("{} beans registered from spring.factories.", count);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @since 1.0.0
 */
public abstract class BeanRegistrar {

    private static final Logger logger = getLogger(BeanRegistrar.class);

    /**
     * Registers an infrastructure bean with the specified type into the given registry.
     * <p>
     * This method delegates to {@link #registerInfrastructureBean(BeanDefinitionRegistry, String, Class)}
     * with a {@code null} bean name, causing a unique bean name to be generated automatically.
     * The registered bean definition will have its role set to {@link BeanDefinition#ROLE_INFRASTRUCTURE}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerInfrastructureBean(registry, MyInfrastructureComponent.class);
     * if (registered) {
     *     logger.info("Infrastructure bean registered successfully with auto-generated name.");
     * } else {
     *     logger.warn("Infrastructure bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, Class<?> beanType) {
        return registerInfrastructureBean(registry, null, beanType);
    }

    /**
     * Registers an infrastructure bean with the specified type and optional name into the given registry.
     * <p>
     * This method creates a generic bean definition for the provided bean type with the infrastructure role,
     * uses the provided bean name if present, or generates a unique bean name otherwise. It then attempts to
     * register the bean definition. If the bean is successfully registered, it returns {@code true}; otherwise,
     * it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name
     * boolean registered = registerInfrastructureBean(registry, null, MyInfrastructureComponent.class);
     *
     * // Register with explicit name
     * boolean registered = registerInfrastructureBean(registry, "myInfraBean", MyInfrastructureComponent.class);
     *
     * if (registered) {
     *     logger.info("Infrastructure bean registered successfully.");
     * } else {
     *     logger.warn("Infrastructure bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        return registerBeanDefinition(registry, beanName, beanType, ROLE_INFRASTRUCTURE);
    }

    /**
     * Register a {@link BeanDefinition} for the specified bean type.
     * <p>
     * This method generates a unique bean name based on the provided bean type and registers its bean definition
     * in the given registry. If the bean definition is successfully registered, it returns {@code true};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerBeanDefinition(registry, MyBean.class);
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType) {
        return registerBeanDefinition(registry, null, beanType);
    }

    /**
     * Register a {@link BeanDefinition} for the specified bean type with an optional name.
     * <p>
     * This method creates a generic bean definition for the provided bean type. If a bean name is provided,
     * it will be used; otherwise, a unique bean name will be generated automatically based on the bean type.
     * The bean definition is then registered in the given registry. If the bean is successfully registered,
     * it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name
     * boolean registered = registerBeanDefinition(registry, null, MyBean.class);
     *
     * // Register with explicit name
     * boolean registered = registerBeanDefinition(registry, "myCustomBean", MyBean.class);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register a {@link BeanDefinition} for the specified bean type with constructor arguments.
     * <p>
     * This method creates a generic bean definition for the provided bean type, applying the specified
     * constructor arguments. If a bean name is provided, it will be used; otherwise, a unique bean name
     * will be generated automatically based on the bean type. The bean definition is then registered in
     * the given registry. If the bean is successfully registered, it returns {@code true}; otherwise,
     * it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name and constructor arguments
     * boolean registered = registerBeanDefinition(registry, null, MyBean.class, "arg1", 42);
     *
     * // Register with explicit name and constructor arguments
     * boolean registered = registerBeanDefinition(registry, "myCustomBean", MyBean.class, "arg1", 42);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry             The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName             The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType             The class type of the bean to be registered.
     * @param constructorArguments The arguments to pass to the bean's constructor.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, Object... constructorArguments) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, constructorArguments);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers a {@link BeanDefinition} for the specified bean type with a specific role.
     * <p>
     * This method creates a generic bean definition for the provided bean type and assigns it the specified role.
     * If a bean name is provided, it will be used; otherwise, a unique bean name will be generated automatically
     * based on the bean type. The bean definition is then registered in the given registry. If the bean is
     * successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register as an infrastructure bean (ROLE_INFRASTRUCTURE)
     * boolean registered = registerBeanDefinition(registry, null, MyInfrastructure.class, BeanDefinition.ROLE_INFRASTRUCTURE);
     *
     * // Register as a support bean (ROLE_SUPPORT)
     * boolean registered = registerBeanDefinition(registry, "mySupportBean", MySupport.class, BeanDefinition.ROLE_SUPPORT);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully with the specified role.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType The class type of the bean to be registered.
     * @param role     The role of the bean definition (e.g., {@link BeanDefinition#ROLE_APPLICATION},
     *                 {@link BeanDefinition#ROLE_SUPPORT}, or {@link BeanDefinition#ROLE_INFRASTRUCTURE}).
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, int role) {
        return registerBeanDefinition(registry, beanName, beanType, builder -> builder.setRole(role));
    }

    /**
     * Registers a {@link BeanDefinition} for the specified bean type with custom configuration via a builder consumer.
     * <p>
     * This method creates a generic bean definition for the provided bean type and applies custom configurations
     * using the specified {@link Consumer} of {@link BeanDefinitionBuilder}. A unique bean name will be generated
     * automatically based on the bean type. The bean definition is then registered in the given registry.
     * If the bean is successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name and custom property
     * boolean registered = registerBeanDefinition(registry, MyBean.class, builder ->
     *     builder.addPropertyValue("myProperty", "myValue")
     * );
     *
     * // Register with auto-generated name and custom constructor argument
     * boolean registered = registerBeanDefinition(registry, MyBean.class, builder ->
     *     builder.addConstructorArgValue("arg1")
     * );
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully with custom configuration.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry        The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanType        The class type of the bean to be registered.
     * @param builderConsumer A {@link Consumer} that configures the {@link BeanDefinitionBuilder} before building the definition.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType,
                                                 Consumer<BeanDefinitionBuilder> builderConsumer) {
        return registerBeanDefinition(registry, null, beanType, builderConsumer);
    }

    /**
     * Registers a {@link BeanDefinition} for the specified bean type with custom configuration via a builder consumer.
     * <p>
     * This method creates a generic bean definition for the provided bean type and applies custom configurations
     * using the specified {@link Consumer} of {@link BeanDefinitionBuilder}. If a bean name is provided,
     * it will be used; otherwise, a unique bean name will be generated automatically based on the bean type.
     * The bean definition is then registered in the given registry. If the bean is successfully registered,
     * it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name and custom property
     * boolean registered = registerBeanDefinition(registry, null, MyBean.class, builder ->
     *     builder.addPropertyValue("myProperty", "myValue")
     * );
     *
     * // Register with explicit name and custom constructor argument
     * boolean registered = registerBeanDefinition(registry, "myCustomBean", MyBean.class, builder ->
     *     builder.addConstructorArgValue("arg1")
     * );
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully with custom configuration.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry        The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName        The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType        The class type of the bean to be registered.
     * @param builderConsumer A {@link Consumer} that configures the {@link BeanDefinitionBuilder} before building the definition.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, Consumer<BeanDefinitionBuilder> builderConsumer) {
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanType, builderConsumer);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers a {@link BeanDefinition} for the specified bean type with the primary flag.
     * <p>
     * This method creates a generic bean definition for the provided bean type and sets its primary status.
     * A unique bean name will be generated automatically based on the bean type. The bean definition is then
     * registered in the given registry. If the bean is successfully registered, it returns {@code true};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register as a primary bean
     * boolean registered = registerBeanDefinition(registry, MyService.class, true);
     *
     * if (registered) {
     *     System.out.println("Primary bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanType The class type of the bean to be registered.
     * @param primary  Whether this bean should be marked as the primary bean.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType, boolean primary) {
        return registerBeanDefinition(registry, null, beanType, primary);
    }

    /**
     * Registers a {@link BeanDefinition} for the specified bean type with the primary flag and an optional name.
     * <p>
     * This method creates a generic bean definition for the provided bean type and sets its primary status.
     * If a bean name is provided, it will be used; otherwise, a unique bean name will be generated automatically
     * based on the bean type. The bean definition is then registered in the given registry. If the bean is
     * successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register as a primary bean with auto-generated name
     * boolean registered = registerBeanDefinition(registry, null, MyService.class, true);
     *
     * // Register as a non-primary bean with explicit name
     * boolean registered = registerBeanDefinition(registry, "mySecondaryService", MyService.class, false);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully with primary flag.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanType The class type of the bean to be registered.
     * @param primary  Whether this bean should be marked as the primary bean.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 Class<?> beanType, boolean primary) {
        return registerBeanDefinition(registry, beanName, beanType, builder -> builder.setPrimary(primary));
    }

    /**
     * Registers a {@link BeanDefinition} with an optional name into the given registry.
     * <p>
     * This method attempts to register the provided bean definition. If a bean name is provided,
     * it will be used; otherwise, a unique bean name will be generated automatically based on the
     * bean definition. The registration process respects the default bean overriding settings of the registry.
     * If the bean is successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name
     * BeanDefinition beanDefinition = genericBeanDefinition(MyService.class);
     * boolean registered = registerBeanDefinition(registry, null, beanDefinition);
     *
     * // Register with explicit name
     * BeanDefinition anotherDefinition = genericBeanDefinition(AnotherService.class);
     * boolean registered = registerBeanDefinition(registry, "anotherService", anotherDefinition);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry       The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName       The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanDefinition The bean definition to register.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, beanName, beanDefinition, false);
    }

    /**
     * Registers a {@link BeanDefinition} with an optional name and overriding control into the given registry.
     * <p>
     * This method attempts to register the provided bean definition. If a bean name is provided,
     * it will be used; otherwise, a unique bean name will be generated automatically based on the
     * bean definition. The registration process respects the {@code allowBeanDefinitionOverriding} flag.
     * If overriding is not allowed and a bean with the same name already exists, the registration is skipped,
     * and a warning is logged. If the bean is successfully registered, it returns {@code true}; otherwise,
     * it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Register with auto-generated name, allowing overriding
     * BeanDefinition beanDefinition = genericBeanDefinition(MyService.class);
     * boolean registered = registerBeanDefinition(registry, null, beanDefinition, true);
     *
     * // Register with explicit name, disallowing overriding
     * BeanDefinition anotherDefinition = genericBeanDefinition(AnotherService.class);
     * boolean registered = registerBeanDefinition(registry, "anotherService", anotherDefinition, false);
     *
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was not registered (already exists or error).");
     * }
     * }</pre>
     *
     * @param registry                      The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName                      The name to assign to the bean in the registry, or {@code null} to generate a unique name.
     * @param beanDefinition                The bean definition to register.
     * @param allowBeanDefinitionOverriding Whether to allow overriding of an existing bean definition with the same name.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered or registration failed.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, @Nullable String beanName,
                                                 BeanDefinition beanDefinition, boolean allowBeanDefinitionOverriding) {

        String actualBeanName = beanName == null ? generateBeanName(beanDefinition, registry) : beanName;

        boolean registered = false;

        if (!allowBeanDefinitionOverriding && registry.containsBeanDefinition(actualBeanName)) {
            BeanDefinition oldBeanDefinition = registry.getBeanDefinition(actualBeanName);
            if (logger.isWarnEnabled()) {
                logger.warn("The bean[name : '{}'] definition [{}] was registered!", actualBeanName, oldBeanDefinition);
            }
        } else {
            try {
                registry.registerBeanDefinition(actualBeanName, beanDefinition);
                if (logger.isTraceEnabled()) {
                    logger.trace("The bean[name : '{}' , role : {}] definition [{}] has been registered.", actualBeanName, beanDefinition.getRole(), beanDefinition);
                }
                registered = true;
            } catch (BeanDefinitionStoreException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("The bean[name : '{}' , role : {}] definition [{}] can't be registered ", actualBeanName, beanDefinition.getRole(), e);
                }
                registered = false;
            }
        }
        return registered;
    }

    /**
     * Registers a singleton bean with the specified name into the given registry.
     * <p>
     * This method registers the provided bean as a singleton instance under the specified bean name.
     * If the registration is successful and logging at INFO level is enabled, an informational log message is generated.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = new MyServiceImpl();
     * registerSingleton(registry, "myService", myService);
     * }</pre>
     *
     * @param registry The {@link SingletonBeanRegistry} where the singleton bean will be registered.
     * @param beanName The name to assign to the singleton bean in the registry.
     * @param bean     The singleton instance to register.
     */
    public static void registerSingleton(SingletonBeanRegistry registry, String beanName, Object bean) {
        registry.registerSingleton(beanName, bean);
        if (logger.isInfoEnabled()) {
            logger.info("The singleton bean [name : '{}' , instance : {}] has been registered into the BeanFactory.", beanName, bean);
        }
    }

    /**
     * Checks whether the specified alias is associated with the given bean name in the registry.
     *
     * <p>This method verifies if both the bean name and alias are non-empty, and then checks
     * if the alias exists in the list of aliases for the specified bean name.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean hasAlias = BeanRegistrar.hasAlias(registry, "myBean", "myAlias");
     * if (hasAlias) {
     *     logger.info("Alias 'myAlias' is registered for bean 'myBean'.");
     * } else {
     *     logger.info("Alias 'myAlias' is not registered for bean 'myBean'.");
     * }
     * }</pre>
     *
     * @param registry The {@link AliasRegistry} to check for the alias.
     * @param beanName The name of the bean whose aliases are being checked.
     * @param alias    The alias to look for.
     * @return {@code true} if the alias exists for the given bean name; {@code false} otherwise.
     */
    public static boolean hasAlias(AliasRegistry registry, String beanName, String alias) {
        return hasText(beanName) && hasText(alias) && containsElement(registry.getAliases(beanName), alias);
    }

    /**
     * Registers beans into the Spring {@link BeanFactory} by loading their implementation class names from
     * the classpath resource file {@code META-INF/spring.factories}. This method delegates to
     * {@link #registerSpringFactoriesBeans(BeanDefinitionRegistry, Class...)} after converting the
     * {@link BeanFactory} to a {@link BeanDefinitionRegistry}.
     *
     * <p>If the provided {@link BeanFactory} does not implement {@link BeanDefinitionRegistry}, this method
     * will attempt to unwrap it or return an empty map depending on the implementation of
     * {@link io.microsphere.spring.beans.factory.BeanFactoryUtils#asBeanDefinitionRegistry(Object)}.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Assuming beanFactory is an instance of ConfigurableApplicationContext or similar
     * Map<Class, String> registeredBeans = registerSpringFactoriesBeans(beanFactory, MyFactory.class);
     * registeredBeans.forEach((clazz, name) -> {
     *     System.out.println("Registered bean: " + name + " of type: " + clazz.getName());
     * });
     * }</pre>
     *
     * @param beanFactory    The {@link BeanFactory} where beans will be registered.
     * @param factoryClasses An array of factory interface or abstract base classes used to locate implementations via spring.factories.
     * @return A map of successfully registered bean classes to their assigned bean names. The map is unmodifiable.
     * @see #registerSpringFactoriesBeans(BeanDefinitionRegistry, Class...)
     */
    public static Map<Class, String> registerSpringFactoriesBeans(BeanFactory beanFactory, Class<?>... factoryClasses) {
        return registerSpringFactoriesBeans(asBeanDefinitionRegistry(beanFactory), factoryClasses);
    }

    /**
     * Registers beans into the Spring {@link BeanDefinitionRegistry} by loading their implementation class names from
     * the classpath resource file {@code META-INF/spring.factories}. This method iterates through each provided factory class,
     * loads its corresponding implementation classes, and registers them as Spring beans with automatically generated bean names.
     *
     * <p>If a bean with the same name already exists in the registry and overriding is not explicitly allowed, it will not be registered again,
     * and a warning message will be logged.</p>
     *
     * <h3>Bean Registration Logic</h3>
     * <ol>
     *   <li>For each given factory class, this method uses
     *       {@link SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)} to load fully qualified implementation class names.</li>
     *   <li>Each implementation class is resolved using its class loader and registered under a bean name derived from its short class name,
     *       with the first letter decapitalized (e.g., {@code MyService} becomes {@code myService}).</li>
     *   <li>{@link #registerInfrastructureBean(BeanDefinitionRegistry, String, Class)} is used to register the bean definition. If registration
     *       succeeds, the successful registration counter increments; otherwise, a warning is logged.</li>
     * </ol>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<Class, String> registeredBeans = registerSpringFactoriesBeans(registry, MyFactory.class);
     * registeredBeans.forEach((clazz, name) -> {
     *     System.out.println("Registered bean: " + name + " of type: " + clazz.getName());
     * });
     * }</pre>
     *
     * <h4>Generated Bean Name Example</h4>
     * Given an implementation class named {@code io.example.MyServiceImpl}, the generated bean name would be:
     * <pre>{@code
     * String beanName = BeanUtils.generateBeanName("io.example.MyServiceImpl"); // returns "myServiceImpl"
     * }</pre>
     *
     * @param registry       The {@link BeanDefinitionRegistry} where beans will be registered.
     * @param factoryClasses An array of factory interface or abstract base classes used to locate implementations via spring.factories.
     * @return A map of successfully registered bean classes to their assigned bean names. The map is unmodifiable.
     */
    public static Map<Class, String> registerSpringFactoriesBeans(BeanDefinitionRegistry registry, Class<?>... factoryClasses) {
        // Convert the array of factory classes into a Set for efficient lookup and to avoid duplicates
        Set<Class<?>> factoryClassesSet = ofSet(factoryClasses);
        ConfigurableListableBeanFactory beanFactory = asConfigurableListableBeanFactory(registry);
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        Map<Class, String> registeredBeanClassesAndNames = newHashMap(factoryClassesSet.size() * 2);
        for (Class<?> factoryClass : factoryClassesSet) {
            Set<Class<?>> factoryImplClasses = (Set) loadFactoryClasses(factoryClass, classLoader);
            registeredBeanClassesAndNames.putAll(registerGenericBeans(registry, factoryImplClasses));
        }
        return unmodifiableMap(registeredBeanClassesAndNames);
    }

    /**
     * Registers a {@link org.springframework.beans.factory.FactoryBean} with the specified name into the given registry.
     * <p>
     * This method wraps the provided bean instance inside a {@link DelegatingFactoryBean} and registers it as a bean definition.
     * If logging at TRACE level is enabled, a trace log message will be generated upon successful registration.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myServiceInstance = new MyServiceImpl();
     * BeanRegistrar.registerFactoryBean(registry, "myServiceFactory", myServiceInstance);
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the FactoryBean will be registered.
     * @param beanName The name to assign to the FactoryBean in the registry.
     * @param bean     The bean instance that will be wrapped by the FactoryBean.
     */
    public static void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerFactoryBean(registry, beanName, bean, false);
    }

    /**
     * Registers a {@link org.springframework.beans.factory.FactoryBean} with the specified name into the given registry.
     * <p>
     * This method creates a bean definition for a {@link DelegatingFactoryBean}, which wraps the provided bean instance,
     * and registers it under the specified bean name. The registered bean will act as a FactoryBean that delegates to
     * the supplied object.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = new MyServiceImpl();
     * registerFactoryBean(registry, "myServiceFactory", myService, false);
     * }</pre>
     *
     * <p>If you want this FactoryBean to be marked as the primary bean in case of multiple candidates:</p>
     * <pre>{@code
     * registerFactoryBean(registry, "myPrimaryServiceFactory", myService, true);
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the FactoryBean will be registered.
     * @param beanName The name to assign to the FactoryBean in the registry.
     * @param bean     The bean instance that will be wrapped by the FactoryBean.
     * @param primary  Whether this bean should be marked as the primary bean.
     */
    public static void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(DelegatingFactoryBean.class, bean);
        beanDefinition.setSource(bean);
        beanDefinition.setPrimary(primary);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers a bean instance with the specified name into the given registry.
     * <p>
     * This method delegates to {@link #registerBean(BeanDefinitionRegistry, String, Object, boolean)}
     * with the default value of {@code false} for the primary flag. If the bean is successfully registered,
     * it will be treated as a regular bean unless further configuration is needed.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = new MyServiceImpl();
     * BeanRegistrar.registerBean(registry, "myService", myService);
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry.
     * @param bean     The bean instance to register.
     */
    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerBean(registry, beanName, bean, false);
    }

    /**
     * Registers a bean instance with the specified name and primary status into the given registry.
     * <p>
     * This method attempts to register the provided bean instance directly using an {@link AbstractBeanDefinition}
     * with an instance supplier. If setting the instance supplier fails (e.g., due to configuration constraints),
     * it falls back to registering the bean via a {@link DelegatingFactoryBean}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * MyService myService = new MyServiceImpl();
     * BeanRegistrar.registerBean(registry, "myService", myService, false);
     * }</pre>
     *
     * <p>To mark the bean as primary:</p>
     * <pre>{@code
     * BeanRegistrar.registerBean(registry, "myPrimaryService", myService, true);
     * }</pre>
     *
     * <p>If direct instance registration is not possible (e.g., due to proxying or complex lifecycle), the bean will be
     * registered via a FactoryBean:</p>
     * <pre>{@code
     * AnotherBean anotherBean = new AnotherBean();
     * BeanRegistrar.registerBean(registry, "anotherBean", anotherBean, false);
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry.
     * @param bean     The bean instance to register.
     * @param primary  Whether this bean should be marked as the primary bean.
     */
    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        Class beanClass = getTargetClass(bean);
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanClass);
        beanDefinition.setPrimary(primary);
        beanDefinition.setInstanceSupplier(() -> bean);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers generic bean definitions for the specified collection of bean classes into the given registry.
     * <p>
     * This method iterates through the provided collection of bean classes, creates a generic bean definition for each,
     * generates a unique bean name, and attempts to register it in the registry. It returns an unmodifiable map
     * containing the successfully registered bean classes mapped to their assigned bean names.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * List<Class<?>> beanClasses = Arrays.asList(MyService.class, MyRepository.class);
     * Map<Class<?>, String> registeredBeans = registerGenericBeans(registry, beanClasses);
     * registeredBeans.forEach((clazz, name) -> {
     *     logger.info("Registered bean: {} of type: {}", name, clazz.getName());
     * });
     * }</pre>
     *
     * @param registry    The {@link BeanDefinitionRegistry} where the beans will be registered.
     * @param beanClasses A collection of class types of the beans to be registered.
     * @return An unmodifiable map of registered bean classes to their assigned bean names.
     */
    public static Map<Class<?>, String> registerGenericBeans(BeanDefinitionRegistry registry, Collection<Class<?>> beanClasses) {
        return registerGenericBeans(registry, beanClasses.toArray(EMPTY_CLASS_ARRAY));
    }

    /**
     * Registers generic bean definitions for the specified bean classes into the given registry.
     * <p>
     * This method iterates through the provided bean classes, creates a generic bean definition for each,
     * generates a unique bean name, and attempts to register it in the registry. It returns an unmodifiable map
     * containing the successfully registered bean classes mapped to their assigned bean names.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Map<Class<?>, String> registeredBeans = registerGenericBeans(registry, MyService.class, MyRepository.class);
     * registeredBeans.forEach((clazz, name) -> {
     *     logger.info("Registered bean: {} of type: {}", name, clazz.getName());
     * });
     * }</pre>
     *
     * @param registry    The {@link BeanDefinitionRegistry} where the beans will be registered.
     * @param beanClasses An array of class types of the beans to be registered.
     * @return An unmodifiable map of registered bean classes to their assigned bean names.
     */
    public static Map<Class<?>, String> registerGenericBeans(BeanDefinitionRegistry registry, Class<?>... beanClasses) {
        int length = length(beanClasses);
        if (length == 0) {
            return emptyMap();
        }
        Map<Class<?>, String> beanTypesAndNames = newFixedHashMap(length);
        for (int i = 0; i < length; i++) {
            Class<?> beanClass = beanClasses[i];
            Entry<String, Boolean> result = registerGenericBean(registry, beanClass);
            String beanName = result.getKey();
            beanTypesAndNames.put(beanClass, beanName);
        }
        return unmodifiableMap(beanTypesAndNames);
    }


    /**
     * Registers a generic bean definition for the specified bean class into the given registry.
     * <p>
     * This method creates a generic bean definition for the provided bean class, generates a unique bean name
     * using the default {@link BeanNameGenerator} ({@link GenericBeanNameGenerator#INSTANCE}), and attempts to
     * register it in the registry. It returns an immutable entry containing the generated bean name and a boolean
     * indicating whether the registration was successful.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * Entry<String, Boolean> result = registerGenericBean(registry, MyService.class);
     * String beanName = result.getKey(); // "myService"
     * boolean registered = result.getValue();
     * if (registered) {
     *     logger.info("Bean registered with name: {}", beanName);
     * } else {
     *     logger.warn("Bean with name '{}' was already registered.", beanName);
     * }
     * }</pre>
     *
     * @param registry  The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanClass The class type of the bean to be registered.
     * @return An immutable {@link Entry} where the key is the generated bean name and the value is {@code true} if
     * the bean was successfully registered, or {@code false} if it was already present and overriding is disabled.
     */
    public static Entry<String, Boolean> registerGenericBean(BeanDefinitionRegistry registry, Class<?> beanClass) {
        return registerGenericBean(registry, beanClass, INSTANCE);
    }

    /**
     * Registers a generic bean definition for the specified bean class into the given registry using a custom bean name generator.
     * <p>
     * This method creates a generic bean definition for the provided bean class, generates a unique bean name using the
     * specified {@link BeanNameGenerator}, and attempts to register it in the registry. It returns an immutable entry
     * containing the generated bean name and a boolean indicating whether the registration was successful.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * BeanNameGenerator customGenerator = new CustomBeanNameGenerator();
     * Entry<String, Boolean> result = registerGenericBean(registry, MyService.class, customGenerator);
     * String beanName = result.getKey();
     * boolean registered = result.getValue();
     * if (registered) {
     *     logger.info("Bean registered with name: {}", beanName);
     * } else {
     *     logger.warn("Bean with name '{}' was already registered.", beanName);
     * }
     * }</pre>
     *
     * @param registry          The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanClass         The class type of the bean to be registered.
     * @param beanNameGenerator The {@link BeanNameGenerator} to use for generating the bean name.
     * @return An immutable {@link Entry} where the key is the generated bean name and the value is {@code true} if
     * the bean was successfully registered, or {@code false} if it was already present and overriding is disabled.
     */
    public static Entry<String, Boolean> registerGenericBean(BeanDefinitionRegistry registry, Class<?> beanClass,
                                                             BeanNameGenerator beanNameGenerator) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanClass);
        String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
        boolean registered = registerBeanDefinition(registry, beanName, beanDefinition);
        return immutableEntry(beanName, registered);
    }

    private BeanRegistrar() {
    }
}