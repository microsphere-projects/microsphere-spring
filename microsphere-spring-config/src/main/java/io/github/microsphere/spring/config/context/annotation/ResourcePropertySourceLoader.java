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

import io.github.microsphere.spring.config.env.support.YamlPropertySourceFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ObjectUtils;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

/**
 * The Internal {@link AnnotatedPropertySourceLoader} Class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see YamlPropertySourceFactory
 * @since 1.0.0
 */
class ResourcePropertySourceLoader extends ExtendablePropertySourceLoader<ResourcePropertySource> implements
        ResourceLoaderAware, BeanClassLoaderAware {

    private ResourcePatternResolver resourcePatternResolver;

    private ClassLoader classLoader;

    @Override
    protected PropertySource<?> loadPropertySource(PropertySourceExtensionAttributes<ResourcePropertySource> attributes,
                                                   String propertySourceName, AnnotationMetadata metadata) throws Throwable {
        String[] resourceLocations = attributes.getStringArray("value");
        boolean ignoreResourceNotFound = attributes.getBoolean("ignoreResourceNotFound");

        if (ObjectUtils.isEmpty(resourceLocations)) {
            if (ignoreResourceNotFound) {
                return null;
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at the annotation : @" + getAnnotationType().getName());
        }

        String encoding = attributes.getString("encoding");
        Comparator<Resource> resourceComparator = createInstance(attributes, "resourceComparator");
        PropertySourceFactory factory = createInstance(attributes, "factory");

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);

        for (String resourceLocation : resourceLocations) {
            Resource[] resources = null;

            try {
                resources = resourcePatternResolver.getResources(resourceLocation);
            } catch (IOException e) {
                if (!ignoreResourceNotFound) {
                    throw e;
                }
            }

            // sort
            Arrays.sort(resources, resourceComparator);
            // iterate
            int length = resources.length;
            for (int i = 0; i < length; i++) {
                Resource resource = resources[i];
                EncodedResource encodedResource = new EncodedResource(resource, encoding);
                String name = propertySourceName + "#" + i;
                PropertySource propertySource = factory.createPropertySource(name, encodedResource);
                compositePropertySource.addPropertySource(propertySource);
            }
        }
        return compositePropertySource;
    }

    private <T> T createInstance(AnnotationAttributes attributes, String attributeName) {
        Class<T> type = (Class<T>) attributes.getClass(attributeName);
        return BeanUtils.instantiateClass(type);
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourcePatternResolver = ResourcePatternUtils.getResourcePatternResolver(resourceLoader);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}
