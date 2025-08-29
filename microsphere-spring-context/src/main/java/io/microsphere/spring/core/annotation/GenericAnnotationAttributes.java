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
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.microsphere.collection.SetUtils.newFixedLinkedHashSet;
import static io.microsphere.spring.core.annotation.AnnotationUtils.findAnnotationType;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;
import static io.microsphere.util.Assert.assertNotNull;
import static java.util.Arrays.deepHashCode;
import static java.util.Arrays.deepToString;
import static java.util.Collections.emptySet;
import static java.util.Objects.deepEquals;

/**
 * Generic {@link AnnotationAttributes}
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationAttributes
 * @since 1.0.0
 */
public class GenericAnnotationAttributes<A extends Annotation> extends AnnotationAttributes {

    @Nonnull
    private final Class<A> annotationType;

    public GenericAnnotationAttributes(A annotation) {
        this(getAnnotationAttributes(annotation, false), (Class<A>) annotation.annotationType());
    }

    public GenericAnnotationAttributes(AnnotationAttributes attributes) {
        this(attributes, findAnnotationType(attributes));
    }

    public GenericAnnotationAttributes(Map<String, Object> another, @Nonnull Class<A> annotationType) {
        super(another);
        assertNotNull(annotationType, () -> "The annotation type must not be null");
        this.annotationType = annotationType;
    }

    /**
     * Get The {@link Class class} of {@link Annotation}.
     * <p>
     * Current method will override the super classes' method since Spring Framework 4.2
     *
     * @return non-null
     */
    @Nonnull
    public Class<A> annotationType() {
        return annotationType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationAttributes)) {
            return false;
        }

        AnnotationAttributes that = (AnnotationAttributes) o;

        if (!this.annotationType().equals(that.annotationType())) {
            return false;
        }

        if (this.size() == that.size()) {
            for (Entry<String, Object> entry : this.entrySet()) {
                String attributeName = entry.getKey();
                Object attributeValue = entry.getValue();
                Object thatAttributeValue = that.get(attributeName);
                if (!deepEquals(attributeValue, thatAttributeValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Entry<String, Object> entry : this.entrySet()) {
            String attributeName = entry.getKey();
            h += 31 * attributeName.hashCode();
            Object attributeValue = entry.getValue();
            if (attributeValue != null) {
                Class<?> attributeValueType = attributeValue.getClass();
                if (attributeValueType.isArray()) {
                    h += 31 * deepHashCode((Object[]) attributeValue);
                } else {
                    h += 31 * attributeValue.hashCode();
                }
            }
        }
        return h;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("@")
                .append(annotationType().getName())
                .append("(");
        for (Entry<String, Object> entry : entrySet()) {
            String name = entry.getKey();
            Object value = entry.getValue();
            Class<?> valueType = value.getClass();

            stringBuilder.append(name).append('=');

            if (CharSequence.class.isAssignableFrom(valueType)) {
                stringBuilder.append('"').append(value).append('"');
            } else if (valueType.isArray()) {
                stringBuilder.append(deepToString((Object[]) value));
            } else {
                stringBuilder.append(value);
            }
            stringBuilder.append(',');
        }
        stringBuilder.setCharAt(stringBuilder.length() - 1, ')');
        return stringBuilder.toString();
    }


    /**
     * Create an instance of {@link GenericAnnotationAttributes} from the specified {@link Annotation annotation}
     *
     * @param annotation {@link Annotation annotation}
     * @param <A>        the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @Nonnull
    public static <A extends Annotation> GenericAnnotationAttributes<A> of(@Nonnull A annotation) {
        return new GenericAnnotationAttributes(annotation);
    }

    /**
     * Create an instance of {@link GenericAnnotationAttributes} from the specified {@link AnnotationAttributes}
     *
     * @param attributes {@link AnnotationAttributes annotationAttributes}
     * @param <A>        the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @Nonnull
    public static <A extends Annotation> GenericAnnotationAttributes<A> of(@Nonnull AnnotationAttributes attributes) {
        if (attributes instanceof GenericAnnotationAttributes) {
            return (GenericAnnotationAttributes) attributes;
        }
        return new GenericAnnotationAttributes(attributes);
    }

    /**
     * Create a {@link Set set} of {@link GenericAnnotationAttributes}
     *
     * @param attributesArray
     * @return non-null
     */
    @Nonnull
    @Immutable
    public static Set<AnnotationAttributes> ofSet(@Nullable AnnotationAttributes... attributesArray) {
        int length = attributesArray == null ? 0 : attributesArray.length;

        if (length < 1) {
            return emptySet();
        }

        Set<AnnotationAttributes> annotationAttributesSet = newFixedLinkedHashSet(length);
        for (int i = 0; i < length; i++) {
            AnnotationAttributes annotationAttributes = attributesArray[i];
            annotationAttributesSet.add(of(annotationAttributes));
        }

        return annotationAttributesSet;
    }
}
