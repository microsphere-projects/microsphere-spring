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

import io.microsphere.spring.config.env.support.YamlPropertySourceFactory;
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

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 * The Internal {@link AnnotatedPropertySourceLoader} Class for {@link ResourcePropertySource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourcePropertySource
 * @see ResourcePropertySourceAttributes
 * @see YamlPropertySourceFactory
 * @since 1.0.0
 */
public class ResourcePropertySourceLoader<A extends Annotation, EA extends ResourcePropertySourceAttributes<A>>
        extends ExtendablePropertySourceLoader<A, EA> implements ResourceLoaderAware, BeanClassLoaderAware {

    private ResourcePatternResolver resourcePatternResolver;

    private ClassLoader classLoader;

    @Override
    protected Class<A> resolveAnnotationType() {
        Class<A> annotationType = super.resolveAnnotationType();
        if (PropertySourceExtension.class.equals(annotationType)) {
            annotationType = (Class<A>) ResourcePropertySource.class;
        }
        return annotationType;
    }

    @Override
    protected PropertySource<?> loadPropertySource(EA extensionAttributes, String propertySourceName,
                                                   AnnotationMetadata metadata) throws Throwable {
        String[] resourceLocations = extensionAttributes.getValue();
        boolean ignoreResourceNotFound = extensionAttributes.isIgnoreResourceNotFound();

        if (ObjectUtils.isEmpty(resourceLocations)) {
            if (ignoreResourceNotFound) {
                return null;
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at the annotation : @" + getAnnotationType().getName());
        }

        String encoding = extensionAttributes.getEncoding();
        Comparator<Resource> resourceComparator = createInstance(extensionAttributes, extensionAttributes::getResourceComparatorClass);
        PropertySourceFactory factory = createInstance(extensionAttributes, extensionAttributes::getPropertySourceFactoryClass);

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);

        List<Resource> resourcesList = new LinkedList<>();

        for (String resourceLocation : resourceLocations) {
            Resource[] resources = null;

            try {
                resources = getResources(extensionAttributes, propertySourceName, resourceLocation);
            } catch (Throwable e) {
                if (!ignoreResourceNotFound) {
                    throw e;
                }
            }

            if (resources == null) {
                // Nullable result
                continue;
            }

            // iterate
            int length = resources.length;
            for (int i = 0; i < length; i++) {
                Resource resource = resources[i];
                resourcesList.add(resource);
            }
        }

        // sort
        resourcesList.sort(resourceComparator);

        for (int i = 0; i < resourcesList.size(); i++) {
            Resource resource = resourcesList.get(i);
            EncodedResource encodedResource = new EncodedResource(resource, encoding);
            String name = propertySourceName + "#" + i;
            PropertySource propertySource = factory.createPropertySource(name, encodedResource);
            compositePropertySource.addPropertySource(propertySource);
        }

        return compositePropertySource;
    }

    /**
     * Resolve the given location pattern into Resource objects.
     * <p>
     * The subclass can override this method for customization
     *
     * @param extensionAttributes
     * @param propertySourceName
     * @param resourceLocation    the resource location to resolve
     * @return non-null
     * @throws Throwable
     */
    protected Resource[] getResources(EA extensionAttributes, String propertySourceName, String resourceLocation)
            throws Throwable {
        return resourcePatternResolver.getResources(resourceLocation);
    }

    private <T> T createInstance(AnnotationAttributes attributes, Supplier<Class<T>> typeSupplier) {
        Class<T> type = typeSupplier.get();
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
