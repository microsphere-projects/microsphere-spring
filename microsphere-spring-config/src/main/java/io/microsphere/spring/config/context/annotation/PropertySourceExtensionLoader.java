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
import org.springframework.core.env.PropertySources;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static io.microsphere.text.FormatUtils.format;
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
    protected final void loadPropertySource(AnnotationAttributes attributes, AnnotationMetadata metadata, String propertySourceName,
                                            MutablePropertySources propertySources) throws Throwable {
        Class<A> annotationType = getAnnotationType();
        Class<EA> extensionAttributesClass = getExtensionAttributesType();
        ConfigurableEnvironment environment = getEnvironment();
        EA extensionAttributes = buildExtensionAttributes(annotationType, extensionAttributesClass, attributes, environment);
        PropertySource<?> propertySource = loadPropertySource(extensionAttributes, propertySourceName, metadata);
        if (propertySource == null) {
            String message = format("The PropertySources' Resource can't be found by the {} that annotated on {}", extensionAttributes, metadata.getClassName());
            if (extensionAttributes.isIgnoreResourceNotFound()) {
                logger.warn(message);
            } else {
                throw new IllegalArgumentException(message);
            }
        } else {
            addPropertySource(extensionAttributes, propertySources, propertySource);
        }
    }

    /**
     * Add the {@link PropertySource} into {@link PropertySources} via {@link EA}
     *
     * @param extensionAttributes the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySources     the {@link MutablePropertySources} to be added
     * @param propertySource      the {@link PropertySource} is about to add
     */
    protected void addPropertySource(EA extensionAttributes, MutablePropertySources propertySources, PropertySource<?> propertySource) {
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
    protected EA buildExtensionAttributes(Class<A> annotationType, Class<EA> extensionAttributesType, AnnotationAttributes attributes, ConfigurableEnvironment environment) throws Throwable {
        Constructor constructor = extensionAttributesType.getConstructor(Map.class, Class.class, PropertyResolver.class);
        return (EA) constructor.newInstance(attributes, annotationType, environment);
    }

    protected final PropertySource<?> loadPropertySource(EA extensionAttributes, String propertySourceName, AnnotationMetadata metadata) throws Throwable {
        String[] resourceValues = extensionAttributes.getValue();

        boolean ignoreResourceNotFound = extensionAttributes.isIgnoreResourceNotFound();

        if (ObjectUtils.isEmpty(resourceValues)) {
            if (ignoreResourceNotFound) {
                return null;
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at the annotation : @" + getAnnotationType().getName());
        }

        List<PropertySourceResource> propertySourceResources = new LinkedList<>();

        for (String resourceValue : resourceValues) {
            Resource[] resources = null;

            try {
                resources = resolveResources(extensionAttributes, propertySourceName, resourceValue);
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
                PropertySourceResource propertySourceResource = createPropertySourceResource(resourceValue, resource);
                propertySourceResources.add(propertySourceResource);
            }
        }

        if (propertySourceResources.isEmpty()) {
            return null;
        }

        Comparator<Resource> resourceComparator = createResourceComparator(extensionAttributes, propertySourceName, metadata);

        PropertySourceFactory factory = createPropertySourceFactory(extensionAttributes);

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);

        // sort
        Collections.sort(propertySourceResources, (o1, o2) -> resourceComparator.compare(o1.resource, o2.resource));


        // Add Resources' PropertySource
        for (int i = 0; i < propertySourceResources.size(); i++) {
            PropertySourceResource propertySourceResource = propertySourceResources.get(i);
            PropertySource resourcePropertySource = createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource);
            compositePropertySource.addPropertySource(resourcePropertySource);
        }

        if (extensionAttributes.isAutoRefreshed()) {
            ResourcePropertySourcesRefresher resourcePropertySourcesRefresher = (resourceValue, resource) -> {
                PropertySourceResource propertySourceResource = createPropertySourceResource(resourceValue, resource);
                // Merge Resources' PropertySource
                PropertySource resourcePropertySource = createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource);
                updatePropertySource(extensionAttributes, propertySourceName, resourcePropertySource);
            };
            configureResourcePropertySourcesRefresher(extensionAttributes, propertySourceResources, compositePropertySource,
                    resourcePropertySourcesRefresher);
        }

        return compositePropertySource;
    }

    private void updatePropertySource(EA extensionAttributes, String propertySourceName, PropertySource resourcePropertySource) {
        MutablePropertySources propertySources = getEnvironment().getPropertySources();
        PropertySource propertySource = propertySources.get(propertySourceName);
        if (propertySource instanceof CompositePropertySource) {
            CompositePropertySource oldCompositePropertySource = (CompositePropertySource) propertySource;
            Collection<PropertySource<?>> resourcePropertySources = oldCompositePropertySource.getPropertySources();
            Iterator<PropertySource<?>> iterator = resourcePropertySources.iterator();

            String resourcePropertySourceName = resourcePropertySource.getName();

            // New CompositePropertySource
            CompositePropertySource newCompositePropertySource = new CompositePropertySource(propertySourceName);

            while (iterator.hasNext()) {
                PropertySource oldResourcePropertySource = iterator.next();
                if (Objects.equals(resourcePropertySourceName, oldResourcePropertySource.getName())) {
                    // Add the new Resources' PropertySource if same with old one
                    newCompositePropertySource.addPropertySource(resourcePropertySource);
                    // TODO may publish events
                } else {
                    // Add an old Resources' PropertySource if not same
                    newCompositePropertySource.addPropertySource(oldResourcePropertySource);
                }
            }

            addPropertySource(extensionAttributes, propertySources, newCompositePropertySource);
        }
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
     * @param propertySourceName  the name of {PropertySource} declared by {@link PropertySourceExtension#name()}
     * @param metadata            {@link AnnotationMetadata}
     * @return an instance of {@link Comparator} for {@link Resource}
     * @see PropertySourceExtension#resourceComparator()
     * @see Comparator
     */
    @NonNull
    protected Comparator<Resource> createResourceComparator(EA extensionAttributes, String propertySourceName, AnnotationMetadata metadata) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getResourceComparatorClass);
    }


    protected PropertySourceResource createPropertySourceResource(String resourceValue, Resource resource) {
        return new PropertySourceResource(resourceValue, resource);
    }

    protected String createResourcePropertySourceName(String propertySourceName, String resourceValue, Resource resource) {
        Object suffix = resource.toString() == null ? resource.hashCode() : resource.toString();
        return propertySourceName + "#" + resourceValue + "@" + suffix;
    }

    /**
     * Create the {@link PropertySource PropertySource} for the specified {@link Resource resource}
     *
     * @param extensionAttributes    the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceName     the name of {PropertySource} declared by {@link PropertySourceExtension#name()}
     * @param factory                the factory to the {@link PropertySource PropertySource} declared by {@link PropertySourceExtension#factory()}
     * @param propertySourceResource
     * @return
     * @throws Throwable
     */
    protected PropertySource createResourcePropertySource(EA extensionAttributes, String propertySourceName,
                                                          PropertySourceFactory factory,
                                                          PropertySourceResource propertySourceResource) throws Throwable {
        String resourceValue = propertySourceResource.resourceValue;
        Resource resource = propertySourceResource.resource;
        String encoding = extensionAttributes.getEncoding();
        EncodedResource encodedResource = new EncodedResource(resource, encoding);
        String name = createResourcePropertySourceName(propertySourceName, resourceValue, resource);
        return factory.createPropertySource(name, encodedResource);
    }

    /**
     * Configure the {@link ResourcePropertySourcesRefresher} of {@link PropertySource} {@link Resource Resources} when
     * {@link PropertySourceExtension#autoRefreshed()} is <code>true</code>
     *
     * @param extensionAttributes              the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceResources          The sorted list of the resolved {@link Resource resources}
     * @param propertySource                   The {@link CompositePropertySource property source} of current loader to be added into
     *                                         the Spring's {@link PropertySources property sources}
     * @param refresher The Refresher of {@link PropertySource PropertySources'} {@link Resource}
     * @throws Throwable any error
     */
    protected void configureResourcePropertySourcesRefresher(EA extensionAttributes, List<PropertySourceResource> propertySourceResources,
                                                             CompositePropertySource propertySource,
                                                             ResourcePropertySourcesRefresher refresher) throws Throwable {
        // DO NOTHING
    }

    /**
     * Resolve the given resource value(s) to be {@link PropertySourceResource} array.
     *
     * @param extensionAttributes {@link EA}
     * @param propertySourceName  {@link PropertySourceExtension#name()}
     * @param resourceValue       the resource value to resolve
     * @return nullable
     * @throws Throwable
     */
    @Nullable
    protected abstract Resource[] resolveResources(EA extensionAttributes, String propertySourceName, String resourceValue) throws Throwable;

    protected <T> T createInstance(EA extensionAttributes, Function<EA, Class<T>> classFunction) {
        Class<T> type = classFunction.apply(extensionAttributes);
        return BeanUtils.instantiateClass(type);
    }


    protected static class PropertySourceResource {

        private final String resourceValue;

        private final Resource resource;

        public PropertySourceResource(String resourceValue, Resource resource) {
            this.resourceValue = resourceValue;
            this.resource = resource;
        }

        public String getResourceValue() {
            return resourceValue;
        }

        public Resource getResource() {
            return resource;
        }
    }

    /**
     * The Refresher of {@link PropertySources PropertySources'} for {@link Resource}
     *
     * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
     * @see PropertySource
     * @see Resource
     * @since 1.0.0
     */
    @FunctionalInterface
    protected interface ResourcePropertySourcesRefresher {

        /**
         * Refresh the {@link PropertySources PropertySources} on {@link {@link Resource}} being refreshed
         *
         * @param resourceValue the value of resource declared by {@link PropertySourceExtension#value()}
         * @param resource      the {@link PropertySource PropertySources'} {@link Resource}
         * @throws Throwable any error occurs
         */
        void refresh(String resourceValue, Resource resource) throws Throwable;
    }

}
