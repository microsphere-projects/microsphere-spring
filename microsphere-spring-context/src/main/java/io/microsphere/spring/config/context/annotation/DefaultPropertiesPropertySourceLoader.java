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
import io.microsphere.spring.util.PropertySourcesUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;

import static io.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.spring.util.PropertySourcesUtils.DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME;
import static io.microsphere.spring.util.PropertySourcesUtils.getDefaultProperties;

/**
 * The Loader class for {@link DefaultPropertiesPropertySource @DefaultPropertiesPropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see DefaultPropertiesPropertySource
 * @see ResourcePropertySourceLoader
 * @since 1.0.0
 */
class DefaultPropertiesPropertySourceLoader extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {

        AnnotationAttributes attributes = getAnnotationAttributes(metadata, DefaultPropertiesPropertySource.class);

        loadPropertySource(attributes, registry);
    }

    /**
     * Load a {@link PropertySource} as a segment of {@link PropertySourcesUtils#DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME "defaultProperties"}
     * {@link PropertySource}
     *
     * @param attributes {@link AnnotationAttributes}
     * @param registry   {@link BeanDefinitionRegistry}
     */
    protected void loadPropertySource(AnnotationAttributes attributes, BeanDefinitionRegistry registry) {
        // The property source name of current single @DefaultPropertiesPropertySource
        String propertySourceName = DEFAULT_PROPERTIES_PROPERTY_SOURCE_NAME + "@" + hashCode();
        // Reuse ResourcePropertySourceLoader
        ResourcePropertySourceLoader delegate = getDelegate();

        Map<String, Object> defaultProperties = getDefaultProperties(this.environment);

        PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttributes = buildExtensionAttributes(attributes);
        try {
            PropertySource<?> propertySource = delegate.loadPropertySource(extensionAttributes, propertySourceName);
            if (propertySource instanceof EnumerablePropertySource) {
                EnumerablePropertySource enumerablePropertySource = (EnumerablePropertySource) propertySource;
                for (String propertyName : enumerablePropertySource.getPropertyNames()) {
                    Object propertyValue = propertySource.getProperty(propertyName);
                    defaultProperties.put(propertyName, propertyValue);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private PropertySourceExtensionAttributes<ResourcePropertySource> buildExtensionAttributes(AnnotationAttributes attributes) {
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
}
