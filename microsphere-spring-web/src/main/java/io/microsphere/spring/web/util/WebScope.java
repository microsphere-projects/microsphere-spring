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

package io.microsphere.spring.web.util;

import io.microsphere.annotation.Nonnull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;

import java.util.Map;

import static io.microsphere.collection.MapUtils.newFixedHashMap;
import static io.microsphere.util.ArrayUtils.length;
import static io.microsphere.util.Assert.assertNotNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * The enumeration of scopes for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributes#SCOPE_REQUEST
 * @see RequestAttributes#SCOPE_SESSION
 * @since 1.0.0
 */
public enum WebScope {

    /**
     * Request Scope
     *
     * @see RequestAttributes#SCOPE_REQUEST
     */
    REQUEST(SCOPE_REQUEST),

    /**
     * Session Scope
     *
     * @see RequestAttributes#SCOPE_SESSION
     */
    SESSION(SCOPE_SESSION);

    private final int value;

    WebScope(int value) {
        this.value = value;
    }

    /**
     * Get the value of the scope
     *
     * @return the value of the scope
     */
    public int value() {
        return value;
    }

    /**
     * Return the request attribute value if present.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param name              the attribute name
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nullable
    public <T> T getAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name) {
        if (name == null) {
            return null;
        }
        return (T) requestAttributes.getAttribute(name, this.value);
    }

    /**
     * Set the request attribute value.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param name              the attribute name
     * @param value             the attribute value
     * @return the previous attribute value if the attribute exists, or <code>null</code>
     */
    public Object setAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name, @Nullable Object value) {
        if (name == null || value == null) {
            return null;
        }
        Object previousValue = getAttribute(requestAttributes, name);
        requestAttributes.setAttribute(name, value, this.value);
        return previousValue;
    }

    /**
     * Remove the request attribute by the specified name.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param name              the attribute name
     * @return the removed attribute value if the attribute exists, or <code>null</code>
     */
    public Object removeAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name) {
        if (name == null) {
            return null;
        }
        Object previousValue = getAttribute(requestAttributes, name);
        requestAttributes.removeAttribute(name, this.value);
        return previousValue;
    }

    /**
     * Return the request attribute value or if not present raise an
     * {@link IllegalArgumentException}.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param name              the attribute name
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nonnull
    public <T> T getRequiredAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name) {
        T value = getAttribute(requestAttributes, name);
        assertNotNull(value, () -> "Required attribute '" + name + "' is missing");
        return value;
    }

    /**
     * Return the request attribute value, or a default, fallback value.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @param name              the attribute name
     * @param defaultValue      a default value to return instead
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nullable
    public <T> T getAttributeOrDefault(@Nonnull RequestAttributes requestAttributes, @Nullable String name,
                                       @Nullable T defaultValue) {
        T value = getAttribute(requestAttributes, name);
        return value != null ? value : defaultValue;
    }

    /**
     * Get all attribute names in the specified {@link RequestAttributes}.
     *
     * @param requestAttributes {@link RequestAttributes}
     * @return an array of attribute names, never <code>null</code>
     */
    @Nonnull
    public String[] getAttributeNames(@Nonnull RequestAttributes requestAttributes) {
        return requestAttributes.getAttributeNames(this.value);
    }

    @Nonnull
    public Map<String, Object> getAttributes(@Nonnull RequestAttributes requestAttributes) {
        String[] attributeNames = getAttributeNames(requestAttributes);
        int length = length(attributeNames);
        Map<String, Object> attributes = newFixedHashMap(length);
        for (int i = 0; i < length; i++) {
            String attributeName = attributeNames[i];
            attributes.put(attributeName, getAttribute(requestAttributes, attributeName));
        }
        return attributes;
    }

    /**
     * Resolve the {@link WebScope} by the specified scope value
     *
     * @param scope the scope value
     * @return the {@link WebScope}
     * @throws IllegalArgumentException if the scope value is not recognized
     * @see #value()
     * @see #REQUEST
     * @see #SESSION
     */
    @Nonnull
    public static WebScope valueOf(int scope) throws IllegalArgumentException {
        for (WebScope attributeScope : values()) {
            if (attributeScope.value == scope) {
                return attributeScope;
            }
        }
        throw new IllegalArgumentException("Unknown RequestAttributeScope value: " + scope);
    }

    /**
     * Get the attribute value by the specified name
     *
     * @param requestAttributes {@link RequestAttributes} source
     * @param name              the attribute name
     * @param scope             the scope value
     * @param <T>               the attribute value type
     * @return the attribute value or {@code null} if not found
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static <T> T getAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).getAttribute(requestAttributes, name);
    }

    /**
     * Set the attribute value
     *
     * @param requestAttributes {@link RequestAttributes} source
     * @param name              the attribute name
     * @param value             the attribute value
     * @param scope             the scope value
     * @return the previous attribute value if the attribute exists, or <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static Object setAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name,
                                      @Nullable Object value, int scope) throws IllegalArgumentException {
        return valueOf(scope).setAttribute(requestAttributes, name, value);
    }

    /**
     * Remove the attribute by the specified name
     *
     * @param requestAttributes {@link RequestAttributes} source
     * @param name              the attribute name
     * @param scope             the scope value
     * @return the removed attribute value if the attribute exists, or <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static Object removeAttribute(@Nonnull RequestAttributes requestAttributes, @Nullable String name, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).removeAttribute(requestAttributes, name);
    }

    /**
     * Get all attribute names in the specified {@link RequestAttributes} by the scope
     *
     * @param requestAttributes {@link RequestAttributes} source
     * @param scope             the scope value
     * @return an array of attribute names, never <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    public static String[] getAttributeNames(@Nonnull RequestAttributes requestAttributes, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).getAttributeNames(requestAttributes);
    }
}