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
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Objects;

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
     * @param idempotent the {@link Idempotent}
     * @return the new token
     */
    @Nonnull
    default String generateToken(NativeWebRequest request, Idempotent idempotent) {
        String tokenName = idempotent.tokenName();
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
     * @param idempotent the {@link Idempotent}
     * @return the token from the request if exists
     */
    @Nullable
    String getToken(NativeWebRequest request, Idempotent idempotent);

    /**
     * Load the last {@link #storeToken(NativeWebRequest, Idempotent, String) stored} token
     *
     * @param request    the {@link NativeWebRequest}
     * @param idempotent the {@link Idempotent}
     * @return the token if exists
     */
    @Nullable
    String loadToken(NativeWebRequest request, Idempotent idempotent);

    /**
     * Invalidate the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param idempotent the {@link Idempotent}
     * @return <code>true</code> if invalidated successfully
     */
    boolean invalidateToken(NativeWebRequest request, Idempotent idempotent);

    /**
     * Store the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param idempotent the {@link Idempotent}
     * @param newToken   the new generated token
     * @return <code>true</code> if stored successfully
     */
    boolean storeToken(NativeWebRequest request, Idempotent idempotent, String newToken);

    /**
     * Validate the token
     *
     * @param request    the {@link NativeWebRequest}
     * @param idempotent the {@link Idempotent}
     * @throws IdempotentException if the validated token is invalid
     */
    default void validateToken(NativeWebRequest request, Idempotent idempotent) throws IdempotentException {
        String lastToken = loadToken(request, idempotent);
        if (lastToken == null) {
            String newToken = generateToken(request, idempotent);
            storeToken(request, idempotent, newToken);
            return;
        }
        String token = getToken(request, idempotent);
        if (token == null) {
            throw new IdempotentException("Token is not exists from the request.");
        }
        if (!Objects.equals(lastToken, token)) {
            throw new IdempotentException("Illegal token : " + token);
        }
        // invalidate
        invalidateToken(request, idempotent);
        // renew token
        validateToken(request, idempotent);
    }
}
