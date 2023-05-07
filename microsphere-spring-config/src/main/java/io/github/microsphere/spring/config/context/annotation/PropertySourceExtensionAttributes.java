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
package io.github.microsphere.spring.config.context.annotation;

import io.github.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.lang.Nullable;

import java.lang.annotation.Annotation;
import java.util.Map;

/**
 * {@link AnnotationAttributes} for the annotation meta-annotated {@link PropertySourceExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertySourceExtension
 * @since 1.0.0
 */
public class PropertySourceExtensionAttributes<A extends Annotation> extends ResolvablePlaceholderAnnotationAttributes<A> {

    public PropertySourceExtensionAttributes(A annotation, @Nullable PropertyResolver propertyResolver) {
        super(annotation, propertyResolver);
    }

    public PropertySourceExtensionAttributes(AnnotationAttributes another, @Nullable PropertyResolver propertyResolver) {
        super(another, propertyResolver);
    }

    public PropertySourceExtensionAttributes(Map<String, Object> another, Class<A> annotationType, @Nullable PropertyResolver propertyResolver) {
        super(another, annotationType, propertyResolver);
    }

    public String getName() {
        return getString("name");
    }

    public boolean isAutoRefreshed() {
        return getBoolean("autoRefreshed");
    }

    public boolean isFirstPropertySource() {
        return getBoolean("first");
    }

    public String getBeforePropertySourceName() {
        return getString("before");
    }

    public String getAfterPropertySourceName() {
        return getString("after");
    }

    public Class<A> getAnnotationType() {
        return annotationType();
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
