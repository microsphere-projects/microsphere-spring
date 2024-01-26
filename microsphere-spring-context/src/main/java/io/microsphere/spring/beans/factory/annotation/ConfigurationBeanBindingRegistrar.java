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
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.beans.factory.support.ConfigurationBeanAliasGenerator;
import io.microsphere.spring.util.PropertySourcesUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.*;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.spring.beans.factory.annotation.ConfigurationBeanBindingPostProcessor.initBeanMetadataAttributes;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_INVALID_FIELDS;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_IGNORE_UNKNOWN_FIELDS;
import static io.microsphere.spring.beans.factory.annotation.EnableConfigurationBeanBinding.DEFAULT_MULTIPLE;
import static io.microsphere.spring.util.AnnotationUtils.getAttribute;
import static io.microsphere.spring.util.AnnotationUtils.getRequiredAttribute;
import static io.microsphere.spring.util.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.util.BeanRegistrar.registerInfrastructureBean;
import static io.microsphere.spring.util.PropertySourcesUtils.getSubProperties;
import static io.microsphere.spring.util.PropertySourcesUtils.normalizePrefix;
import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.loadFactories;
import static java.lang.Boolean.valueOf;
import static java.util.Collections.singleton;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The {@link ImportBeanDefinitionRegistrar} implementation for {@link EnableConfigurationBeanBinding @EnableConfigurationBinding}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConfigurationBeanBindingRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware,
        BeanFactoryAware {

    final static Class ENABLE_CONFIGURATION_BINDING_CLASS = EnableConfigurationBeanBinding.class;

    private final static String ENABLE_CONFIGURATION_BINDING_CLASS_NAME = ENABLE_CONFIGURATION_BINDING_CLASS.getName();

    private final Log log = LogFactory.getLog(getClass());

    private ConfigurableEnvironment environment;
    private BeanFactory beanFactory;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        Map<String, Object> attributes = metadata.getAnnotationAttributes(ENABLE_CONFIGURATION_BINDING_CLASS_NAME);

        registerConfigurationBeanDefinitions(attributes, registry);
        registerRefreshableConfigurationBeanRepository(registry);
    }

    public void registerConfigurationBeanDefinitions(Map<String, Object> attributes, BeanDefinitionRegistry registry) {

        String prefix = getRequiredAttribute(attributes, "prefix");

        prefix = environment.resolvePlaceholders(prefix);

        Class<?> configClass = getRequiredAttribute(attributes, "type");

        boolean multiple = getAttribute(attributes, "multiple", valueOf(DEFAULT_MULTIPLE));

        boolean ignoreUnknownFields = getAttribute(attributes, "ignoreUnknownFields", valueOf(DEFAULT_IGNORE_UNKNOWN_FIELDS));

        boolean ignoreInvalidFields = getAttribute(attributes, "ignoreInvalidFields", valueOf(DEFAULT_IGNORE_INVALID_FIELDS));

        EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy refreshStrategy = getRequiredAttribute(attributes, "refreshStrategy");

        registerConfigurationBeans(prefix, configClass, multiple, ignoreUnknownFields, ignoreInvalidFields, refreshStrategy, registry);
    }


    private void registerConfigurationBeans(String prefix, Class<?> configClass, boolean multiple,
                                            boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                                            EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy refreshStrategy,
                                            BeanDefinitionRegistry registry) {

        Map<String, Object> configurationProperties = PropertySourcesUtils.getSubProperties(environment.getPropertySources(), environment, prefix);

        Set<String> beanNames = multiple ? resolveMultipleBeanNames(configurationProperties) :
                singleton(resolveSingleBeanName(configurationProperties, configClass, registry));

        for (String beanName : beanNames) {
            registerConfigurationBean(beanName, configClass, multiple, prefix, ignoreUnknownFields, ignoreInvalidFields, refreshStrategy,
                    configurationProperties, registry);

            registerConfigurationBeanAlias(beanName, configClass, prefix, registry);
        }

        registerConfigurationBindingBeanPostProcessor(registry);
    }


    private void registerConfigurationBeanAlias(String beanName, Class<?> configClass, String prefix, BeanDefinitionRegistry registry) {
        List<ConfigurationBeanAliasGenerator> configurationBeanAliasGenerators = loadFactories(ConfigurationBeanAliasGenerator.class, beanFactory);
        configurationBeanAliasGenerators.forEach(aliasGenerator -> {
            String alias = aliasGenerator.generateAlias(prefix, beanName, configClass);
            registry.registerAlias(beanName, alias);
        });

    }

    private void registerConfigurationBean(String beanName, Class<?> configClass, boolean multiple, String prefix,                                       
                                           boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                                           EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy refreshStrategy,
                                           Map<String, Object> configurationProperties,
                                           BeanDefinitionRegistry registry) {

        BeanDefinitionBuilder builder = rootBeanDefinition(configClass);

        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

        setSource(beanDefinition);

        Map<String, Object> subProperties = resolveSubProperties(multiple, beanName, environment, configurationProperties);

        initBeanMetadataAttributes(beanDefinition, multiple, prefix, subProperties, ignoreUnknownFields, ignoreInvalidFields, refreshStrategy);

        registry.registerBeanDefinition(beanName, beanDefinition);

        if (log.isInfoEnabled()) {
            log.info("The configuration bean definition [name : " + beanName + ", content : " + beanDefinition
                    + "] has been registered.");
        }
    }

    static Map<String, Object> resolveSubProperties(boolean multiple, String beanName,
                                                    PropertyResolver propertyResolver,
                                                     Map<String, Object> configurationProperties) {
        if (!multiple) {
            return configurationProperties;
        }

        MutablePropertySources propertySources = new MutablePropertySources();

        propertySources.addLast(new MapPropertySource("_", configurationProperties));

        return getSubProperties(propertySources, propertyResolver, normalizePrefix(beanName));
    }

    private void setSource(AbstractBeanDefinition beanDefinition) {
        beanDefinition.setSource(ENABLE_CONFIGURATION_BINDING_CLASS);
    }

    private void registerConfigurationBindingBeanPostProcessor(BeanDefinitionRegistry registry) {
        registerInfrastructureBean(registry, ConfigurationBeanBindingPostProcessor.BEAN_NAME,
                ConfigurationBeanBindingPostProcessor.class);
    }

    protected void registerRefreshableConfigurationBeanRepository(BeanDefinitionRegistry registry) {
        registerBeanDefinition(registry, RefreshableConfigurationBeans.BEAN_NAME,
                RefreshableConfigurationBeans.class);
    }

    @Override
    public void setEnvironment(Environment environment) {

        Assert.isInstanceOf(ConfigurableEnvironment.class, environment);

        this.environment = (ConfigurableEnvironment) environment;

    }

    private Set<String> resolveMultipleBeanNames(Map<String, Object> properties) {

        Set<String> beanNames = new LinkedHashSet<String>();

        for (String propertyName : properties.keySet()) {

            int index = propertyName.indexOf(".");

            if (index > 0) {

                String beanName = propertyName.substring(0, index);

                beanNames.add(beanName);
            }

        }

        return beanNames;

    }

    private String resolveSingleBeanName(Map<String, Object> properties, Class<?> configClass,
                                         BeanDefinitionRegistry registry) {

        String beanName = (String) properties.get("id");

        if (!StringUtils.hasText(beanName)) {
            BeanDefinitionBuilder builder = rootBeanDefinition(configClass);
            beanName = BeanDefinitionReaderUtils.generateBeanName(builder.getRawBeanDefinition(), registry);
        }

        return beanName;

    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
}
