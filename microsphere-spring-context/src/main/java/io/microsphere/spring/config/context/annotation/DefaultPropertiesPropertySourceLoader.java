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
package io.microsphere.spring.config.context.annotation;

import io.microsphere.spring.context.annotation.BeanCapableImportCandidate;
import io.microsphere.spring.util.AnnotationUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

import static io.microsphere.spring.config.context.annotation.DefaultPropertiesPropertySourceLoader.DefaultPropertiesPropertySourceProcessor.getBeanDefinition;
import static io.microsphere.spring.util.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static io.microsphere.spring.util.PropertySourcesUtils.getDefaultProperties;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.rootBeanDefinition;

/**
 * The Loader class for {@link DefaultPropertiesPropertySource @DefaultPropertiesPropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultPropertiesPropertySource
 * @see ResourcePropertySourceLoader
 * @since 1.0.0
 */
final class DefaultPropertiesPropertySourceLoader extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        // The property source name of current single @DefaultPropertiesPropertySource
        String propertySourceName = DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME + "@" + hashCode();
        PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes = buildExtensionAttributes(metadata);

        ResourcePropertySourceLoader delegate = getDelegate();

        try {
            PropertySource<?> propertySource = delegate.loadPropertySource(extensionAttributes, propertySourceName);
            BeanDefinition beanDefinition = getBeanDefinition(registry);
            // AttributeAccessorSupport#attributes is a LinkedHashMap instance that ensures the insertion order, that means
            // the order is based on the loading order of @DefaultPropertiesPropertySource
            beanDefinition.setAttribute(propertySourceName, propertySource);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private PropertySourceExtensionAttributes<ResourcePropertySource> buildExtensionAttributes(AnnotationMetadata metadata) {
        AnnotationAttributes attributes = AnnotationUtils.getAnnotationAttributes(metadata, DefaultPropertiesPropertySource.class);
        return new PropertySourceExtensionAttributes(attributes, ResourcePropertySource.class, this.getEnvironment());
    }

    private ResourcePropertySourceLoader getDelegate() {
        ResourcePropertySourceLoader delegate = new ResourcePropertySourceLoader();
        delegate.setEnvironment(getEnvironment());
        delegate.setBeanFactory(getBeanFactory());
        delegate.setResourceLoader(getResourceLoader());
        delegate.setBeanClassLoader(getClassLoader());
        return delegate;
    }

    static class DefaultPropertiesPropertySourceProcessor implements BeanDefinitionRegistryPostProcessor, PriorityOrdered {

        static final String BEAN_NAME = "defaultPropertiesPropertySourceProcessor";

        private final ConfigurableEnvironment environment;

        DefaultPropertiesPropertySourceProcessor(ConfigurableEnvironment environment) {
            this.environment = environment;
        }

        @Override
        public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
            BeanDefinition beanDefinition = getBeanDefinition(registry);
            mergeDefaultProperties(beanDefinition);
        }

        private void mergeDefaultProperties(BeanDefinition beanDefinition) {

            Map<String, Object> defaultProperties = getDefaultProperties(this.environment);

            for (String attributeName : beanDefinition.attributeNames()) {
                Object attribute = beanDefinition.getAttribute(attributeName);
                if (attribute instanceof EnumerablePropertySource) {
                    EnumerablePropertySource propertySource = (EnumerablePropertySource) attribute;
                    for (String propertyName : propertySource.getPropertyNames()) {
                        Object propertyValue = propertySource.getProperty(propertyName);
                        defaultProperties.put(propertyName, propertyValue);
                    }
                }
            }
        }

        @Override
        public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        }

        @Override
        public int getOrder() {
            return HIGHEST_PRECEDENCE;
        }

        static BeanDefinition getBeanDefinition(BeanDefinitionRegistry registry) {
            BeanDefinition beanDefinition = registry.getBeanDefinition(BEAN_NAME);
            if (beanDefinition == null) {
                beanDefinition = rootBeanDefinition(DefaultPropertiesPropertySourceProcessor.class).getBeanDefinition();
                registry.registerBeanDefinition(BEAN_NAME, beanDefinition);
            }
            return beanDefinition;
        }
    }
}
