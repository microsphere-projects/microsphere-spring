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

import io.microsphere.spring.config.env.event.PropertySourceChangedEvent;
import io.microsphere.spring.config.env.event.PropertySourcesChangedEvent;
import org.springframework.beans.BeanUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
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
import org.springframework.util.AntPathMatcher;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PathMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.added;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.replaced;
import static io.microsphere.text.FormatUtils.format;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Collections.sort;
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
        extends AnnotatedPropertySourceLoader<A> implements ApplicationContextAware {

    private final Class<EA> extensionAttributesType;

    private final PathMatcher resourceMatcher;

    private ApplicationContext context;


    public PropertySourceExtensionLoader() {
        super();
        this.extensionAttributesType = resolveExtensionAttributesType();
        this.resourceMatcher = new AntPathMatcher();
    }

    protected Class<EA> resolveExtensionAttributesType() {
        ResolvableType type = ResolvableType.forType(this.getClass());
        ResolvableType superType = type.as(PropertySourceExtensionLoader.class);
        return (Class<EA>) superType.resolveGeneric(1);
    }

    /**
     * Test the resource value is pattern or not
     *
     * @param resourceValue the {@link String} value presenting resource
     * @return <code>true</code> if <code>resourceValue</code> is a pattern value
     */
    public boolean isResourcePattern(String resourceValue) {
        return this.resourceMatcher.isPattern(resourceValue);
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
        PropertySource<?> propertySource = loadPropertySource(extensionAttributes, propertySourceName);
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

    protected final PropertySource<?> loadPropertySource(EA extensionAttributes, String propertySourceName) throws Throwable {

        Comparator<Resource> resourceComparator = createResourceComparator(extensionAttributes, propertySourceName);

        List<PropertySourceResource> propertySourceResources = resolvePropertySourceResources(extensionAttributes, propertySourceName, resourceComparator);

        int propertySourceResourcesSize = propertySourceResources.size();

        if (propertySourceResourcesSize < 1) { // No PropertySourceResource found
            return null;
        }

        PropertySourceFactory factory = createPropertySourceFactory(extensionAttributes);

        CompositePropertySource compositePropertySource = new CompositePropertySource(propertySourceName);

        // Add Resources' PropertySource
        for (int i = 0; i < propertySourceResourcesSize; i++) {
            PropertySourceResource propertySourceResource = propertySourceResources.get(i);
            ResourcePropertySource resourcePropertySource = createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource);
            compositePropertySource.addPropertySource(resourcePropertySource);
        }

        if (extensionAttributes.isAutoRefreshed()) {
            ResourcePropertySourcesRefresher resourcePropertySourcesRefresher = createResourcePropertySourcesRefresher(
                    extensionAttributes, propertySourceName, factory, resourceComparator);

            configureResourcePropertySourcesRefresher(extensionAttributes, propertySourceResources, compositePropertySource,
                    resourcePropertySourcesRefresher);
        }

        return compositePropertySource;
    }

    private ResourcePropertySourcesRefresher createResourcePropertySourcesRefresher(EA extensionAttributes, String propertySourceName,
                                                                                    PropertySourceFactory factory,
                                                                                    Comparator<Resource> resourceComparator) throws Throwable {
        return (resourceValue, resource) -> {

            CompositePropertySource compositePropertySource = getPropertySource(propertySourceName);
            if (compositePropertySource == null) {
                return;
            }

            List<PropertySourceChangedEvent> subEvents = new LinkedList<>();

            if (resource == null) { // No Resource specified
                refreshPropertySources(extensionAttributes, propertySourceName, factory, resourceComparator, resourceValue, compositePropertySource, subEvents);
            } else {
                refreshPropertySources(extensionAttributes, propertySourceName, factory, resourceComparator, resourceValue, resource, compositePropertySource, subEvents);
            }

            publishPropertySourcesChangedEvent(subEvents);
        };

    }

    private void publishPropertySourcesChangedEvent(List<PropertySourceChangedEvent> subEvents) {
        PropertySourcesChangedEvent propertySourcesChangedEvent = new PropertySourcesChangedEvent(this.context, subEvents);
        this.context.publishEvent(propertySourcesChangedEvent);
    }

    private void refreshPropertySources(EA extensionAttributes, String propertySourceName, PropertySourceFactory factory,
                                        Comparator<Resource> resourceComparator, String resourceValue,
                                        CompositePropertySource compositePropertySource, List<PropertySourceChangedEvent> subEvents) throws Throwable {

        // Resolve the target PropertySourceResources
        List<PropertySourceResource> propertySourceResources = resolvePropertySourceResources(extensionAttributes, propertySourceName, resourceValue, resourceComparator);

        int propertySourceResourcesSize = propertySourceResources.size();

        if (propertySourceResourcesSize < 1) {
            return;
        }

        List<ResourcePropertySource> resourcePropertySources = getResourcePropertySources(compositePropertySource);

        List<ResourcePropertySource> newResourcePropertySources = new ArrayList<>(propertySourceResourcesSize);

        for (int i = 0; i < propertySourceResourcesSize; i++) {
            PropertySourceResource propertySourceResource = propertySourceResources.get(i);
            ResourcePropertySource newResourcePropertySource = createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource);
            newResourcePropertySources.add(newResourcePropertySource);
        }

        updateResourcePropertySources(newResourcePropertySources, resourcePropertySources, subEvents);

        updatePropertySources(propertySourceName, resourcePropertySources);
    }

    private void refreshPropertySources(EA extensionAttributes, String propertySourceName, PropertySourceFactory factory,
                                        Comparator<Resource> resourceComparator, String resourceValue, Resource resource,
                                        CompositePropertySource compositePropertySource, List<PropertySourceChangedEvent> subEvents) throws Throwable {

        PropertySourceResource propertySourceResource = createPropertySourceResource(resourceValue, resource, resourceComparator);

        ResourcePropertySource newResourcePropertySource = createResourcePropertySource(extensionAttributes, propertySourceName, factory, propertySourceResource);

        List<ResourcePropertySource> resourcePropertySources = getResourcePropertySources(compositePropertySource);

        updateResourcePropertySources(singleton(newResourcePropertySource), resourcePropertySources, subEvents);

        updatePropertySources(propertySourceName, resourcePropertySources);
    }

    private void updatePropertySources(String propertySourceName, List<ResourcePropertySource> resourcePropertySources) {
        // Sort all ResourcePropertySources
        sort(resourcePropertySources);

        // Add all ResourcePropertySources into new CompositePropertySource
        CompositePropertySource newCompositePropertySource = new CompositePropertySource(propertySourceName);
        resourcePropertySources.forEach(newCompositePropertySource::addPropertySource);

        // Add new CompositePropertySource
        MutablePropertySources propertySources = getPropertySources();
        propertySources.replace(propertySourceName, newCompositePropertySource);
    }

    private void updateResourcePropertySources(Iterable<ResourcePropertySource> newResourcePropertySources,
                                               List<ResourcePropertySource> resourcePropertySources, List<PropertySourceChangedEvent> subEvents) {

        for (ResourcePropertySource newResourcePropertySource : newResourcePropertySources) {
            String newResourcePropertySourceName = newResourcePropertySource.getName();

            Iterator<ResourcePropertySource> iterator = resourcePropertySources.iterator();

            boolean addedSubEvent = false;
            while (iterator.hasNext()) {
                ResourcePropertySource resourcePropertySource = iterator.next();
                // Remove the old ResourcePropertySource if exists
                if (newResourcePropertySourceName.equals(resourcePropertySource.getName())) {
                    subEvents.add(replaced(this.context, newResourcePropertySource, resourcePropertySource));
                    addedSubEvent = true;
                    iterator.remove();
                }
            }
            if (!addedSubEvent) {
                subEvents.add(added(this.context, newResourcePropertySource));
            }
            // Add new ResourcePropertySource
            resourcePropertySources.add(newResourcePropertySource);
        }
    }

    private void updateResourcePropertySources(ResourcePropertySource newResourcePropertySource,
                                               List<ResourcePropertySource> resourcePropertySources, List<PropertySourceChangedEvent> subEvents) {

        String newResourcePropertySourceName = newResourcePropertySource.getName();

        Iterator<ResourcePropertySource> iterator = resourcePropertySources.iterator();

        List<ResourcePropertySource> oldResourcePropertySources = new LinkedList<>();

        while (iterator.hasNext()) {
            ResourcePropertySource resourcePropertySource = iterator.next();
            // Remove the old ResourcePropertySource if exists
            if (newResourcePropertySourceName.equals(resourcePropertySource.getName())) {
                oldResourcePropertySources.add(resourcePropertySource);
                iterator.remove();
            }
        }

        // Add new ResourcePropertySource
        resourcePropertySources.add(newResourcePropertySource);


    }

    private List<ResourcePropertySource> getResourcePropertySources(CompositePropertySource compositePropertySource) {
        Collection<PropertySource<?>> propertySources = compositePropertySource.getPropertySources();
        List<ResourcePropertySource> resourcePropertySources = new ArrayList<>(propertySources.size());
        propertySources.stream()
                .map(ResourcePropertySource.class::cast)
                .forEach(resourcePropertySources::add);
        return resourcePropertySources;
    }

    private CompositePropertySource getPropertySource(String propertySourceName) {
        MutablePropertySources propertySources = getPropertySources();
        PropertySource propertySource = propertySources.get(propertySourceName);
        if (propertySource instanceof CompositePropertySource) {
            return (CompositePropertySource) propertySource;
        } else {
            logger.warn("The CompositePropertySource can't be found by the name : {} , actual : {}", propertySourceName, propertySource);
        }
        return null;
    }

    private List<PropertySourceResource> resolvePropertySourceResources(EA extensionAttributes, String propertySourceName,
                                                                        Comparator<Resource> resourceComparator) throws Throwable {
        String[] resourceValues = extensionAttributes.getValue();

        boolean ignoreResourceNotFound = extensionAttributes.isIgnoreResourceNotFound();

        if (ObjectUtils.isEmpty(resourceValues)) {
            if (ignoreResourceNotFound) {
                return emptyList();
            }
            throw new IllegalArgumentException("The 'value' attribute must be present at the annotation : @" + getAnnotationType().getName());
        }

        List<PropertySourceResource> propertySourceResources = new LinkedList<>();

        for (String resourceValue : resourceValues) {
            propertySourceResources.addAll(resolvePropertySourceResources(extensionAttributes, propertySourceName, resourceValue, resourceComparator));
        }

        sort(propertySourceResources);

        return propertySourceResources;
    }

    protected List<PropertySourceResource> resolvePropertySourceResources(EA extensionAttributes, String propertySourceName,
                                                                          String resourceValue, Comparator<Resource> resourceComparator) throws Throwable {

        boolean ignoreResourceNotFound = extensionAttributes.isIgnoreResourceNotFound();

        Resource[] resources = null;

        try {
            resources = resolveResources(extensionAttributes, propertySourceName, resourceValue);
        } catch (Throwable e) {
            if (!ignoreResourceNotFound) {
                throw e;
            }
        }

        if (resources == null) {
            return emptyList();
        }

        // iterate
        int length = resources.length;

        List<PropertySourceResource> propertySourceResources = new ArrayList<>(length);

        for (int i = 0; i < length; i++) {
            Resource resource = resources[i];
            PropertySourceResource propertySourceResource = createPropertySourceResource(resourceValue, resource, resourceComparator);
            propertySourceResources.add(propertySourceResource);
        }

        return propertySourceResources;
    }

    private MutablePropertySources getPropertySources() {
        return this.getEnvironment().getPropertySources();
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
     * @return an instance of {@link Comparator} for {@link Resource}
     * @see PropertySourceExtension#resourceComparator()
     * @see Comparator
     */
    @NonNull
    protected Comparator<Resource> createResourceComparator(EA extensionAttributes, String propertySourceName) {
        return createInstance(extensionAttributes, PropertySourceExtensionAttributes::getResourceComparatorClass);
    }

    private PropertySourceResource createPropertySourceResource(String resourceValue, Resource resource, Comparator<Resource> resourceComparator) {
        return new PropertySourceResource(resourceValue, resource, resourceComparator);
    }

    protected String createResourcePropertySourceName(String propertySourceName, String resourceValue, Resource resource) {
        Object suffix = resource.toString() == null ? resource.hashCode() : resource.toString();
        return propertySourceName + "#" + resourceValue + "@" + suffix;
    }

    /**
     * Create an instance of {@link ResourcePropertySource} for the specified {@link Resource resource}
     *
     * @param extensionAttributes    the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceName     the name of {PropertySource} declared by {@link PropertySourceExtension#name()}
     * @param factory                the factory to the {@link PropertySource PropertySource} declared by {@link PropertySourceExtension#factory()}
     * @param propertySourceResource the source of {@link PropertySource}
     * @return an instance of {@link ResourcePropertySource}
     * @throws Throwable
     */
    protected final ResourcePropertySource createResourcePropertySource(EA extensionAttributes,
                                                                        String propertySourceName,
                                                                        PropertySourceFactory factory,
                                                                        PropertySourceResource propertySourceResource) throws Throwable {
        String resourceValue = propertySourceResource.resourceValue;
        Resource resource = propertySourceResource.resource;
        String encoding = extensionAttributes.getEncoding();
        EncodedResource encodedResource = new EncodedResource(resource, encoding);
        String name = createResourcePropertySourceName(propertySourceName, resourceValue, resource);
        PropertySource propertySource = factory.createPropertySource(name, encodedResource);
        return new ResourcePropertySource(propertySourceResource, propertySource);
    }

    /**
     * Configure the {@link ResourcePropertySourcesRefresher} of {@link PropertySource} {@link Resource Resources} when
     * {@link PropertySourceExtension#autoRefreshed()} is <code>true</code>
     *
     * @param extensionAttributes     the {@link PropertySourceExtensionAttributes annotation attributes} of {@link PropertySourceExtension}
     * @param propertySourceResources The sorted list of the resolved {@link Resource resources}
     * @param propertySource          The {@link CompositePropertySource property source} of current loader to be added into
     *                                the Spring's {@link PropertySources property sources}
     * @param refresher               The Refresher of {@link PropertySource PropertySources'} {@link Resource}
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

    @Override
    public void setApplicationContext(ApplicationContext context) {
        this.context = context;
    }

    /**
     * The resource of {@link PropertySource}
     */
    protected static class PropertySourceResource implements Comparable<PropertySourceResource> {

        private final String resourceValue;

        private final Resource resource;

        private final Comparator<Resource> resourceComparator;

        public PropertySourceResource(String resourceValue, Resource resource, Comparator<Resource> resourceComparator) {
            this.resourceValue = resourceValue;
            this.resource = resource;
            this.resourceComparator = resourceComparator;
        }

        public String getResourceValue() {
            return resourceValue;
        }

        public Resource getResource() {
            return resource;
        }

        @Override
        public int compareTo(PropertySourceResource o) {
            return this.resourceComparator.compare(this.resource, o.resource);
        }
    }

    /**
     * The {@link Resource}-oriented {@link PropertySource}
     */
    private static class ResourcePropertySource<T> extends EnumerablePropertySource<T> implements Comparable<ResourcePropertySource<T>> {

        private final PropertySourceResource propertySourceResource;

        private final PropertySource<T> original;

        private final EnumerablePropertySource<T> enumerablePropertySource;

        public ResourcePropertySource(PropertySourceResource propertySourceResource, PropertySource<T> original) {
            super(original.getName(), original.getSource());
            this.propertySourceResource = propertySourceResource;
            this.original = original;
            this.enumerablePropertySource = original instanceof EnumerablePropertySource ? (EnumerablePropertySource<T>) original : null;
        }

        @Override
        public String[] getPropertyNames() {
            if (enumerablePropertySource == null) {
                return EMPTY_STRING_ARRAY;
            }
            return enumerablePropertySource.getPropertyNames();
        }

        @Override
        public Object getProperty(String name) {
            if (enumerablePropertySource == null) {
                return null;
            }
            return enumerablePropertySource.getProperty(name);
        }

        /**
         * @return the value of resource
         */
        public String getResourceValue() {
            return propertySourceResource.resourceValue;
        }

        public Resource getResource() {
            return propertySourceResource.resource;
        }

        /**
         * @return the original {@link PropertySource}
         */
        public PropertySource<T> getOriginal() {
            return original;
        }

        @Override
        public int compareTo(ResourcePropertySource<T> o) {
            return this.propertySourceResource.compareTo(o.propertySourceResource);
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
         * Refresh the {@link PropertySources PropertySources} on {@link Resource} being refreshed
         *
         * @param resourceValue the value of resource declared by {@link PropertySourceExtension#value()}
         * @param resource      the optional {@link PropertySource PropertySources'} {@link Resource}.
         *                      If <code>resource</code> is <code>null</code>, it indicates the resource is not specified,
         *                      the actual resource(s) will be resolved by the <code>resourceValue</code>, or refreshes
         *                      the {@link PropertySources PropertySources} from the specified {@link Resource}
         * @throws Throwable any error occurs
         */
        void refresh(String resourceValue, @Nullable Resource resource) throws Throwable;

        /**
         * Refresh the {@link PropertySources PropertySources} on the {@link Resource}(s) that was or were resolved by the value
         *
         * @param resourceValue the value of resource declared by {@link PropertySourceExtension#value()}
         * @throws Throwable any error occurs
         */
        default void refresh(String resourceValue) throws Throwable {
            refresh(resourceValue, null);
        }
    }

}
