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
package io.github.microsphere.spring.config.context.annotation;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;

/**
 * Abstract {@link ImportSelector} class to load the {@link PropertySource PropertySource}
 * when the {@link Configuration configuration} annotated the Enable annotation that meta-annotates {@link PropertySourceExtension @PropertySourceExtension}
 *
 * @param <A> The type of {@link Annotation} must meta-annotate {@link PropertySourceExtension}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @see PropertySourceExtensionAttributes
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class ExtendablePropertySourceLoader<A extends Annotation> extends AnnotatedPropertySourceLoader<A> {

    @Override
    protected final void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                            String propertySourceName, MutablePropertySources propertySources) throws Throwable {
        Class<A> annotationType = getAnnotationType();
        PropertySourceExtensionAttributes<A> propertySourceExtensionAttributes = new PropertySourceExtensionAttributes(attributes, getEnvironment());
        PropertySource<?> propertySource = loadPropertySource(propertySourceExtensionAttributes, propertySourceName, metadata);
        if (propertySource == null) {
            logger.warn("The PropertySource[annotationType : '{}' , configuration : '{}'] can't be loaded", annotationType.getName(), metadata.getClassName());
        } else {
            if (propertySourceExtensionAttributes.isFirstPropertySource()) {
                propertySources.addFirst(propertySource);
            } else {
                String relativePropertySourceName = propertySourceExtensionAttributes.getAfterPropertySourceName();
                if (StringUtils.hasText(relativePropertySourceName)) {
                    propertySources.addAfter(relativePropertySourceName, propertySource);
                } else {
                    relativePropertySourceName = propertySourceExtensionAttributes.getBeforePropertySourceName();
                }
                if (StringUtils.hasText(relativePropertySourceName)) {
                    propertySources.addBefore(relativePropertySourceName, propertySource);
                } else {
                    propertySources.addLast(propertySource);
                }
            }
        }
    }

    /**
     * Load the {@link PropertySource}
     *
     * @param attributes         {@link PropertySourceExtensionAttributes}
     * @param propertySourceName the name of {@link PropertySource}
     * @param metadata           {@link AnnotationMetadata}
     * @return <code>null</code> if the {@link PropertySource} can't be loaded
     * @throws Throwable the failure of the loading
     */
    @Nullable
    protected abstract PropertySource<?> loadPropertySource(PropertySourceExtensionAttributes<A> attributes,
                                                            String propertySourceName, AnnotationMetadata metadata) throws Throwable;

}
