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


import io.microsphere.annotation.Nullable;
import io.microsphere.spring.web.util.WebSource;
import io.microsphere.spring.web.util.WebTarget;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.PropertyResolver;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;

import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.web.idempotent.Idempotent.DEFAULT_TOKEN_NAME;
import static io.microsphere.spring.web.idempotent.IdempotentAttributes.of;
import static io.microsphere.spring.web.util.WebSource.REQUEST_HEADER;
import static io.microsphere.spring.web.util.WebSource.REQUEST_PARAMETER;
import static io.microsphere.spring.web.util.WebTarget.RESPONSE_COOKIE;
import static io.microsphere.spring.web.util.WebTarget.RESPONSE_HEADER;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link IdempotentAttributes} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see IdempotentAttributes
 * @since 1.0.0
 */
class IdempotentAttributesTest {

    @Test
    void testDefaultAttributes() {
        IdempotentAttributes attributes = createAttributes("defaultAttributes");
        String tokenName = attributes.getTokenName();
        assertEquals(DEFAULT_TOKEN_NAME, tokenName);

        RequestMethod[] method = attributes.getValidatedMethod();
        assertArrayEquals(ofArray(POST, PATCH), method);

        WebSource source = attributes.getSource();
        assertEquals(REQUEST_HEADER, source);

        WebTarget target = attributes.getTarget();
        assertEquals(RESPONSE_HEADER, target);

        assertValidatedMethods(attributes);
    }

    @Test
    void testPlaceholderAttributesAttributes() {
        IdempotentAttributes attributes = createAttributes("placeholderAttributes");
        String tokenName = attributes.getTokenName();
        assertEquals("${idempotent.tokenName:token}", tokenName);

        MockEnvironment environment = new MockEnvironment();
        attributes = createAttributes("placeholderAttributes", environment);
        tokenName = attributes.getTokenName();
        assertEquals("token", tokenName);

        environment.setProperty("idempotent.tokenName", "token-1");
        attributes = createAttributes("placeholderAttributes", environment);
        tokenName = attributes.getTokenName();
        assertEquals("token-1", tokenName);

        RequestMethod[] method = attributes.getValidatedMethod();
        assertArrayEquals(ofArray(POST, PATCH), method);

        WebSource source = attributes.getSource();
        assertEquals(REQUEST_HEADER, source);

        WebTarget target = attributes.getTarget();
        assertEquals(RESPONSE_HEADER, target);

        assertValidatedMethods(attributes);
    }

    @Test
    void testSourceAndTargetAttributes() {
        IdempotentAttributes attributes = createAttributes("sourceAndTargetAttributes");
        String tokenName = attributes.getTokenName();
        assertEquals(DEFAULT_TOKEN_NAME, tokenName);

        RequestMethod[] method = attributes.getValidatedMethod();
        assertArrayEquals(ofArray(POST, PATCH), method);

        WebSource source = attributes.getSource();
        assertEquals(REQUEST_PARAMETER, source);

        WebTarget target = attributes.getTarget();
        assertEquals(RESPONSE_COOKIE, target);

        assertValidatedMethods(attributes);
    }

    private IdempotentAttributes createAttributes(String methodName) {
        Method method = findMethod(this.getClass(), methodName);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        return of(idempotent);
    }

    private IdempotentAttributes createAttributes(String methodName, @Nullable PropertyResolver propertyResolver) {
        Method method = findMethod(this.getClass(), methodName);
        Idempotent idempotent = method.getAnnotation(Idempotent.class);
        return of(idempotent, propertyResolver);
    }

    @Idempotent
    private void defaultAttributes() {
    }

    @Idempotent(tokenName = "${idempotent.tokenName:token}")
    private void placeholderAttributes() {
    }

    @Idempotent(source = REQUEST_PARAMETER, target = RESPONSE_COOKIE)
    private void sourceAndTargetAttributes() {
    }

    private void assertValidatedMethods(IdempotentAttributes attributes) {
        assertTrue(attributes.isValidatedMethod("POST"));
        assertTrue(attributes.isValidatedMethod("PATCH"));
        assertFalse(attributes.isValidatedMethod("PUT"));
    }
}