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
package io.microsphere.spring.redis.annotation;

import io.microsphere.spring.redis.beans.RedisConnectionFactoryWrapperBeanPostProcessor;
import io.microsphere.spring.redis.beans.RedisTemplateWrapperBeanPostProcessor;
import io.microsphere.spring.redis.beans.WrapperProcessors;
import io.microsphere.spring.redis.interceptor.EventPublishingRedisCommandInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;
import static org.springframework.util.CollectionUtils.isEmpty;
import static org.springframework.util.StringUtils.commaDelimitedListToSet;
import static org.springframework.util.StringUtils.hasText;
import static org.springframework.util.StringUtils.trimWhitespace;


/**
 * Redis Interceptor {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableRedisInterceptor
 * @since 1.0.0
 */
public class RedisInterceptorBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    private static final Logger logger = LoggerFactory.getLogger(RedisInterceptorBeanDefinitionRegistrar.class);

    private ConfigurableEnvironment environment;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        Map<String, Object> attributes = importingClassMetadata.getAnnotationAttributes(EnableRedisInterceptor.class.getName());
        String[] wrapRedisTemplates = (String[]) attributes.get("wrapRedisTemplates");

        boolean exposeCommandEvent = (boolean) attributes.get("exposeCommandEvent");

        Set<String> wrapRedisTemplateBeanNames = resolveWrappedRedisTemplateBeanNames(wrapRedisTemplates);

        registerBeanDefinitions(wrapRedisTemplateBeanNames, exposeCommandEvent, registry);
    }

    public void registerBeanDefinitions(Set<String> wrappedRedisTemplateBeanNames, boolean exposedCommandEvent, BeanDefinitionRegistry registry) {

        if (isEmpty(wrappedRedisTemplateBeanNames)) {
            registerRedisConnectionFactoryWrapperBeanPostProcessor(registry);
        } else {
            registerRedisTemplateWrapperBeanPostProcessor(wrappedRedisTemplateBeanNames, registry);
        }

        registerWrapperProcessors(registry);

        if (exposedCommandEvent) {
            registerEventPublishingRedisCommendInterceptor(registry);
        }
    }

    private void registerRedisTemplateWrapperBeanPostProcessor(Set<String> wrappedRedisTemplateBeanNames, BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisTemplateWrapperBeanPostProcessor.BEAN_NAME, RedisTemplateWrapperBeanPostProcessor.class, wrappedRedisTemplateBeanNames);

    }

    private Set<String> resolveWrappedRedisTemplateBeanNames(String[] wrapRedisTemplates) {
        Set<String> wrappedRedisTemplateBeanNames = new LinkedHashSet<>();
        for (String wrapRedisTemplate : wrapRedisTemplates) {
            String wrappedRedisTemplateBeanName = environment.resolveRequiredPlaceholders(wrapRedisTemplate);
            Set<String> beanNames = commaDelimitedListToSet(wrappedRedisTemplateBeanName);
            for (String beanName : beanNames) {
                wrappedRedisTemplateBeanName = trimWhitespace(beanName);
                if (hasText(wrappedRedisTemplateBeanName)) {
                    wrappedRedisTemplateBeanNames.add(wrappedRedisTemplateBeanName);
                }
            }
        }
        return wrappedRedisTemplateBeanNames;
    }

    private void registerWrapperProcessors(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, WrapperProcessors.BEAN_NAME, WrapperProcessors.class);
    }

    private void registerRedisConnectionFactoryWrapperBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RedisConnectionFactoryWrapperBeanPostProcessor.BEAN_NAME, RedisConnectionFactoryWrapperBeanPostProcessor.class);
    }

    private void registerEventPublishingRedisCommendInterceptor(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, EventPublishingRedisCommandInterceptor.BEAN_NAME, EventPublishingRedisCommandInterceptor.class);
    }

    private void registerBeanDefinition(BeanDefinitionRegistry registry, String beanName, Class<?>
            beanClass, Object... constructorArgs) {
        if (!registry.containsBeanDefinition(beanName)) {
            BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanClass);
            for (Object constructorArg : constructorArgs) {
                beanDefinitionBuilder.addConstructorArgValue(constructorArg);
            }
            registry.registerBeanDefinition(beanName, beanDefinitionBuilder.getBeanDefinition());
            logger.debug("Redis Interceptor Component[name : '{}' , class : {} , args : {}] registered", beanName, beanClass, asList(constructorArgs));
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }
}
