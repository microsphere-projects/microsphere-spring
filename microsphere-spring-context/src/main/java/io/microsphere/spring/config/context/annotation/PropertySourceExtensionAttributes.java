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

import io.microsphere.annotation.Nullable;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertySourceFactory;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.util.ExceptionUtils.create;
import static java.lang.Boolean.TRUE;

/**
 * {@link AnnotationAttributes} for the annotation meta-annotated {@link PropertySourceExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @see ResolvablePlaceholderAnnotationAttributes
 * @since 1.0.0
 */
public class PropertySourceExtensionAttributes<A extends Annotation> extends ResolvablePlaceholderAnnotationAttributes<A> {

    private static final Class<PropertySourceExtension> PROPERTY_SOURCE_EXTENSION_CLASS = PropertySourceExtension.class;

    /**
     * Constructs a new {@link PropertySourceExtensionAttributes} instance from the given annotation attributes map,
     * annotation type, and an optional {@link PropertyResolver} for resolving placeholders.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AnnotationAttributes attrs = getAnnotationAttributes(MyConfig.class, ResourcePropertySource.class, environment, false);
     *   PropertySourceExtensionAttributes<ResourcePropertySource> extensionAttrs =
     *       new PropertySourceExtensionAttributes<>(attrs, ResourcePropertySource.class, environment);
     * }</pre>
     *
     * @param another          the map of annotation attribute name-value pairs
     * @param annotationType   the annotation type that must be meta-annotated with {@link PropertySourceExtension}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders, may be {@code null}
     * @throws IllegalArgumentException if the annotation type is not meta-annotated with {@link PropertySourceExtension}
     */
    public PropertySourceExtensionAttributes(Map<String, Object> another, Class<A> annotationType, @Nullable PropertyResolver propertyResolver) {
        super(another, validateAnnotationType(annotationType), propertyResolver);
    }

    static <A> Class<A> validateAnnotationType(Class<A> annotationType) {
        if (!annotationType.isAnnotationPresent(PROPERTY_SOURCE_EXTENSION_CLASS)) {
            throw create(IllegalArgumentException.class, "The annotation type '{}' must be meta-annotated by '{}'",
                    annotationType.getName(), PROPERTY_SOURCE_EXTENSION_CLASS.getName());
        }
        return annotationType;
    }

    /**
     * Returns the name of the property source as specified by the {@code name} attribute of the annotation.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(name = "test-property-source", value = "classpath*:/META-INF/test/*.properties")
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String name = attributes.getName(); // "test-property-source"
     * }</pre>
     *
     * @return the property source name, or an empty string if not specified
     */
    public final String getName() {
        return getString("name");
    }

    /**
     * Returns whether the property source should be automatically refreshed when the underlying
     * resource changes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", autoRefreshed = true)
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   boolean autoRefreshed = attributes.isAutoRefreshed(); // true
     * }</pre>
     *
     * @return {@code true} if auto-refresh is enabled, {@code false} otherwise
     */
    public final boolean isAutoRefreshed() {
        return TRUE.equals(get("autoRefreshed"));
    }

    /**
     * Returns whether this property source should be placed first in the
     * {@link org.springframework.core.env.MutablePropertySources} list.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", first = true)
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   boolean first = attributes.isFirstPropertySource(); // true
     * }</pre>
     *
     * @return {@code true} if this property source should be first, {@code false} otherwise
     */
    public final boolean isFirstPropertySource() {
        return getBoolean("first");
    }

    /**
     * Returns the name of the property source before which this property source should be inserted.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties",
     *   //     before = "systemEnvironment")
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String before = attributes.getBeforePropertySourceName(); // "systemEnvironment"
     * }</pre>
     *
     * @return the name of the property source to insert before, or an empty string if not specified
     */
    public final String getBeforePropertySourceName() {
        return getString("before");
    }

