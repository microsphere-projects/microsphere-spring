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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link DefaultIdempotentService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultIdempotentService
 * @since 1.0.0
 */
@Idempotent
class DefaultIdempotentServiceTest {

    private NativeWebRequest request;

    private Idempotent idempotent;

    private IdempotentService idempotentService;

    @BeforeEach
    void setUp() {
        this.request = createWebRequest();
        this.idempotent = this.getClass().getAnnotation(Idempotent.class);
        this.idempotentService = new DefaultIdempotentService();
    }

    @Test
    void testGenerateToken() {
        String newToken = this.idempotentService.generateToken(request, idempotent);
        assertSame(newToken, idempotentService.generateToken(request, idempotent));
    }

    @Test
    void testGetToken() {
        String token = this.idempotentService.getToken(request, idempotent);
        assertNull(token);

        String newToken = this.idempotentService.generateToken(this.request, idempotent);

        this.request = createWebRequest(r -> {
            r.addParameter(idempotent.tokenName(), newToken);
        });

        token = this.idempotentService.getToken(request, idempotent);
        assertSame(newToken, token);
    }

    @Test
    void testLoadToken() {
        String token = this.idempotentService.loadToken(request, idempotent);
        assertNull(token);

        String newToken = this.idempotentService.generateToken(this.request, idempotent);

        this.request = createWebRequest(r -> {
            r.getSession().setAttribute(idempotent.tokenName(), newToken);
        });

        token = this.idempotentService.loadToken(request, idempotent);
        assertSame(newToken, token);
    }

    @Test
    void testInvalidateToken() {
        assertFalse(this.idempotentService.invalidateToken(request, idempotent));
        testLoadToken();
        assertTrue(this.idempotentService.invalidateToken(request, idempotent));
    }

    @Test
    void testStoreToken() {
        assertTrue(this.idempotentService.storeToken(request, idempotent, null));

        String newToken = this.idempotentService.generateToken(this.request, idempotent);
        assertTrue(this.idempotentService.storeToken(request, idempotent, newToken));

        String token = this.idempotentService.loadToken(request, idempotent);
        assertSame(newToken, token);
    }

    @Test
    void testValidateToken() {
        String token = this.idempotentService.loadToken(request, idempotent);
        assertNull(token);

        // store new token in the first request
        this.idempotentService.validateToken(request, idempotent);
        token = this.idempotentService.loadToken(request, idempotent);
        assertNotNull(token);

        // throw a IdempotentException if the token is absent in the request
        assertThrows(IdempotentException.class, () -> this.idempotentService.validateToken(request, idempotent));

        // Add the token parameter to the request
        MockHttpServletRequest httpServletRequest = this.request.getNativeRequest(MockHttpServletRequest.class);
        httpServletRequest.setParameter(idempotent.tokenName(), token);

        // works fine
        this.idempotentService.validateToken(request, idempotent);

        // throw a IdempotentException if the token is invalid
        assertThrows(IdempotentException.class, () -> this.idempotentService.validateToken(request, idempotent));
    }
}