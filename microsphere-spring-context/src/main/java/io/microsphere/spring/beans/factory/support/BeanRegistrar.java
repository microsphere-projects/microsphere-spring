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

import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.DelegatingFactoryBean;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.AliasRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asBeanDefinitionRegistry;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static java.beans.Introspector.decapitalize;
import static java.lang.String.format;
import static org.springframework.aop.support.AopUtils.getTargetClass;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getShortName;
import static org.springframework.util.ClassUtils.resolveClassName;
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
     * Registers an infrastructure bean of the specified type into the given registry.
     * <p>
     * This method generates a unique bean name based on the bean definition and registers it as an infrastructure bean.
     * If the bean definition is successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerInfrastructureBean(registry, MyInfrastructureClass.class);
     * if (registered) {
     *     System.out.println("Infrastructure bean registered successfully.");
     * } else {
     *     System.out.println("Infrastructure bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, ROLE_INFRASTRUCTURE);
        String beanName = generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers an infrastructure bean with the specified name and type into the given registry.
     * <p>
     * This method creates a generic bean definition for the provided bean type with the default role,
     * and attempts to register it under the specified bean name. If the bean is successfully registered,
     * it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerInfrastructureBean(registry, "myBean", MyBeanClass.class);
     * if (registered) {
     *     System.out.println("Bean registered successfully.");
     * } else {
     *     System.out.println("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, ROLE_INFRASTRUCTURE);
        return registerBeanDefinition(registry, beanName, beanDefinition);
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
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        String beanName = generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers a bean definition with the specified name and type into the given registry.
     * <p>
     * This method creates a generic bean definition for the provided bean type and attempts to register it
     * under the specified bean name. If the bean is successfully registered, it returns {@code true};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerBeanDefinition(registry, "myBean", MyBean.class);
     * if (registered) {
     *     logger.info("Bean registered successfully.");
     * } else {
     *     logger.warn("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry.
     * @param beanType The class type of the bean to be registered.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Registers a bean definition with the specified name, type, and constructor arguments into the given registry.
     * <p>
     * This method creates a generic bean definition for the provided bean type using the supplied constructor arguments,
     * and attempts to register it under the specified bean name. If the bean is successfully registered, it returns {@code true};
     * otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerBeanDefinition(registry, "myService", MyService.class, "arg1", 123);
     * if (registered) {
     *     logger.info("Bean with custom constructor arguments registered successfully.");
     * } else {
     *     logger.warn("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry             The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName             The name to assign to the bean in the registry.
     * @param beanType             The class type of the bean to be registered.
     * @param constructorArguments The arguments to use for the constructor when instantiating the bean.
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, Object... constructorArguments) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, constructorArguments);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register a bean definition with the specified name, type, and role.
     * <p>
     * This method creates a generic bean definition for the provided bean type with the specified role,
     * and attempts to register it under the specified bean name. If the bean is successfully registered,
     * it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * boolean registered = registerBeanDefinition(registry, "myService", MyService.class, BeanDefinition.ROLE_INFRASTRUCTURE);
     * if (registered) {
     *     logger.info("Bean with custom role registered successfully.");
     * } else {
     *     logger.warn("Bean was already registered.");
     * }
     * }</pre>
     *
     * @param registry The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName The name to assign to the bean in the registry.
     * @param beanType The class type of the bean to be registered.
     * @param role     The role hint for the bean definition ({@link BeanDefinition#ROLE_APPLICATION},
     *                 {@link BeanDefinition#ROLE_INFRASTRUCTURE}, or {@link BeanDefinition#ROLE_SUPPORT})
     * @return Returns {@code true} if the bean is registered for the first time; returns {@code false} if it was already registered.
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, int role) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register a {@link BeanDefinition} with name if absent.
     * <p>
     * This method attempts to register the given bean definition under the specified name only if there is no existing
     * bean definition with the same name in the registry. It internally delegates to
     * {@link #registerBeanDefinition(BeanDefinitionRegistry, String, BeanDefinition, boolean)} with
     * {@code allowBeanDefinitionOverriding} set to {@code false}.
     * </p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * BeanDefinition beanDefinition = genericBeanDefinition(MyService.class);
     * String beanName = "myService";
     * boolean registered = registerBeanDefinition(registry, beanName, beanDefinition);
     * if (registered) {
     *     logger.info("Bean was successfully registered.");
     * } else {
     *     logger.warn("Bean was already registered and not overridden.");
     * }
     * }</pre>
     *
     * @param registry       The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName       The name to assign to the bean in the registry.
     * @param beanDefinition The bean definition to register.
     * @return Returns {@code true} if the bean definition was registered for the first time; returns {@code false}
     * if it was already registered and overriding is disabled.
     */
    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, beanName, beanDefinition, false);
    }

    /**
     * Registers a bean definition with the specified name into the given registry.
     * <p>
     * This method attempts to register the provided bean definition under the specified bean name. If the bean definition
     * is successfully registered, it returns {@code true}; otherwise, it returns {@code false}.
     * </p>
     *
     * <p>If overriding is not allowed and a bean definition with the same name already exists in the registry, the new
     * definition will not be registered, a warning log message will be generated, and this method will return
     * {@code false}.</p>
     *
     * <p>If overriding is allowed and a bean definition with the same name already exists, the existing definition will
     * be replaced with the new one.</p>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * BeanDefinition beanDefinition = genericBeanDefinition(MyService.class);
     * String beanName = "myService";
     * boolean allowBeanDefinitionOverriding = false;
     *
     * boolean registered = registerBeanDefinition(registry, beanName, beanDefinition, allowBeanDefinitionOverriding);
     * if (registered) {
     *     logger.info("Bean was successfully registered.");
     * } else {
     *     logger.warn("Bean was already registered and not overridden.");
     * }
     * }</pre>
     *
     * @param registry                      The {@link BeanDefinitionRegistry} where the bean will be registered.
     * @param beanName                      The name to assign to the bean in the registry.
     * @param beanDefinition                The bean definition to register.
     * @param allowBeanDefinitionOverriding Whether the bean definition is allowed to override an existing one.
     * @return <code>true</code> if registered successfully; <code>false</code> if it was already registered and overriding is disabled.
     */
    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition, boolean allowBeanDefinitionOverriding) {

        boolean registered = false;

        if (!allowBeanDefinitionOverriding && registry.containsBeanDefinition(beanName)) {
            BeanDefinition oldBeanDefinition = registry.getBeanDefinition(beanName);
            if (logger.isWarnEnabled()) {
                logger.warn("The bean[name : '{}'] definition [{}] was registered!", beanName, oldBeanDefinition);
            }
        } else {
            try {
                registry.registerBeanDefinition(beanName, beanDefinition);
                if (logger.isTraceEnabled()) {
                    logger.trace("The bean[name : '{}' , role : {}] definition [{}] has been registered.", beanName, beanDefinition.getRole(), beanDefinition);
                }
                registered = true;
            } catch (BeanDefinitionStoreException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("The bean[name : '{}' , role : {}] definition [{}] can't be registered ", beanName, beanDefinition.getRole(), e);
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
     * Registers beans from the specified factory classes using Spring's {@link SpringFactoriesLoader}.
     * <p>
     * This method loads the fully qualified implementation class names from the classpath resources located in
     * {@code META-INF/spring.factories} for each given factory interface or abstract class. It then registers each
     * implementation class as a bean in the provided {@link BeanFactory}.
     * </p>
     *
     * <h3>How It Works</h3>
     * <ol>
     *   <li>For each given factory class, it loads the list of implementation class names using
     *       {@link SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader)}.</li>
     *   <li>Each implementation class is resolved and registered as a Spring bean with a generated name derived from
     *       its short class name (starting with a lowercase letter).</li>
     *   <li>If a bean with the same name already exists and overriding is disabled, it logs a warning but skips registration.</li>
     * </ol>
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * int count = registerSpringFactoriesBeans(beanFactory, MyFactory.class);
     * System.out.println(count + " beans were successfully registered.");
     * }</pre>
     *
     * <p><strong>Note:</strong> This method delegates to the overloaded version that accepts a
     * {@link BeanDefinitionRegistry} by converting the given {@link BeanFactory} into one via
     * {@link BeanFactoryUtils#asBeanDefinitionRegistry(Object)}.</p>
     *
     * @param beanFactory    The {@link BeanFactory} where the beans will be registered.
     * @param factoryClasses The array of factory classes used to locate implementations from spring.factories files.
     * @return The number of beans that were successfully registered.
     */
    public static int registerSpringFactoriesBeans(BeanFactory beanFactory, Class<?>... factoryClasses) {
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
     * int count = registerSpringFactoriesBeans(registry, MyFactory.class);
     * System.out.println(count + " beans were successfully registered.");
     * }</pre>
     *
     * <h4>Generated Bean Name Example</h4>
     * Given an implementation class named {@code io.example.MyServiceImpl}, the generated bean name would be:
     * <pre>{@code
     * String beanName = decapitalize(getShortName("io.example.MyServiceImpl")); // returns "myServiceImpl"
     * }</pre>
     *
     * @param registry       The {@link BeanDefinitionRegistry} where beans will be registered.
     * @param factoryClasses An array of factory interface or abstract base classes used to locate implementations via spring.factories.
     * @return The number of beans that were successfully registered.
     */
    public static int registerSpringFactoriesBeans(BeanDefinitionRegistry registry, Class<?>... factoryClasses) {
        int count = 0;

        for (int i = 0; i < factoryClasses.length; i++) {
            Class<?> factoryClass = factoryClasses[i];
            ClassLoader classLoader = factoryClass.getClassLoader();
            List<String> factoryImplClassNames = loadFactoryNames(factoryClass, classLoader);
            for (String factoryImplClassName : factoryImplClassNames) {
                Class<?> factoryImplClass = resolveClassName(factoryImplClassName, classLoader);
                String beanName = decapitalize(getShortName(factoryImplClassName));
                if (registerInfrastructureBean(registry, beanName, factoryImplClass)) {
                    count++;
                } else {
                    if (logger.isWarnEnabled()) {
                        logger.warn(format("The Factory Class bean[%s] has been registered with bean name[%s]", factoryImplClassName, beanName));
                    }
                }
            }
        }

        return count;
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
        beanDefinition.setInstanceSupplier(() -> bean);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }
}
