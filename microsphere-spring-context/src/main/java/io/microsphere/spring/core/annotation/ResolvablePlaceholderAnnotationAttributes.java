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
package io.microsphere.spring.core.annotation;

import io.microsphere.annotation.Immutable;
import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.core.env.PropertyResolverUtils;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.spring.core.annotation.AnnotationUtils.findAnnotationType;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.util.ArrayUtils.length;
import static java.util.Collections.emptySet;

/**
 * The resolvable placeholders of {@link AnnotationAttributes}
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericAnnotationAttributes
 * @since 1.0.0
 */
public class ResolvablePlaceholderAnnotationAttributes<A extends Annotation> extends GenericAnnotationAttributes<A> {

    /**
     * Constructs a new {@link ResolvablePlaceholderAnnotationAttributes} instance from the specified {@link Annotation}.
     * <p>
     * Placeholders in the annotation attribute values will be resolved using the provided {@link PropertyResolver}.
     * If the {@code propertyResolver} is {@code null}, placeholders will remain unresolved.
     *
     * @param annotation       the source annotation, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @example <pre>{@code
     * // Given an annotation instance and a PropertyResolver
     * MyAnnotation annotation = ...;
     * PropertyResolver resolver = ...;
     *
     * // Create ResolvablePlaceholderAnnotationAttributes with placeholder resolution
     * ResolvablePlaceholderAnnotationAttributes<MyAnnotation> resolvableAttrs =
     *     new ResolvablePlaceholderAnnotationAttributes<>(annotation, resolver);
     * }</pre>
     */
    public ResolvablePlaceholderAnnotationAttributes(@Nonnull A annotation, @Nullable PropertyResolver propertyResolver) {
        this(getAnnotationAttributes(annotation, false), (Class<A>) annotation.annotationType(), propertyResolver);
    }

    /**
     * Constructs a new {@link ResolvablePlaceholderAnnotationAttributes} instance by copying the attributes
     * from another {@link GenericAnnotationAttributes}.
     * <p>
     * The placeholders in the attributes will not be resolved as no {@link PropertyResolver} is provided.
     *
     * @param another the source {@link GenericAnnotationAttributes} to copy attributes from, must not be {@code null}
     * @see #ResolvablePlaceholderAnnotationAttributes(GenericAnnotationAttributes, org.springframework.core.env.PropertyResolver)
     */
    public ResolvablePlaceholderAnnotationAttributes(GenericAnnotationAttributes<A> another) {
        this(another, null);
    }

    /**
     * Constructs a new {@link ResolvablePlaceholderAnnotationAttributes} instance by copying the attributes
     * from another {@link GenericAnnotationAttributes} and resolving placeholders using the given {@link PropertyResolver}.
     * <p>
     * If the {@code propertyResolver} is {@code null}, placeholders in the attributes will not be resolved.
     *
     * @param another          the source {@link GenericAnnotationAttributes} to copy attributes from, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @see #ResolvablePlaceholderAnnotationAttributes(GenericAnnotationAttributes)
     * @see PropertyResolverUtils#resolvePlaceholders(Map, PropertyResolver)
     */
    public ResolvablePlaceholderAnnotationAttributes(GenericAnnotationAttributes<A> another, @Nullable PropertyResolver propertyResolver) {
        this(another, findAnnotationType(another), propertyResolver);
    }

    /**
     * Constructs a new {@link ResolvablePlaceholderAnnotationAttributes} instance with the specified attributes,
     * annotation type, and property resolver.
     * <p>
     * Placeholders in the attribute values will be resolved using the provided {@link PropertyResolver}.
     * If the {@code propertyResolver} is {@code null}, placeholders will remain unresolved.
     *
     * @param another          the map of annotation attributes, must not be {@code null}
     * @param annotationType   the type of the annotation, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @see PropertyResolverUtils#resolvePlaceholders(Map, PropertyResolver)
     */
    public ResolvablePlaceholderAnnotationAttributes(Map<String, Object> another, @Nonnull Class<A> annotationType, @Nullable PropertyResolver propertyResolver) {
        super(resolvePlaceholders(another, propertyResolver), annotationType);
    }

    private static Map<String, Object> resolvePlaceholders(Map<String, Object> source, @Nullable PropertyResolver propertyResolver) {
        if (source instanceof ResolvablePlaceholderAnnotationAttributes) {
            // source has been resolved
            return source;
        }
        return PropertyResolverUtils.resolvePlaceholders(source, propertyResolver);
    }

