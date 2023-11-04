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


import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * The event raised when one single {@link PropertySource} is changed
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertySource
 * @since 1.0.0
 */
public class PropertySourceChangedEvent extends ApplicationContextEvent {

    private final Kind kind;

    private final PropertySource newPropertySource;

    private final PropertySource oldPropertySource;

    protected PropertySourceChangedEvent(ApplicationContext source, Kind kind, @Nullable PropertySource newPropertySource,
                                         @Nullable PropertySource oldPropertySource) {
        super(source);
        this.kind = kind;
        this.newPropertySource = newPropertySource;
        this.oldPropertySource = oldPropertySource;
    }

    protected PropertySourceChangedEvent(ApplicationContext source, Kind kind, @Nullable PropertySource newPropertySource) {
        this(source, kind, newPropertySource, null);
    }

    /**
     * @return the new {@link PropertySource}
     */
    @Nullable
    public PropertySource getNewPropertySource() {
        return newPropertySource;
    }

    /**
     * @return the old {@link PropertySource}
     */
    @Nullable
    public PropertySource getOldPropertySource() {
        return oldPropertySource;
    }

    /**
     * @return the Kind of {@link PropertySource} Changed Event
     */
    public Kind getKind() {
        return kind;
    }

    /**
     * Create an {@link Kind#ADDED added} instance
     *
     * @param source            {@link ApplicationContext}
     * @param newPropertySource the new {@link PropertySource}
     * @return non-null
     */
    public static PropertySourceChangedEvent added(ApplicationContext source, @NonNull PropertySource newPropertySource) {
        return new PropertySourceChangedEvent(source, Kind.ADDED, newPropertySource);
    }

    /**
     * Create an {@link Kind#REPLACED replaced} instance
     *
     * @param source            {@link ApplicationContext}
     * @param newPropertySource the new {@link PropertySource}
     * @param oldPropertySource the old {@link PropertySource}
     * @return non-null
     */
    public static PropertySourceChangedEvent replaced(ApplicationContext source, @NonNull PropertySource newPropertySource, @NonNull PropertySource oldPropertySource) {
        return new PropertySourceChangedEvent(source, Kind.REPLACED, newPropertySource, oldPropertySource);
    }

    /**
     * Create an {@link Kind#REMOVED removed} instance
     *
     * @param source            {@link ApplicationContext}
     * @param oldPropertySource the old {@link PropertySource}
     * @return non-null
     */
    public static PropertySourceChangedEvent removed(ApplicationContext source, @NonNull PropertySource oldPropertySource) {
        return new PropertySourceChangedEvent(source, Kind.REMOVED, null, oldPropertySource);
    }

    /**
     * The Kind of {@link PropertySource} Changed Event
     */
    public static enum Kind {

        /**
         * The {@link PropertySource} added
         *
         * @see MutablePropertySources#addAfter(String, PropertySource)
         * @see MutablePropertySources#addBefore(String, PropertySource)
         * @see MutablePropertySources#addFirst(PropertySource)
         * @see MutablePropertySources#addLast(PropertySource)
         */
        ADDED,

        /**
         * The {@link PropertySource} replaced
         *
         * @see MutablePropertySources#replace(String, PropertySource)
         */
        REPLACED,

        /**
         * The {@link PropertySource} deleted
         *
         * @see MutablePropertySources#remove(String)
         */
        REMOVED;
    }
}
