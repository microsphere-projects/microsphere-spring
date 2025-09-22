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


import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.clearAttributes;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.idempotent.IdempotentAttributes.of;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_SESSION;

/**
 * {@link DefaultIdempotentService} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultIdempotentService
 * @since 1.0.0
 */
@Idempotent
public class DefaultIdempotentServiceTest {

    private NativeWebRequest request;

    private Idempotent idempotent;

    private IdempotentAttributes attributes;

    private IdempotentService idempotentService;

    @Before
    public void setUp() {
        this.request = createWebRequest();
        this.idempotent = this.getClass().getAnnotation(Idempotent.class);
        this.attributes = of(idempotent);
        this.idempotentService = new DefaultIdempotentService();
    }

    @Test
    public void testGenerateToken() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertSame(newToken, idempotentService.generateToken(this.request, this.attributes));
    }

    @Test
    public void testGetToken() {
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
    public void testStoreToken() {
        assertFalse(this.idempotentService.storeToken(this.request, this.attributes, null));

        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertTrue(this.idempotentService.storeToken(this.request, this.attributes, newToken));
        assertFalse(this.idempotentService.storeToken(this.request, this.attributes, newToken));
    }

    @Test
    public void testCheckToken() {
        String newToken = "test";
        assertFalse(this.idempotentService.checkToken(this.request, this.attributes, newToken));

        newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertTrue(this.idempotentService.checkToken(this.request, this.attributes, newToken));
    }

    @Test
    public void testInvalidate() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        assertTrue(this.idempotentService.checkToken(this.request, this.attributes, newToken));

        // invalidate success
        assertTrue(this.idempotentService.invalidate(this.request, this.attributes, newToken));
        assertFalse(this.idempotentService.checkToken(this.request, this.attributes, newToken));

        // invalidate failed when token is absent
        assertFalse(this.idempotentService.invalidate(this.request, this.attributes, newToken));
        assertFalse(this.idempotentService.checkToken(this.request, this.attributes, newToken));
    }

    @Test
    public void testValidateToken() {
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


        newToken = this.idempotentService.generateToken(this.request, this.attributes);

        // clear attributes from Session
        clearAttributes(this.request, SCOPE_SESSION);
        // store new token
        assertTrue(this.idempotentService.storeToken(this.request, this.attributes, newToken));

        // throw a IdempotentException if the token is existed in the session
        assertThrows(IdempotentException.class, () -> this.idempotentService.validateToken(this.request, this.attributes));
    }

    @Test
    public void testDestroy() {
        String newToken = this.idempotentService.generateToken(this.request, this.attributes);
        this.idempotentService.destroy();
        assertFalse(this.idempotentService.checkToken(this.request, this.attributes, newToken));
    }
}