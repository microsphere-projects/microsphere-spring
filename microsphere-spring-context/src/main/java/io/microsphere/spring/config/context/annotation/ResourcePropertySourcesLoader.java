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

import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

import static io.microsphere.spring.core.annotation.GenericAnnotationAttributes.ofSet;

/**
 * The loader of {@link PropertySource} for {@link ResourcePropertySources}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySources
 * @see ResourcePropertySource
 * @see ResourcePropertySourceLoader
 * @see AnnotatedPropertySourceLoader
 * @since 1.0.0
 */
class ResourcePropertySourcesLoader extends AnnotatedPropertySourceLoader<ResourcePropertySources> implements
        ResourceLoaderAware, BeanClassLoaderAware {

    /**
     * Loads property sources from each {@link ResourcePropertySource} annotation declared
     * within the {@link ResourcePropertySources} container annotation. Each individual
     * {@link ResourcePropertySource} is delegated to a {@link ResourcePropertySourceLoader}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Used implicitly via the @ResourcePropertySources annotation:
     *   @ResourcePropertySources({
     *       @ResourcePropertySource(value = {"classpath*:/META-INF/test/*.properties"}),
     *       @ResourcePropertySource(value = {"classpath*:/META-INF/test/*.properties"})
     *   })
     *   public class AppConfig { }
     *
     *   // After loading, properties from the resources are available:
     *   assertEquals("1", environment.getProperty("a"));
     * }</pre>
     *
     * @param attributes         the annotation attributes of {@link ResourcePropertySources}
     * @param metadata           the annotation metadata of the importing class
     * @param propertySourceName the resolved name for the property source
     * @param propertySources    the {@link MutablePropertySources} to add loaded sources to
     * @throws Throwable if an error occurs during property source loading
     */
    @Override
    protected void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata, String propertySourceName,
                                      MutablePropertySources propertySources) throws Throwable {

        AnnotationAttributes[] annotationAttributesArray = attributes.getAnnotationArray("value");

        Set<AnnotationAttributes> attributesSet = ofSet(annotationAttributesArray);

        ResourcePropertySourceLoader delegate = getDelegate();

        for (AnnotationAttributes elementAttributes : attributesSet) {
            String name = resolvePropertySourceName(elementAttributes, metadata);
            delegate.loadPropertySource(elementAttributes, metadata, name, propertySources);
        }
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
