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
package io.microsphere.spring.config.env.event;


import io.microsphere.annotation.Nonnull;
import io.microsphere.spring.core.env.PropertySourcesUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.PropertySource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.ADDED;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.REMOVED;
import static io.microsphere.spring.config.env.event.PropertySourceChangedEvent.Kind.REPLACED;
import static java.util.Arrays.binarySearch;
import static java.util.Collections.emptyMap;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;

/**
 * Event raised when one or more {@link PropertySource} instances are added, removed, or replaced in the environment's property sources.
 * This event extends {@link ApplicationContextEvent}, which means it is associated with an {@link ApplicationContext}.
 *
 * <p>{@link PropertySourcesChangedEvent} encapsulates multiple {@link PropertySourceChangedEvent}s to represent bulk changes
 * to the property sources. It allows consumers to inspect individual changes and also provides utility methods to extract
 * added, removed, and overall changed properties.
 *
 * <h3>Example Usage</h3>
 * <pre>{@code
 * // Creating a PropertySourcesChangedEvent from multiple PropertySourceChangedEvents
 * PropertySourceChangedEvent addedEvent = PropertySourceChangedEvent.added(context, newPropertySource);
 * PropertySourceChangedEvent removedEvent = PropertySourceChangedEvent.removed(context, oldPropertySource);
 *
 * PropertySourcesChangedEvent combinedEvent = new PropertySourcesChangedEvent(context, Arrays.asList(addedEvent, removedEvent));
 *
 * // Getting all changed properties
 * Map<String, Object> allChangedProperties = combinedEvent.getChangedProperties();
 *
 * // Getting only added properties
 * Map<String, Object> addedProperties = combinedEvent.getAddedProperties();
 *
 * // Getting only removed properties
 * Map<String, Object> removedProperties = combinedEvent.getRemovedProperties();
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceChangedEvent
 * @see PropertySource
 * @see ApplicationContextEvent
 * @since 1.0.0
 */
public class PropertySourcesChangedEvent extends ApplicationContextEvent {

    private final List<PropertySourceChangedEvent> subEvents;

    public PropertySourcesChangedEvent(ApplicationContext source, PropertySourceChangedEvent... subEvents) {
        this(source, ofList(subEvents));
    }

    public PropertySourcesChangedEvent(ApplicationContext source, List<PropertySourceChangedEvent> subEvents) {
        super(source);
        this.subEvents = subEvents;
    }

    /**
     * @return The sub {@link PropertySourceChangedEvent events}
     */
    public List<PropertySourceChangedEvent> getSubEvents() {
        return unmodifiableList(this.subEvents);
    }

    /**
     * @return Get the changed properties
     */
    @Nonnull
    public Map<String, Object> getChangedProperties() {
        return getProperties(PropertySourceChangedEvent::getNewPropertySource, PropertySourcesUtils::getProperties,
                ADDED, REPLACED);
    }

    /**
     * @return Get the added properties
     */
    @Nonnull
    public Map<String, Object> getAddedProperties() {
        return getProperties(PropertySourceChangedEvent::getNewPropertySource, PropertySourcesUtils::getProperties,
                ADDED);
    }

    /**
     * @return Get the removed properties
     */
    @Nonnull
    public Map<String, Object> getRemovedProperties() {
        return getProperties(PropertySourceChangedEvent::getOldPropertySource, PropertySourcesUtils::getProperties,
                REMOVED);
    }

    protected Map<String, Object> getProperties(Function<PropertySourceChangedEvent, PropertySource> propertySourceGetter,
                                                Function<PropertySource, Map<String, Object>> propertiesGenerator,
                                                PropertySourceChangedEvent.Kind... kinds) {
        int size = subEvents.size();
        if (size == 0) {
            return emptyMap();
        }

        Predicate<PropertySourceChangedEvent.Kind> predicate = findAny(kinds);

        Map<String, Object> properties = new LinkedHashMap<>();

        for (int i = 0; i < size; i++) {
            PropertySourceChangedEvent event = subEvents.get(i);
            if (predicate.test(event.getKind())) {
                PropertySource propertySource = propertySourceGetter.apply(event);
                Map<String, Object> subProperties = propertiesGenerator.apply(propertySource);
                for (Map.Entry<String, Object> entry : subProperties.entrySet()) {
                    String propertyName = entry.getKey();
                    properties.computeIfAbsent(propertyName, k -> entry.getValue());
                }
            }
        }

        return unmodifiableMap(properties);
    }

    private Predicate<PropertySourceChangedEvent.Kind> findAny(PropertySourceChangedEvent.Kind... kinds) {
        return kind -> binarySearch(kinds, kind) > -1;
    }
}
