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

package io.microsphere.spring.web.idempotent;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.web.util.WebTarget;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.web.util.WebRequestUtils.getMethod;
import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static java.util.UUID.randomUUID;

/**
 * The {@link Idempotent} Service
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Idempotent
 * @since 1.0.0
 */
public interface IdempotentService {

    /**
     * Generate a new token.
     * <p>
     * Note : The new token should not be generated twice in one request.
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @return the new token
     */
    @Nonnull
    default String generateToken(NativeWebRequest request, IdempotentAttributes attributes) {
        String tokenName = attributes.getTokenName();
        String newToken = REQUEST.getAttribute(request, tokenName);
        if (newToken == null) {
            newToken = randomUUID().toString();
            REQUEST.setAttribute(request, tokenName, newToken);
        }
        return newToken;
    }

    /**
     * Get the token from the request.
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @return the token from the request if exists
     */
    @Nullable
    String getToken(NativeWebRequest request, IdempotentAttributes attributes);

    /**
     * Store the token into backend storage.
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @param newToken   the new generated token
     * @return <code>true</code> if stored successfully
     */
    boolean storeToken(NativeWebRequest request, IdempotentAttributes attributes, String newToken);

    /**
     * Check the token is existed
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @param token      the token
     * @return <code>true</code> if exists
     */
    boolean checkToken(NativeWebRequest request, IdempotentAttributes attributes, String token);

    /**
     * Invalidate the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @param token      the token
     * @return <code>true</code> if invalidated successfully
     */
    boolean invalidate(NativeWebRequest request, IdempotentAttributes attributes, String token);

    /**
     * Renew the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @return the new token
     */
    default String renewToken(NativeWebRequest request, IdempotentAttributes attributes) {
        String newToken = generateToken(request, attributes);
        String tokenName = attributes.getTokenName();
        WebTarget target = attributes.getTarget();
        target.writeValue(request, tokenName, newToken);
        return newToken;
    }

    /**
     * Validate the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param attributes the {@link IdempotentAttributes}
     * @throws IdempotentException if the validated token is invalid
     */
    default void validateToken(NativeWebRequest request, IdempotentAttributes attributes) throws IdempotentException {
        String method = getMethod(request);
        if (attributes.isValidatedMethod(method)) {
            String token = getToken(request, attributes);
            if (token == null) {
                throw new IdempotentException("the request token is missing.");
            }
            if (!checkToken(request, attributes, token)) {
                throw new IdempotentException("the request token is invalid.");
            }
            if (!storeToken(request, attributes, token)) {
                throw new IdempotentException("the request token is existed.");
            }
            renewToken(request, attributes);
            invalidate(request, attributes, token);
        }
    }

    /**
     * Destroy
     */
    void destroy();
}
