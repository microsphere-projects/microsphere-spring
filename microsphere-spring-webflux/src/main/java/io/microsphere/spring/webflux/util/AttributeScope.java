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

package io.microsphere.spring.webflux.util;

import io.microsphere.annotation.Nonnull;
import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;

import java.util.Map;

import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.Assert.assertNotNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * The enumeration of attributes scopes for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributes#SCOPE_REQUEST
 * @see RequestAttributes#SCOPE_SESSION
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public enum AttributeScope {

    /**
     * Request Scope
     */
    REQUEST(SCOPE_REQUEST) {
        @Override
        protected Map<String, Object> getAttributes(ServerWebExchange serverWebExchange) {
            return serverWebExchange.getAttributes();
        }
    },

    /**
     * Session Scope
     */
    SESSION(SCOPE_SESSION) {
        @Override
        protected Map<String, Object> getAttributes(ServerWebExchange serverWebExchange) {
            WebSession webSession = getSession(serverWebExchange);
            return webSession.getAttributes();
        }

        protected WebSession getSession(ServerWebExchange serverWebExchange) {
            return serverWebExchange.getSession().block();
        }
    };

    private final int value;

    AttributeScope(int value) {
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
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nullable
    public <T> T getAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name) {
        if (name == null) {
            return null;
        }
        Map<String, Object> attributes = getAttributes(serverWebExchange);
        return (T) attributes.get(name);
    }

    /**
     * Set the request attribute value.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @param value             the attribute value
     * @return the previous attribute value if the attribute exists, or <code>null</code>
     */
    public Object setAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name, @Nullable Object value) {
        if (name == null || value == null) {
            return null;
        }
        Map<String, Object> attributes = getAttributes(serverWebExchange);
        return attributes.put(name, value);
    }

    /**
     * Remove the request attribute by the specified name.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @return the removed attribute value if the attribute exists, or <code>null</code>
     */
    public Object removeAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name) {
        if (name == null) {
            return null;
        }
        Map<String, Object> attributes = getAttributes(serverWebExchange);
        return attributes.remove(name);
    }

    /**
     * Return the request attribute value or if not present raise an
     * {@link IllegalArgumentException}.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nonnull
    public <T> T getRequiredAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name) {
        T value = getAttribute(serverWebExchange, name);
        assertNotNull(value, () -> "Required attribute '" + name + "' is missing");
        return value;
    }

    /**
     * Return the request attribute value, or a default, fallback value.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @param defaultValue      a default value to return instead
     * @param <T>               the attribute type
     * @return the attribute value
     */
    @Nullable
    public <T> T getAttributeOrDefault(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name,
                                       @Nullable T defaultValue) {
        T value = getAttribute(serverWebExchange, name);
        return value != null ? value : defaultValue;
    }

    /**
     * Get all attribute names in the specified {@link ServerWebExchange}.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @return an array of attribute names, never <code>null</code>
     */
    @Nonnull
    public String[] getAttributeNames(@Nonnull ServerWebExchange serverWebExchange) {
        return getAttributes(serverWebExchange).keySet().toArray(EMPTY_STRING_ARRAY);
    }

    @Nonnull
    protected abstract Map<String, Object> getAttributes(@Nonnull ServerWebExchange serverWebExchange);

    /**
     * Resolve the {@link AttributeScope} by the specified scope value
     *
     * @param scope the scope value
     * @return the {@link AttributeScope}
     * @throws IllegalArgumentException if the scope value is not recognized
     * @see #value()
     * @see #REQUEST
     * @see #SESSION
     */
    @Nonnull
    public static AttributeScope valueOf(int scope) throws IllegalArgumentException {
        for (AttributeScope attributeScope : values()) {
            if (attributeScope.value == scope) {
                return attributeScope;
            }
        }
        throw new IllegalArgumentException("Unknown RequestAttributeScope value: " + scope);
    }

    /**
     * Get the attribute value by the specified name
     *
     * @param serverWebExchange {@link ServerWebExchange} source
     * @param name              the attribute name
     * @param scope             the scope value
     * @param <T>               the attribute value type
     * @return the attribute value or {@code null} if not found
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static <T> T getAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).getAttribute(serverWebExchange, name);
    }

    /**
     * Set the attribute value
     *
     * @param serverWebExchange {@link ServerWebExchange} source
     * @param name              the attribute name
     * @param value             the attribute value
     * @param scope             the scope value
     * @return the previous attribute value if the attribute exists, or <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static Object setAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name,
                                      @Nullable Object value, int scope) throws IllegalArgumentException {
        return valueOf(scope).setAttribute(serverWebExchange, name, value);
    }

    /**
     * Remove the attribute by the specified name
     *
     * @param serverWebExchange {@link ServerWebExchange} source
     * @param name              the attribute name
     * @param scope             the scope value
     * @return the removed attribute value if the attribute exists, or <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    @Nullable
    public static Object removeAttribute(@Nonnull ServerWebExchange serverWebExchange, @Nullable String name, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).removeAttribute(serverWebExchange, name);
    }

    /**
     * Get all attribute names in the specified {@link ServerWebExchange} by the scope
     *
     * @param serverWebExchange {@link ServerWebExchange} source
     * @param scope             the scope value
     * @return an array of attribute names, never <code>null</code>
     * @throws IllegalArgumentException if the scope value is not recognized
     */
    public static String[] getAttributeNames(@Nonnull ServerWebExchange serverWebExchange, int scope)
            throws IllegalArgumentException {
        return valueOf(scope).getAttributeNames(serverWebExchange);
    }
}
