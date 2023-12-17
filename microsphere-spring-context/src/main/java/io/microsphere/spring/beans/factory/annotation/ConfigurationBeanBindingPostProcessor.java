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

import io.microsphere.spring.context.config.ConfigurationBeanBinder;
import io.microsphere.spring.context.config.ConfigurationBeanCustomizer;
import io.microsphere.spring.context.config.DefaultConfigurationBeanBinder;
import io.microsphere.spring.core.convert.support.ConversionServiceResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.convert.ConversionService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static io.microsphere.spring.beans.factory.annotation.ConfigurationBeanBindingRegistrar.ENABLE_CONFIGURATION_BINDING_CLASS;
import static io.microsphere.spring.context.config.ConfigurationBeanBinder.*;
import static io.microsphere.spring.util.WrapperUtils.unwrap;
import static org.springframework.beans.factory.BeanFactoryUtils.beansOfTypeIncludingAncestors;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.sort;
import static org.springframework.util.ClassUtils.getUserClass;
import static org.springframework.util.ObjectUtils.nullSafeEquals;

/**
 * The {@link BeanPostProcessor} class to bind the configuration bean
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public class ConfigurationBeanBindingPostProcessor implements BeanPostProcessor, BeanFactoryAware, PriorityOrdered {

    /**
     * The bean name of {@link ConfigurationBeanBindingPostProcessor}
     */
    public static final String BEAN_NAME = "configurationBeanBindingPostProcessor";

    private final Log log = LogFactory.getLog(getClass());

    private ConfigurableListableBeanFactory beanFactory = null;

    private ConfigurationBeanBinder configurationBeanBinder = null;

    private List<ConfigurationBeanCustomizer> configurationBeanCustomizers = null;

    private volatile RefreshableConfigurationBeans beanRepository = null;

    private int order = LOWEST_PRECEDENCE;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        BeanDefinition beanDefinition = getNullableBeanDefinition(beanName);

        if (isConfigurationBean(bean, beanDefinition)) {
            bindConfigurationBean(beanDefinition, bean);
            customize(beanName, bean);
            registerConfigurationBean(beanName, bean, beanDefinition);
        }

        return bean;
    }

    private void registerConfigurationBean(String beanName, Object bean, BeanDefinition beanDefinition) {
        getConfigurationBeanRepository().registerConfigurationBean(beanName, bean, beanDefinition);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    /**
     * Set the order for current instance
     *
     * @param order the order
     */
    public void setOrder(int order) {
        this.order = order;
    }

    public ConfigurationBeanBinder getConfigurationBeanBinder() {
        if (configurationBeanBinder == null) {
            initConfigurationBeanBinder();
        }
        return configurationBeanBinder;
    }

    public RefreshableConfigurationBeans getConfigurationBeanRepository() {
        if (this.beanRepository == null) {
            initConfigurationBeanRepository();
        }
        return this.beanRepository;
    }

    public void setConfigurationBeanBinder(ConfigurationBeanBinder configurationBeanBinder) {
        this.configurationBeanBinder = configurationBeanBinder;
    }

    /**
     * Get the {@link List} of {@link ConfigurationBeanCustomizer ConfigurationBeanCustomizers}
     *
     * @return non-null
     * @since 1.0.0
     */
    public List<ConfigurationBeanCustomizer> getConfigurationBeanCustomizers() {
        if (configurationBeanCustomizers == null) {
            initBindConfigurationBeanCustomizers();
        }
        return configurationBeanCustomizers;
    }

    public void setConfigurationBeanCustomizers(Collection<ConfigurationBeanCustomizer> configurationBeanCustomizers) {
        List<ConfigurationBeanCustomizer> customizers = new ArrayList<ConfigurationBeanCustomizer>(configurationBeanCustomizers);
        sort(customizers);
        this.configurationBeanCustomizers = Collections.unmodifiableList(customizers);
    }

    private BeanDefinition getNullableBeanDefinition(String beanName) {
        return beanFactory.containsBeanDefinition(beanName) ? beanFactory.getBeanDefinition(beanName) : null;
    }

    private boolean isConfigurationBean(Object bean, BeanDefinition beanDefinition) {
        return beanDefinition != null && ENABLE_CONFIGURATION_BINDING_CLASS.equals(beanDefinition.getSource()) && nullSafeEquals(getBeanClassName(bean), beanDefinition.getBeanClassName());
    }

    private String getBeanClassName(Object bean) {
        return getUserClass(bean.getClass()).getName();
    }

    private void bindConfigurationBean(BeanDefinition beanDefinition, Object configurationBean) {
        Map<String, Object> configurationProperties = getConfigurationProperties(beanDefinition);
        getConfigurationBeanBinder().bind(configurationProperties, beanDefinition, configurationBean);

        if (log.isInfoEnabled()) {
            log.info("The configuration bean [" + configurationBean + "] have been binding by the " + "configuration properties [" + configurationProperties + "]");
        }
    }

    private void initConfigurationBeanBinder() {
        ConfigurationBeanBinder configurationBeanBinder = this.configurationBeanBinder;
        if (configurationBeanBinder == null) {
            try {
                configurationBeanBinder = beanFactory.getBean(ConfigurationBeanBinder.class);
            } catch (BeansException ignored) {
                if (log.isInfoEnabled()) {
                    log.info("configurationBeanBinder Bean can't be found in ApplicationContext.");
                }
                // Use Default implementation
                configurationBeanBinder = defaultConfigurationBeanBinder();
            }
        }

        ConversionService conversionService = new ConversionServiceResolver(beanFactory).resolve();
        configurationBeanBinder.setConversionService(conversionService);

        this.configurationBeanBinder = configurationBeanBinder;
    }

    private void initConfigurationBeanRepository() {
        RefreshableConfigurationBeans beanRepository = this.beanRepository;
        if (beanRepository == null) {
            try {
                beanRepository = beanFactory.getBean(RefreshableConfigurationBeans.class);
            } catch (BeansException ex) {
                if (log.isInfoEnabled()) {
                    log.info("refreshableConfigurationBeanRepository Bean can't be found in ApplicationContext.");
                }
                throw ex;
            }
        }

        this.beanRepository = beanRepository;
    }

    private void initBindConfigurationBeanCustomizers() {
        Collection<ConfigurationBeanCustomizer> customizers = beansOfTypeIncludingAncestors(beanFactory, ConfigurationBeanCustomizer.class).values();
        setConfigurationBeanCustomizers(customizers);
    }

    private void customize(String beanName, Object configurationBean) {
        for (ConfigurationBeanCustomizer customizer : getConfigurationBeanCustomizers()) {
            customizer.customize(beanName, configurationBean);
        }
    }

    /**
     * Create {@link ConfigurationBeanBinder} instance.
     *
     * @return {@link DefaultConfigurationBeanBinder}
     */
    private ConfigurationBeanBinder defaultConfigurationBeanBinder() {
        return new DefaultConfigurationBeanBinder();
    }

    static void initBeanMetadataAttributes(AbstractBeanDefinition beanDefinition, boolean multiple, String prefix,
                                           Map<String, Object> configurationProperties,
                                           boolean ignoreUnknownFields, boolean ignoreInvalidFields,
                                           EnableConfigurationBeanBinding.ConfigurationBeanRefreshStrategy refreshStrategy) {
        beanDefinition.setAttribute(CONFIGURATION_PREFIX_ATTRIBUTE_NAME, prefix);
        beanDefinition.setAttribute(USING_MULTIPLE_CONFIGURATION_ATTRIBUTE_NAME, multiple);
        beanDefinition.setAttribute(CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME, configurationProperties);
        beanDefinition.setAttribute(IGNORE_UNKNOWN_FIELDS_ATTRIBUTE_NAME, ignoreUnknownFields);
        beanDefinition.setAttribute(IGNORE_INVALID_FIELDS_ATTRIBUTE_NAME, ignoreInvalidFields);
        beanDefinition.setAttribute(CONFIGURATION_BEAN_REFRESH_STRATEGY, refreshStrategy);
    }

    private static <T> T getAttribute(BeanDefinition beanDefinition, String attributeName) {
        return (T) beanDefinition.getAttribute(attributeName);
    }

    private static Map<String, Object> getConfigurationProperties(BeanDefinition beanDefinition) {
        return getAttribute(beanDefinition, CONFIGURATION_PROPERTIES_ATTRIBUTE_NAME);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = unwrap(beanFactory);
    }

    @Override
    public int getOrder() {
        return order;
    }
}
