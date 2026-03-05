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

package io.microsphere.spring.core.env;

import io.microsphere.annotation.Nullable;
import io.microsphere.util.Utils;
import org.springframework.core.env.PropertyResolver;

import java.util.Map;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * The utilties class for {@link PropertyResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see PropertyResolver
 * @since 1.0.0
 */
public abstract class PropertyResolverUtils implements Utils {

    /**
     * Resolve the placeholders in the source {@link Map}
     *
     * @param source           the source {@link Map}
     * @param propertyResolver the {@link PropertyResolver}
     * @return the resolved {@link Map}
     */
    public static Map<String, Object> resolvePlaceholders(Map<String, Object> source, @Nullable PropertyResolver propertyResolver) {
        if (isEmpty(source) || propertyResolver == null) {
            return source;
        }
        Map<String, Object> copy = newLinkedHashMap(source);
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof String) {
                value = resolvePlaceholders((String) value, propertyResolver);
            } else if (value instanceof String[]) {
                value = resolvePlaceholders((String[]) value, propertyResolver);
            }
            copy.put(key, value);
        }
        return copy;
    }

    /**
     * Resolve the placeholders in the source {@link String} array
     *
     * @param values           the source {@link String} array
     * @param propertyResolver the {@link PropertyResolver}
     * @return the resolved {@link String} array
     */
    @Nullable
    public static String[] resolvePlaceholders(@Nullable String[] values, @Nullable PropertyResolver propertyResolver) {
        if (propertyResolver == null) {
            return values;
        }
        int length = length(values);
        if (length < 1) {
            return values;
        }
        String[] newValues = new String[length];
        for (int i = 0; i < length; i++) {
            newValues[i] = resolvePlaceholders(values[i], propertyResolver);
        }
        return newValues;
    }

    /**
     * Resolve the placeholders in the source {@link String}
     *
     * @param attributeValue   the source {@link String}
     * @param propertyResolver the {@link PropertyResolver}
     * @return the resolved {@link String}
     */
    public static String resolvePlaceholders(@Nullable String attributeValue, @Nullable PropertyResolver propertyResolver) {
        if (isBlank(attributeValue) || propertyResolver == null) {
            return attributeValue;
        }
        return propertyResolver.resolvePlaceholders(attributeValue);
    }

    private PropertyResolverUtils() {
    }
}