    /**
     * Returns the name of the property source after which this property source should be inserted.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties",
     *   //     after = "systemProperties")
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String after = attributes.getAfterPropertySourceName(); // "systemProperties"
     * }</pre>
     *
     * @return the name of the property source to insert after, or an empty string if not specified
     */
    public final String getAfterPropertySourceName() {
        return getString("after");
    }

    /**
     * Returns the annotation type associated with these extension attributes.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   Class<ResourcePropertySource> type = attributes.getAnnotationType();
     *   // type == ResourcePropertySource.class
     * }</pre>
     *
     * @return the {@link Class} of the annotation type
     */
    public final Class<A> getAnnotationType() {
        return annotationType();
    }

    /**
     * Returns the resource location values specified by the {@code value} attribute of the annotation.
     * These are the resource paths from which properties will be loaded.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = {"classpath*:/META-INF/test/*.properties"})
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String[] values = attributes.getValue(); // ["classpath*:/META-INF/test/*.properties"]
     * }</pre>
     *
     * @return an array of resource location strings
     */
    public final String[] getValue() {
        return getStringArray("value");
    }

    /**
     * Returns the {@link Comparator} class used to sort the resolved {@link Resource} instances
     * before loading them into the property source.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   Class<? extends Comparator<Resource>> comparatorClass = attributes.getResourceComparatorClass();
     *   // defaults to DefaultResourceComparator.class
     * }</pre>
     *
     * @return the {@link Class} of the {@link Resource} {@link Comparator}
     */
    public final Class<? extends Comparator<Resource>> getResourceComparatorClass() {
        return getClass("resourceComparator");
    }

    /**
     * Returns whether a missing resource should be silently ignored rather than
     * throwing an exception.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/not-found.properties", ignoreResourceNotFound = true)
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   boolean ignore = attributes.isIgnoreResourceNotFound(); // true
     * }</pre>
     *
     * @return {@code true} if missing resources should be ignored, {@code false} otherwise
     */
    public final boolean isIgnoreResourceNotFound() {
        return getBoolean("ignoreResourceNotFound");
    }

    /**
     * Returns the character encoding to use when reading the property source resources.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // Given @ResourcePropertySource(value = "classpath*:/META-INF/test/*.properties", encoding = "UTF-8")
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String encoding = attributes.getEncoding(); // "UTF-8"
     * }</pre>
     *
     * @return the character encoding name, or an empty string if not specified
     */
    public final String getEncoding() {
        return getString("encoding");
    }

    /**
     * Returns the {@link PropertySourceFactory} class used to create the
     * {@link org.springframework.core.env.PropertySource} from the resolved resources.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   Class<? extends PropertySourceFactory> factoryClass = attributes.getPropertySourceFactoryClass();
     *   // defaults to DefaultPropertySourceFactory.class
     * }</pre>
     *
     * @return the {@link Class} of the {@link PropertySourceFactory}
     */
    public final Class<? extends PropertySourceFactory> getPropertySourceFactoryClass() {
        return getClass("factory");
    }

    /**
     * Returns a string representation of these annotation attributes in the format
     * {@code @AnnotationType(attr1="value1",attr2=value2)}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   PropertySourceExtensionAttributes<ResourcePropertySource> attributes = ...;
     *   String str = attributes.toString();
     *   // e.g. "@io.microsphere...ResourcePropertySource(name="test-property-source",value=...)"
     * }</pre>
     *
     * @return a non-null string representation of the annotation attributes
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("@")
                .append(getAnnotationType().getName())
                .append("(");

        for (Entry<String, Object> entry : entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            boolean isStringValue = value instanceof String;

            stringBuilder.append(name).append('=');

            if (isStringValue) {
                stringBuilder.append('"');
            }

            stringBuilder.append(value);

            if (isStringValue) {
                stringBuilder.append('"');
            }

            stringBuilder.append(',');

        }

        stringBuilder.setCharAt(stringBuilder.length() - 1, ')');

        return stringBuilder.toString();
    }
}
