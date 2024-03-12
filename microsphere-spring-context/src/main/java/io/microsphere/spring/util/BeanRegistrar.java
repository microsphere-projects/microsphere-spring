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

import io.microsphere.spring.beans.factory.DelegatingFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.AliasRegistry;
import org.springframework.core.io.support.SpringFactoriesLoader;

import java.util.List;

import static io.microsphere.util.ArrayUtils.EMPTY_OBJECT_ARRAY;
import static io.microsphere.util.ArrayUtils.length;
import static java.beans.Introspector.decapitalize;
import static java.lang.String.format;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
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

    private static final Logger logger = LoggerFactory.getLogger(BeanRegistrar.class);

    /**
     * Register Infrastructure Bean
     *
     * @param beanDefinitionRegistry {@link BeanDefinitionRegistry}
     * @param beanType               the type of bean
     * @return if it's a first time to register, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry beanDefinitionRegistry, Class<?> beanType) {
        String beanName = decapitalize(beanType.getSimpleName());
        return registerBeanDefinition(beanDefinitionRegistry, beanName, beanType, ROLE_INFRASTRUCTURE);
    }

    /**
     * Register Infrastructure Bean
     *
     * @param beanDefinitionRegistry {@link BeanDefinitionRegistry}
     * @param beanName               the name of bean
     * @param beanType               the type of bean
     * @return if it's a first time to register, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerInfrastructureBean(BeanDefinitionRegistry beanDefinitionRegistry, String beanName, Class<?> beanType) {
        return registerBeanDefinition(beanDefinitionRegistry, beanName, beanType, ROLE_INFRASTRUCTURE);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry {@link BeanDefinitionRegistry}
     * @param beanType the type of bean
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    public static boolean registerBeanDefinition(BeanDefinitionRegistry registry, Class<?> beanType) {
        String beanName = decapitalize(beanType.getSimpleName());
        return registerBeanDefinition(registry, beanName, beanType, ROLE_APPLICATION);
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
        return registerBeanDefinition(registry, beanName, beanType, EMPTY_OBJECT_ARRAY);
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
        return registerBeanDefinition(registry, beanName, beanType, ROLE_APPLICATION, constructorArguments);
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
        return registerBeanDefinition(registry, beanName, beanType, role, EMPTY_OBJECT_ARRAY);
    }

    /**
     * Register {@link BeanDefinition}
     *
     * @param registry             {@link BeanDefinitionRegistry}
     * @param beanName             the name of bean
     * @param beanType             the type of bean
     * @param role                 the role hint for BeanDefinition
     * @param constructorArguments the arguments of Bean Classes' constructor
     * @return if the named {@link BeanDefinition} is not registered, return <code>true</code>, or <code>false</code>
     */
    static boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?> beanType, int role, Object... constructorArguments) {

        boolean registered = false;

        if (registry.containsBeanDefinition(beanName)) {
            BeanDefinition oldBeanDefinition = registry.getBeanDefinition(beanName);
            if (logger.isWarnEnabled()) {
                logger.warn("The bean[name : '{}'] definition [{}] was registered!", beanName, oldBeanDefinition);
            }
        } else {
            BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanType)
                    .setRole(role);
            // Add the arguments of constructor if present
            int length = length(constructorArguments);
            for (int i = 0; i < length; i++) {
                Object constructorArgument = constructorArguments[i];
                beanDefinitionBuilder.addConstructorArgValue(constructorArgument);
            }
            AbstractBeanDefinition beanDefinition = beanDefinitionBuilder.getBeanDefinition();
            registered = registerBeanDefinition(registry, beanName, beanDefinition);
        }

        return registered;
    }

    public static final boolean registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, BeanDefinition beanDefinition) {
        try {
            registry.registerBeanDefinition(beanName, beanDefinition);
            if (logger.isDebugEnabled()) {
                logger.debug("The bean[name : '{}' , role : {}] definition [{}] has been registered.", beanName, beanDefinition.getRole(), beanDefinition);
            }
        } catch (BeanDefinitionStoreException e) {
            if (logger.isErrorEnabled()) {
                logger.error("The bean[name : '{}' , role : {}] definition [{}] can't be registered ", beanName, beanDefinition.getRole(), e);
            }
            return false;
        }
        return true;
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
     * @param registry       {@link BeanDefinitionRegistry}
     * @param factoryClasses The factory classes to register
     * @return the count of beans that are succeeded to be registered
     * @since 1.0.0
     */
    public static int registerSpringFactoriesBeans(BeanDefinitionRegistry registry, Class<?>... factoryClasses) {
        int count = 0;

        ClassLoader classLoader = registry.getClass().getClassLoader();

        for (int i = 0; i < factoryClasses.length; i++) {
            Class<?> factoryClass = factoryClasses[i];
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
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(DelegatingFactoryBean.class)
                .addConstructorArgValue(bean)
                .getBeanDefinition();
        beanDefinition.setSource(bean);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean) {
        registerBean(registry, beanName, bean, false);
    }

    public static void registerBean(BeanDefinitionRegistry registry, String beanName, Object bean, boolean primary) {
        Class beanClass = AopUtils.getTargetClass(bean);
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(beanClass, () -> bean).getBeanDefinition();
        beanDefinition.setPrimary(primary);
        registerBeanDefinition(registry, beanName, beanDefinition);
    }
}
