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

import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Comparator;
import java.util.Map;

/**
 * {@link AnnotationAttributes} for the annotation meta-annotated {@link PropertySourceExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @see ResolvablePlaceholderAnnotationAttributes
 * @since 1.0.0
 */
public class PropertySourceExtensionAttributes<A extends Annotation> extends ResolvablePlaceholderAnnotationAttributes<A> {

    public PropertySourceExtensionAttributes(Map<String, Object> another, Class<A> annotationType, @Nullable PropertyResolver propertyResolver) {
        super(another, annotationType, propertyResolver);
    }

    public final String getName() {
        return getString("name");
    }

    public final boolean isAutoRefreshed() {
        return Boolean.TRUE.equals(get("autoRefreshed"));
    }

    public final boolean isFirstPropertySource() {
        return getBoolean("first");
    }

    public final String getBeforePropertySourceName() {
        return getString("before");
    }

    public final String getAfterPropertySourceName() {
        return getString("after");
    }

    public final Class<A> getAnnotationType() {
        return annotationType();
    }

    public final String[] getValue() {
        return getStringArray("value");
    }

    public final Class<? extends Comparator<Resource>> getResourceComparatorClass() {
        return getClass("resourceComparator");
    }

    public final boolean isIgnoreResourceNotFound() {
        return getBoolean("ignoreResourceNotFound");
    }

    public final String getEncoding() {
        return getString("encoding");
    }

    public final Class<? extends PropertySourceFactory> getPropertySourceFactoryClass() {
        return getClass("factory");
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("@")
                .append(getAnnotationType().getName())
                .append("(");

        for (Map.Entry<String, Object> entry : entrySet()) {
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
