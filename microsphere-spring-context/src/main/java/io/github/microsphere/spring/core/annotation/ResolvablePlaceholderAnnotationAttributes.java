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
package io.github.microsphere.spring.core.annotation;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static io.github.microsphere.collection.MapUtils.shallowCloneMap;
import static io.github.microsphere.spring.util.AnnotationUtils.findAnnotationType;
import static io.github.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
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

    public ResolvablePlaceholderAnnotationAttributes(A annotation, @Nullable PropertyResolver propertyResolver) {
        this(getAnnotationAttributes(annotation, false), (Class<A>) annotation.annotationType(), propertyResolver);
    }

    public ResolvablePlaceholderAnnotationAttributes(AnnotationAttributes another, @Nullable PropertyResolver propertyResolver) {
        this(another, findAnnotationType(another), propertyResolver);
    }

    public ResolvablePlaceholderAnnotationAttributes(Map<String, Object> another, Class<A> annotationType, @Nullable PropertyResolver propertyResolver) {
        super(resolvePlaceholders(another, propertyResolver), annotationType);
    }

    private static Map<String, Object> resolvePlaceholders(Map<String, Object> source, @Nullable PropertyResolver propertyResolver) {
        if (source instanceof ResolvablePlaceholderAnnotationAttributes) {
            // source has been resolved
            return source;
        }
        Map<String, Object> copy = shallowCloneMap(source);
        for (Map.Entry<String, Object> entry : copy.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                entry.setValue(resolvePlaceholder((String) value, propertyResolver));
            } else if (value instanceof String[]) {
                entry.setValue(resolvePlaceholders((String[]) value, propertyResolver));
            }
        }
        return copy;
    }

    private static String[] resolvePlaceholders(String[] values, @Nullable PropertyResolver propertyResolver) {
        for (int i = 0; i < values.length; i++) {
            values[i] = resolvePlaceholder(values[i], propertyResolver);
        }
        return values;
    }

    private static String resolvePlaceholder(String value, @Nullable PropertyResolver propertyResolver) {
        return propertyResolver == null ? value : propertyResolver.resolvePlaceholders(value);
    }

    /**
     * Create an instance of {@link ResolvablePlaceholderAnnotationAttributes} from the specified {@link Annotation annotation}
     *
     * @param attributes       {@link AnnotationAttributes}
     * @param propertyResolver {@link PropertyResolver}
     * @param <A>              the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @NonNull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@NonNull AnnotationAttributes attributes,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        if (attributes instanceof ResolvablePlaceholderAnnotationAttributes) {
            return (ResolvablePlaceholderAnnotationAttributes) attributes;
        }
        return new ResolvablePlaceholderAnnotationAttributes(attributes, propertyResolver);
    }

    /**
     * Create an instance of {@link ResolvablePlaceholderAnnotationAttributes} from the specified {@link Annotation annotation}
     *
     * @param annotation       {@link Annotation annotation}
     * @param propertyResolver {@link PropertyResolver}
     * @param <A>              the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @NonNull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(@NonNull A annotation,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes(annotation, propertyResolver);
    }

    /**
     * Create an instance of {@link ResolvablePlaceholderAnnotationAttributes} from the specified {@link Annotation annotation}
     *
     * @param attributes       The {@link Map} for the attributes of annotation
     * @param propertyResolver {@link PropertyResolver}
     * @param <A>              the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @NonNull
    public static <A extends Annotation> ResolvablePlaceholderAnnotationAttributes<A> of(Map<String, Object> attributes,
                                                                                         Class<A> annotationType,
                                                                                         @Nullable PropertyResolver propertyResolver) {
        return new ResolvablePlaceholderAnnotationAttributes(attributes, annotationType, propertyResolver);
    }

    /**
     * Create a {@link Set set} of {@link GenericAnnotationAttributes}
     *
     * @param attributesArray
     * @param propertyResolver
     * @return non-null
     */
    @NonNull
    public static Set<AnnotationAttributes> ofSet(@Nullable AnnotationAttributes[] attributesArray, @Nullable PropertyResolver propertyResolver) {
        int length = attributesArray == null ? 0 : attributesArray.length;

        if (length < 1) {
            return emptySet();
        }

        Set<AnnotationAttributes> annotationAttributesSet = new LinkedHashSet<>();
        for (int i = 0; i < length; i++) {
            AnnotationAttributes annotationAttributes = attributesArray[i];
            annotationAttributesSet.add(of(annotationAttributes, propertyResolver));
        }

        return annotationAttributesSet;
    }
}
