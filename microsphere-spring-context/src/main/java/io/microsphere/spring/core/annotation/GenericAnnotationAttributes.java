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

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.spring.util.AnnotationUtils.findAnnotationType;
import static io.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;
import static java.util.Collections.emptySet;

/**
 * Generic {@link AnnotationAttributes}
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotationAttributes
 * @since 1.0.0
 */
public class GenericAnnotationAttributes<A extends Annotation> extends AnnotationAttributes {

    @Nullable
    private final Class<A> annotationType;

    public GenericAnnotationAttributes(A annotation) {
        this(getAnnotationAttributes(annotation, false), (Class<A>) annotation.annotationType());
    }

    public GenericAnnotationAttributes(AnnotationAttributes another) {
        this(another, findAnnotationType(another));
    }

    public GenericAnnotationAttributes(Map<String, Object> another, Class<A> annotationType) {
        super(another);
        this.annotationType = annotationType;
    }

    /**
     * Get The {@link Class class} of {@link Annotation}.
     * <p>
     * Current method will override the super classes' method since Spring Framework 4.2
     *
     * @return <code>null</code> if not found
     */
    @Nullable
    public Class<A> annotationType() {
        return annotationType;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AnnotationAttributes)) {
            return false;
        }

        AnnotationAttributes that = (AnnotationAttributes) o;

        if (this.size() == that.size()) {
            for (Map.Entry<String, Object> entry : this.entrySet()) {
                String attributeName = entry.getKey();
                Object attributeValue = entry.getValue();
                Object thatAttributeValue = that.get(attributeName);
                if (!Objects.deepEquals(attributeValue, thatAttributeValue)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Map.Entry<String, Object> entry : this.entrySet()) {
            String attributeName = entry.getKey();
            h += 31 * attributeName.hashCode();
            Object attributeValue = entry.getValue();
            if (attributeValue != null) {
                Class<?> attributeValueType = attributeValue.getClass();
                if (attributeValueType.isArray()) {
                    h += 31 * Arrays.deepHashCode((Object[]) attributeValue);
                } else {
                    h += 31 * attributeValue.hashCode();
                }
            }
        }
        return h;
    }


    /**
     * Create an instance of {@link GenericAnnotationAttributes} from the specified {@link Annotation annotation}
     *
     * @param annotation {@link Annotation annotation}
     * @param <A>        the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @NonNull
    public static <A extends Annotation> GenericAnnotationAttributes<A> of(@NonNull A annotation) {
        return new GenericAnnotationAttributes(annotation);
    }

    /**
     * Create an instance of {@link GenericAnnotationAttributes} from the specified {@link AnnotationAttributes}
     *
     * @param attributes {@link AnnotationAttributes annotationAttributes}
     * @param <A>        the {@link Class class} of {@link Annotation annotation}
     * @return non-null
     */
    @NonNull
    public static <A extends Annotation> GenericAnnotationAttributes<A> of(@NonNull AnnotationAttributes attributes) {
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
    @NonNull
    public static Set<AnnotationAttributes> ofSet(@Nullable AnnotationAttributes... attributesArray) {
        int length = attributesArray == null ? 0 : attributesArray.length;

        if (length < 1) {
            return emptySet();
        }

        Set<AnnotationAttributes> annotationAttributesSet = new LinkedHashSet<>();
        for (int i = 0; i < length; i++) {
            AnnotationAttributes annotationAttributes = attributesArray[i];
            annotationAttributesSet.add(of(annotationAttributes));
        }

        return annotationAttributesSet;
    }
}
