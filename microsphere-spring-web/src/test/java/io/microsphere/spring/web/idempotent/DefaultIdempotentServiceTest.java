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
import static io.microsphere.spring.web.idempotent.IdempotentAttributes.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

    private IdempotentAttributes attributes;

    private IdempotentService idempotentService;

    @BeforeEach
    void setUp() {
        this.request = createWebRequest();
        this.idempotent = this.getClass().getAnnotation(Idempotent.class);
        this.attributes = of(idempotent);
        this.idempotentService = new DefaultIdempotentService();
    }

    @Test
    void testGenerateToken() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertSame(newToken, idempotentService.generateToken(this.request, this.attributes));
    }

    @Test
    void testGetToken() {
        String token = this.idempotentService.getToken(this.request, this.attributes);
        assertNull(token);

        String newToken = this.idempotentService.generateToken(this.request, this.attributes);

        this.request = createWebRequest(r -> {
            r.addHeader(idempotent.tokenName(), newToken);
        });

        token = this.idempotentService.getToken(this.request, this.attributes);
        assertSame(newToken, token);
    }

    @Test
    void testStoreToken() {
        assertFalse(this.idempotentService.storeToken(this.request, this.attributes, null));

        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertTrue(this.idempotentService.storeToken(this.request, this.attributes, newToken));
    }

    @Test
    void testCheckToken() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertFalse(this.idempotentService.checkToken(this.request, this.attributes, newToken));

        assertTrue(this.idempotentService.storeToken(this.request, this.attributes, newToken));
        assertTrue(this.idempotentService.checkToken(this.request, this.attributes, newToken));
    }

    @Test
    void testInvalidate() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertFalse(this.idempotentService.invalidate(this.request, this.attributes, newToken));

        assertTrue(this.idempotentService.storeToken(this.request, this.attributes, newToken));
        assertTrue(this.idempotentService.invalidate(this.request, this.attributes, newToken));
    }

    @Test
    void testValidateToken() {
        // validate token in idempotent method
        this.idempotentService.validateToken(this.request, this.attributes);
        this.idempotentService.validateToken(this.request, this.attributes);

        String newToken = this.idempotentService.generateToken(this.request, this.attributes);

        MockHttpServletRequest httpServletRequest = this.request.getNativeRequest(MockHttpServletRequest.class);
        httpServletRequest.setMethod("POST");
        httpServletRequest.addHeader(idempotent.tokenName(), newToken);

        // store new token in the first request
        this.idempotentService.validateToken(this.request, this.attributes);

        // throw a IdempotentException if the token is absent in the request
        assertThrows(IdempotentException.class, () -> this.idempotentService.validateToken(this.request, this.attributes));
    }
}