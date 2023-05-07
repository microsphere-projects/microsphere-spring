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
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

import static io.github.microsphere.spring.util.AnnotationUtils.getAnnotationAttributes;

/**
 * The resolvable of {@link AnnotationAttributes} for placeholders
 *
 * @param <A> The type of {@link Annotation}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericAnnotationAttributes
 * @since 1.0.0
 */
public class PlaceholderResolvableAnnotationAttributes<A extends Annotation> extends GenericAnnotationAttributes<A> {

    private final PropertyResolver propertyResolver;

    public PlaceholderResolvableAnnotationAttributes(A annotation, @Nullable PropertyResolver propertyResolver) {
        super(annotation);
        this.propertyResolver = propertyResolver;
    }

    public PlaceholderResolvableAnnotationAttributes(AnnotationAttributes another, @Nullable PropertyResolver propertyResolver) {
        super(another);
        this.propertyResolver = propertyResolver;
    }

    @Override
    public String getString(String attributeName) {
        return super.getString(attributeName);
    }

    @Override
    public String[] getStringArray(String attributeName) {
        String[] values = super.getStringArray(attributeName);
        for (int i = 0; i < values.length; i++) {
            values[i] = resolvePlaceholders(values[i]);
        }
        return values;
    }

    private String resolvePlaceholders(String text) {
        return propertyResolver == null ? text : propertyResolver.resolvePlaceholders(text);
    }
}