    /**
     * Creates a {@link ResolvablePlaceholderAnnotationAttributes} instance from the given {@link GenericAnnotationAttributes}.
     * <p>
     * The placeholders in the attributes will not be resolved as no {@link PropertyResolver} is provided.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an existing GenericAnnotationAttributes instance
     * GenericAnnotationAttributes<MyAnnotation> attrs = ...;
     *
     * // Create a ResolvablePlaceholderAnnotationAttributes without resolving placeholders
     * ResolvablePlaceholderAnnotationAttributes<MyAnnotation> resolvableAttrs =
     *     ResolvablePlaceholderAnnotationAttributes.of(attrs);
     * }</pre>
     *
     * @param attributes the source {@link GenericAnnotationAttributes}, must not be {@code null}
     * @param <A>        the type of the annotation
     * @return a new {@link ResolvablePlaceholderAnnotationAttributes} instance
     */
    @Nonnull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@Nonnull GenericAnnotationAttributes<A> attributes) {
        return new ResolvablePlaceholderAnnotationAttributes<>(attributes);
    }

    /**
     * Creates a {@link ResolvablePlaceholderAnnotationAttributes} instance from the given {@link GenericAnnotationAttributes}
     * and resolves placeholders using the provided {@link PropertyResolver}.
     * <p>
     * If the {@code propertyResolver} is {@code null}, placeholders in the attributes will not be resolved.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an existing GenericAnnotationAttributes instance and a PropertyResolver
     * GenericAnnotationAttributes<MyAnnotation> attrs = ...;
     * PropertyResolver resolver = ...;
     *
     * // Create a ResolvablePlaceholderAnnotationAttributes with placeholder resolution
     * ResolvablePlaceholderAnnotationAttributes<MyAnnotation> resolvableAttrs =
     *     ResolvablePlaceholderAnnotationAttributes.of(attrs, resolver);
     * }</pre>
     *
     * @param attributes       the source {@link GenericAnnotationAttributes}, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @param <A>              the type of the annotation
     * @return a new {@link ResolvablePlaceholderAnnotationAttributes} instance
     */
    @Nonnull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@Nonnull GenericAnnotationAttributes<A> attributes,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        if (attributes instanceof ResolvablePlaceholderAnnotationAttributes) {
            return (ResolvablePlaceholderAnnotationAttributes) attributes;
        }
        return new ResolvablePlaceholderAnnotationAttributes(attributes, propertyResolver);
    }

    /**
     * Creates a {@link ResolvablePlaceholderAnnotationAttributes} instance from the given {@link Annotation}
     * and resolves placeholders using the provided {@link PropertyResolver}.
     * <p>
     * If the {@code propertyResolver} is {@code null}, placeholders in the annotation attribute values will not be resolved.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an annotation instance and a PropertyResolver
     * MyAnnotation annotation = ...;
     * PropertyResolver resolver = ...;
     *
     * // Create a ResolvablePlaceholderAnnotationAttributes with placeholder resolution
     * ResolvablePlaceholderAnnotationAttributes<MyAnnotation> resolvableAttrs =
     *     ResolvablePlaceholderAnnotationAttributes.of(annotation, resolver);
     * }</pre>
     *
     * @param annotation       the source annotation, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @param <A>              the type of the annotation
     * @return a new {@link ResolvablePlaceholderAnnotationAttributes} instance
     */
    @Nonnull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@Nonnull A annotation,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes(annotation, propertyResolver);
    }

    /**
     * Creates a {@link ResolvablePlaceholderAnnotationAttributes} instance from the given map of attributes,
     * annotation type, and resolves placeholders using the provided {@link PropertyResolver}.
     * <p>
     * If the {@code propertyResolver} is {@code null}, placeholders in the attribute values will not be resolved.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given a map of attributes, annotation type, and a PropertyResolver
     * Map<String, Object> attrs = new HashMap<>();
     * attrs.put("value", "${my.property}");
     * Class<MyAnnotation> annotationType = MyAnnotation.class;
     * PropertyResolver resolver = ...;
     *
     * // Create a ResolvablePlaceholderAnnotationAttributes with placeholder resolution
     * ResolvablePlaceholderAnnotationAttributes<MyAnnotation> resolvableAttrs =
     *     ResolvablePlaceholderAnnotationAttributes.of(attrs, annotationType, resolver);
     * }</pre>
     *
     * @param attributes       the map of annotation attributes, must not be {@code null}
     * @param annotationType   the type of the annotation, must not be {@code null}
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @param <A>              the type of the annotation
     * @return a new {@link ResolvablePlaceholderAnnotationAttributes} instance
     */
    @Nonnull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@Nonnull Map<String, Object> attributes,
                                                                                         Class<A> annotationType,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes(attributes, annotationType, propertyResolver);
    }

    /**
     * Creates a {@link Set} of {@link AnnotationAttributes} from the given array of {@link GenericAnnotationAttributes},
     * resolving placeholders using the provided {@link PropertyResolver}.
     * <p>
     * If the {@code propertyResolver} is {@code null}, placeholders in the attribute values will not be resolved.
     * If the {@code attributesArray} is {@code null} or empty, an empty set is returned.
     * <p>
     * <h3>Example Usage</h3>
     * <pre>{@code
     * // Given an array of GenericAnnotationAttributes and a PropertyResolver
     * GenericAnnotationAttributes<MyAnnotation>[] attrsArray = ...;
     * PropertyResolver resolver = ...;
     *
     * // Create a Set of ResolvablePlaceholderAnnotationAttributes with placeholder resolution
     * Set<AnnotationAttributes> resolvableAttrsSet =
     *     ResolvablePlaceholderAnnotationAttributes.ofSet(attrsArray, resolver);
     * }</pre>
     *
     * @param attributesArray  the array of source {@link GenericAnnotationAttributes}, may be {@code null} or empty
     * @param propertyResolver the {@link PropertyResolver} used to resolve placeholders in attribute values, may be {@code null}
     * @return a {@link Set} of {@link AnnotationAttributes} with resolved placeholders, or an empty set if the input is null or empty
     */
    @Nonnull
    @Immutable
    public static Set<AnnotationAttributes> ofSet(@Nullable GenericAnnotationAttributes[] attributesArray, @Nullable PropertyResolver propertyResolver) {
        int length = length(attributesArray);

        if (length < 1) {
            return emptySet();
        }

        Set<AnnotationAttributes> annotationAttributesSet = newLinkedHashSet();
        for (int i = 0; i < length; i++) {
            GenericAnnotationAttributes annotationAttributes = attributesArray[i];
            annotationAttributesSet.add(of(annotationAttributes, propertyResolver));
        }

        return annotationAttributesSet;
    }
}