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

import org.springframework.lang.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import static io.microsphere.util.Assert.assertNotNull;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * The enumeration of Request Attribute Scopes for Spring Web
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributes#SCOPE_REQUEST
 * @see RequestAttributes#SCOPE_SESSION
 * @since 1.0.0
 */
@SuppressWarnings("unchecked")
public enum RequestAttributeScope {

    /**
     * Request Scope
     */
    REQUEST(SCOPE_REQUEST) {
        @Override
        public <T> T getAttribute(ServerWebExchange serverWebExchange, String name) {
            return serverWebExchange.getAttribute(name);
        }
    },

    /**
     * Session Scope
     */
    SESSION(SCOPE_SESSION) {
        @Override
        public <T> T getAttribute(ServerWebExchange serverWebExchange, String name) {
            Mono<WebSession> session = serverWebExchange.getSession();
            WebSession webSession = session.block();
            return webSession == null ? null : (T) webSession.getAttribute(name);
        }
    };

    private final int value;

    RequestAttributeScope(int value) {
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
    public abstract <T> T getAttribute(ServerWebExchange serverWebExchange, String name);

    /**
     * Return the request attribute value or if not present raise an
     * {@link IllegalArgumentException}.
     *
     * @param serverWebExchange {@link ServerWebExchange}
     * @param name              the attribute name
     * @param <T>               the attribute type
     * @return the attribute value
     */
    public <T> T getRequiredAttribute(ServerWebExchange serverWebExchange, String name) {
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
    public <T> T getAttributeOrDefault(ServerWebExchange serverWebExchange, String name, T defaultValue) {
        T value = getAttribute(serverWebExchange, name);
        return value != null ? value : defaultValue;
    }

    /**
     * Resolve the {@link RequestAttributeScope} by the specified scope value
     *
     * @param scope the scope value
     * @return the {@link RequestAttributeScope}
     * @throws IllegalArgumentException if the scope value is not recognized
     * @see #value()
     * @see #REQUEST
     * @see #SESSION
     */
    public static RequestAttributeScope valueOf(int scope) {
        for (RequestAttributeScope requestAttributeScope : values()) {
            if (requestAttributeScope.value == scope) {
                return requestAttributeScope;
            }
        }
        throw new IllegalArgumentException("Unknown RequestAttributeScope value: " + scope);
    }
}
