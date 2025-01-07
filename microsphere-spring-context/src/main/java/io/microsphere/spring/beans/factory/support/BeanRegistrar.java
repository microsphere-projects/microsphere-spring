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
import org.springframework.aop.support.AopUtils;
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
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.setInstanceSupplier;
import static java.beans.Introspector.decapitalize;
import static java.lang.String.format;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.beans.factory.support.BeanDefinitionReaderUtils.generateBeanName;
import static org.springframework.core.io.support.SpringFactoriesLoader.loadFactoryNames;
import static org.springframework.util.ClassUtils.getShortName;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ObjectUtils.containsElement;
import static org.springframework.util.StringUtils.hasText;

/**
 * Bean Registrar
 *
 * @since 1.0.0
 */
public abstract class BeanRegistrar {

    private static final Logger logger = getLogger(BeanRegistrar.class);

    /**
     * Register Infrastructure Bean
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanType the type of bean
     * @return if it's a first time to register, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, ROLE_INFRASTRUCTURE);
        String beanName = generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register Infrastructure Bean
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanName the name of bean
     * @param beanType the type of bean
     * @return if it's a first time to register, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, ROLE_INFRASTRUCTURE);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanType the type of bean
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        String beanName = generateBeanName(beanDefinition, registry);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanName the name of bean
     * @param beanType the type of bean
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry             {@link BeanDefinitionRegistry}
     * @param beanName             the name of bean
     * @param beanType             the type of bean
     * @param constructorArguments the arguments of Bean Classes' constructor
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, Object... constructorArguments) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, constructorArguments);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanName the name of bean
     * @param beanType the type of bean
     * @param role     the role hint for BeanDefinition
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, int role) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType, role);
        return registerBeanDefinition(registry, beanName, beanDefinition);
    }

    /**
     * Register a {@link BeanDefinition} with name if absent
     *
     * @param registry       {@link BeanDefinitionRegistry}
     * @param beanName       the name of bean
     * @param beanDefinition {@link BeanDefinition}
     * @return <code>true</code> if registered, otherwise <code>false</code>
     */
    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition) {
        return registerBeanDefinition(registry, beanName, beanDefinition, false);
    }

    /**
     * Register a {@link BeanDefinition} with name
     *
     * @param registry                      {@link BeanDefinitionRegistry}
     * @param beanName                      the name of bean
     * @param beanDefinition                {@link BeanDefinition}
     * @param allowBeanDefinitionOverriding the {@link BeanDefinition} is allowed to be overridden or not
     * @return <code>true</code> if registered, otherwise <code>false</code>
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

    public static void registerSingleton(SingletonBeanRegistry registry, String beanName, Object bean) {
        registry.registerSingleton(beanName, bean);
        if (logger.isInfoEnabled()) {
            logger.info("The singleton bean [name : '{}' , instance : {}] has been registered into the BeanFactory.", beanName, bean);
        }
    }

    /**
     * Detect the alias is present or not in the given bean name from {@link AliasRegistry}
     *
     * @param registry {@link AliasRegistry}
     * @param beanName the bean name
     * @param alias    alias to test
     * @return if present, return <code>true</code>, or <code>false</code>
     */
    public static boolean hasAlias(AliasRegistry registry, String beanName, String alias) {
        return hasText(beanName) && hasText(alias) && containsElement(registry.getAliases(beanName), alias);
    }

    /**
     * Register the beans from {@link SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader) SpringFactoriesLoader}
     *
     * @param beanFactory    {@link BeanFactory}
     * @param factoryClasses The factory classes to register
     * @return the count of beans that are succeeded to be registered
     */
    public static int registerSpringFactoriesBeans(BeanFactory beanFactory, Class<?>... factoryClasses) {
        return registerSpringFactoriesBeans(asBeanDefinitionRegistry(beanFactory), factoryClasses);
    }

    /**
     * Register the beans from {@link SpringFactoriesLoader#loadFactoryNames(Class, ClassLoader) SpringFactoriesLoader}
     *
     * @param registry       {@link BeanDefinitionRegistry}
     * @param factoryClasses The factory classes to register
     * @return the count of beans that are succeeded to be registered
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

    public static final void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerFactoryBean(registry, beanName, bean, false);
    }

    public static final void registerFactoryBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(DelegatingFactoryBean.class, bean);
        beanDefinition.setSource(bean);
        beanDefinition.setPrimary(primary);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerBean(registry, beanName, bean, false);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        Class beanClass = AopUtils.getTargetClass(bean);
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanClass);
        if (setInstanceSupplier(beanDefinition, () -> bean)) {
            beanDefinition.setPrimary(primary);
            registerBeanDefinition(registry, beanName, beanDefinition);
        } else {
            registerFactoryBean(registry, beanName, bean, primary);
        }
    }
}
