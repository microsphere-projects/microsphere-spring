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

import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.springframework.util.StringUtils.hasText;

/**
 * Abstract {@link ImportSelector} class to load the {@link PropertySource PropertySource}
 * when the {@link Configuration configuration} annotated the Enable annotation that meta-annotates {@link PropertySourceExtension @PropertySourceExtension}
 *
 * @param <A>  The type of {@link Annotation} must meta-annotate {@link PropertySourceExtension}
 * @param <EA> The {@link PropertySourceExtensionAttributes} or its subtype
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @see PropertySourceExtensionAttributes
 * @see ImportSelector
 * @since 1.0.0
 */
public abstract class PropertySourceExtensionLoader<A extends Annotation, EA extends PropertySourceExtensionAttributes<A>>
        extends AnnotatedPropertySourceLoader<A> {

    private final Class<EA> extensionAttributesType;

    public PropertySourceExtensionLoader() {
        super();
        this.extensionAttributesType = resolveExtensionAttributesType();
    }

    protected Class<EA> resolveExtensionAttributesType() {
        ResolvableType type = ResolvableType.forType(this.getClass());
        ResolvableType superType = type.as(PropertySourceExtensionLoader.class);
        return (Class<EA>) superType.resolveGeneric(1);
    }

    /**
     * Get the type of {@link EA}
     *
     * @return non-null
     * @see PropertySourceExtensionAttributes
     */
    public final Class<EA> getExtensionAttributesType() {
        return extensionAttributesType;
    }

    @Override
    protected final void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata,
                                            String propertySourceName, MutablePropertySources propertySources) throws Throwable {
        Class<A> annotationType = getAnnotationType();
        Class<EA> extensionAttributesClass = getExtensionAttributesType();
        ConfigurableEnvironment environment = getEnvironment();
        EA extensionAttributes = buildExtensionAttributes(annotationType, extensionAttributesClass, attributes, environment);
        PropertySource<?> propertySource = loadPropertySource(extensionAttributes, propertySourceName, metadata);
        if (propertySource == null) {
            logger.warn("The PropertySource[annotationType : '{}' , configuration : '{}'] can't be loaded", annotationType.getName(), metadata.getClassName());
        } else {
            if (extensionAttributes.isFirstPropertySource()) {
                propertySources.addFirst(propertySource);
            } else {
                String relativePropertySourceName = extensionAttributes.getAfterPropertySourceName();
                if (hasText(relativePropertySourceName)) {
                    propertySources.addAfter(relativePropertySourceName, propertySource);
                } else {
                    relativePropertySourceName = extensionAttributes.getBeforePropertySourceName();
                }
                if (hasText(relativePropertySourceName)) {
                    propertySources.addBefore(relativePropertySourceName, propertySource);
                } else {
                    propertySources.addLast(propertySource);
                }
            }
        }
    }

    /**
     * Builds an instance of {@link EA}
     *
     * @param annotationType          the type of {@link A}
     * @param extensionAttributesType the type of {@link EA}
     * @param attributes              {@link AnnotationAttributes}
     * @param environment             {@link ConfigurableEnvironment}
     * @return non-null
     * @throws Throwable
     */
    protected EA buildExtensionAttributes(Class<A> annotationType, Class<EA> extensionAttributesType,
                                          AnnotationAttributes attributes, ConfigurableEnvironment environment) throws Throwable {
        Constructor constructor = extensionAttributesType.getConstructor(Map.class, Class.class, PropertyResolver.class);
        return (EA) constructor.newInstance(attributes, annotationType, environment);
    }

    protected final PropertySource<?> loadPropertySource(EA extensionAttributes, String propertySourceName,
                                                         AnnotationMetadata metadata) throws Throwable {
        String[] resourceValues = extensionAttributes.getValue();

        boolean ignoreResourceNotFound = extensionAttributes.isIgnoreResourceNotFound();

        if (ObjectUtils.isEmpty(resourceValues)) {
            if (ignoreResourceNotFound) {
                return null;
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at the annotation : @" + getAnnotationType().getName());
        }

        Comparator<Resource> resourceComparator = createResourceComparator(extensionAttributes, propertySourceName, metadata);

        PropertySourceFactory factory = createPropertySourceFactory(extensionAttributes);

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);

        List<Resource> resourcesList = new LinkedList<>();

        for (String resourceValue : resourceValues) {
            Resource[] resources = null;

            try {
                resources = getResources(extensionAttributes, propertySourceName, resourceValue);
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

        String encoding = extensionAttributes.getEncoding();

        if (extensionAttributes.isAutoRefreshed()) {
            configureAutoRefreshedResources(extensionAttributes, propertySourceName, resourceValues, resourcesList);
        }

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
     * Creates an instance of {@link PropertySourceFactory}
     *
     * @param extensionAttributes the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @return
     * @see PropertySourceExtension#factory()
     * @see PropertySourceFactory
     */
    @NonNull
    protected PropertySourceFactory createPropertySourceFactory(EA extensionAttributes) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getPropertySourceFactoryClass);
    }

    /**
     * Creates an instance of {@link Comparator} for {@link Resource}
     *
     * @param extensionAttributes the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceName  {@link PropertySourceExtension#name()}
     * @param metadata            {@link AnnotationMetadata}
     * @return an instance of {@link Comparator} for {@link Resource}
     * @see PropertySourceExtension#resourceComparator()
     * @see Comparator
     */
    @NonNull
    protected Comparator<Resource> createResourceComparator(EA extensionAttributes, String propertySourceName, AnnotationMetadata metadata) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getResourceComparatorClass);
    }

    /**
     * Configure the listeners for Auto-Refreshed Resources when
     * {@link PropertySourceExtension#autoRefreshed()} is <code>true</code>
     *
     * @param extensionAttributes the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceName  {@link PropertySourceExtension#name()}
     * @param resourceValues      {@link PropertySourceExtension#value() the values of resources}
     * @param resourcesList       The resolved {@link Resource resources} list
     * @throws Throwable any error
     */
    protected void configureAutoRefreshedResources(EA extensionAttributes, String propertySourceName,
                                                   String[] resourceValues, List<Resource> resourcesList) throws Throwable {

    }

    /**
     * Resolve the given location pattern into Resource objects.
     *
     * @param extensionAttributes {@link EA}
     * @param propertySourceName  {@link PropertySourceExtension#name()}
     * @param resourceValue       the resource value to resolve
     * @return nullable
     * @throws Throwable
     */
    @Nullable
    protected abstract Resource[] getResources(EA extensionAttributes, String propertySourceName, String resourceValue)
            throws Throwable;

    protected <T> T createInstance(EA extensionAttributes, Function<EA, Class<T>> classFunction) {
        Class<T> type = classFunction.apply(extensionAttributes);
        return BeanUtils.instantiateClass(type);
    }

}
