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

package io.microsphere.spring.test.web;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicBoolean;

import static io.microsphere.spring.test.web.WebTestUtils.ATTRIBUTE_NAME;
import static io.microsphere.spring.test.web.WebTestUtils.ATTRIBUTE_VALUE;
import static io.microsphere.spring.test.web.WebTestUtils.AUTH_NAME;
import static io.microsphere.spring.test.web.WebTestUtils.AUTH_VALUE;
import static io.microsphere.spring.test.web.WebTestUtils.GET_PERSON_PATH;
import static io.microsphere.spring.test.web.WebTestUtils.HEADER_NAME;
import static io.microsphere.spring.test.web.WebTestUtils.HEADER_NAME_2;
import static io.microsphere.spring.test.web.WebTestUtils.HEADER_VALUE;
import static io.microsphere.spring.test.web.WebTestUtils.HEADER_VALUE_2;
import static io.microsphere.spring.test.web.WebTestUtils.NOT_FOUND_ATTRIBUTE_NAME;
import static io.microsphere.spring.test.web.WebTestUtils.PARAM_NAME;
import static io.microsphere.spring.test.web.WebTestUtils.PARAM_NAME_2;
import static io.microsphere.spring.test.web.WebTestUtils.PARAM_VALUE;
import static io.microsphere.spring.test.web.WebTestUtils.PARAM_VALUE_2;
import static io.microsphere.spring.test.web.WebTestUtils.PERSON_ID_PATH;
import static io.microsphere.spring.test.web.WebTestUtils.PERSON_PATH;
import static io.microsphere.spring.test.web.WebTestUtils.PERSON_TEST_PATH;
import static io.microsphere.spring.test.web.WebTestUtils.REMOTE_ADDRESS;
import static io.microsphere.spring.test.web.WebTestUtils.TEST_ROOT_PATH;
import static io.microsphere.spring.test.web.WebTestUtils.getValue;
import static io.microsphere.spring.test.web.WebTestUtils.mockServerWebExchange;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static reactor.core.publisher.Mono.just;
import static reactor.core.scheduler.Schedulers.newSingle;

/**
 * {@link WebTestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebTestUtils
 * @since 1.0.0
 */
class WebTestUtilsTest {

    @Test
    void testConstants() {
        assertEquals("/test", TEST_ROOT_PATH);
        assertEquals("test-name", ATTRIBUTE_NAME);
        assertEquals("not-found-name", NOT_FOUND_ATTRIBUTE_NAME);
        assertEquals("test-value", ATTRIBUTE_VALUE);
        assertEquals("test-header-name", HEADER_NAME);
        assertEquals("test-header-value", HEADER_VALUE);
        assertEquals("test-header-name-2", HEADER_NAME_2);
        assertArrayEquals(ofArray("test-header-value-2", "test-header-value-3"), HEADER_VALUE_2);
        assertEquals("test-param-name", PARAM_NAME);
        assertEquals("test-param-value", PARAM_VALUE);
        assertEquals("test-param-name-2", PARAM_NAME_2);
        assertArrayEquals(ofArray("test-param-value-2", "test-param-value-3"), PARAM_VALUE_2);
        assertEquals("/person", PERSON_PATH);
        assertEquals("/test/person", PERSON_TEST_PATH);
        assertEquals("/{id}", PERSON_ID_PATH);
        assertEquals("_auth", AUTH_NAME);
        assertEquals("123456789", AUTH_VALUE);
        assertEquals("/test/person/{id}", GET_PERSON_PATH);
        assertNotNull(REMOTE_ADDRESS);
    }

    @Test
    void testMockServerWebExchange() {
        MockServerWebExchange mockServerWebExchange = mockServerWebExchange();
        assertNotNull(mockServerWebExchange);
    }

    @Test
    void testGetValueOnBlockingThread() {
        String value = "Hello,World";
        Mono<String> mono = just("Hello,World");
        assertEquals(value, getValue(mono));
    }

    @Test
    void testGetValueOnNonBlockingThread() throws InterruptedException {
        String value = "Hello,World";
        Mono<String> mono = just("Hello,World");

        AtomicBoolean executed = new AtomicBoolean(false);

        newSingle("test").schedule(() -> {
            assertEquals(value, getValue(mono));
            executed.set(true);
        });

        while (!executed.get()) {
            Thread.sleep(100L);
        }
    }
}