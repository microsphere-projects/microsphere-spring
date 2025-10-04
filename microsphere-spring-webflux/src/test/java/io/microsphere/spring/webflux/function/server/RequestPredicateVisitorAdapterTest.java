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

package io.microsphere.spring.webflux.function.server;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;

import java.util.Set;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKindTest.TEST_PATH_EXTENSION;
import static io.microsphere.spring.webflux.function.server.RequestPredicateVisitorAdapter.VISITOR_CLASS;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_NAME;
import static io.microsphere.spring.webflux.test.WebTestUtils.PARAM_VALUE;
import static io.microsphere.spring.webflux.test.WebTestUtils.TEST_ROOT_PATH;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.headers;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.pathExtension;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

/**
 * {@link RequestPredicateVisitorAdapter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicateVisitorAdapter
 * @since 1.0.0
 */
class RequestPredicateVisitorAdapterTest {

    private RequestPredicateVisitorAdapter adapter;

    @BeforeEach
    void setUp() {
        this.adapter = new RequestPredicateVisitorAdapter() {
        };
    }

    @Test
    void testMethod() {
        this.adapter.method(null);
    }

    @Test
    void testPath() {
        this.adapter.path(null);
    }

    @Test
    void testPathExtension() {
        this.adapter.pathExtension(null);
    }

    @Test
    void testHeader() {
        this.adapter.header(null, null);
    }

    @Test
    void testQueryParam() {
        this.adapter.queryParam(null, null);
    }

    @Test
    void testStartAnd() {
        this.adapter.startAnd();
    }

    @Test
    void testAnd() {
        this.adapter.and();
    }

    @Test
    void testEndAnd() {
        this.adapter.endAnd();
    }

    @Test
    void testStartOr() {
        this.adapter.startOr();
    }

    @Test
    void testOr() {
        this.adapter.or();
    }

    @Test
    void testEndOr() {
        this.adapter.endOr();
    }

    @Test
    void testStartNegate() {
        this.adapter.startNegate();
    }

    @Test
    void testEndNegate() {
        this.adapter.endNegate();
    }

    @Test
    void testUnknown() {
        this.adapter.unknown(null);
    }

    @Test
    void testIsVisitorSupported() {
        assertEquals(VISITOR_CLASS != null, this.adapter.isVisitorSupported());
    }

    @Test
    void testGetProxy() {
        if (this.adapter.isVisitorSupported()) {
            Object proxy = this.adapter.getProxy();
            assertTrue(VISITOR_CLASS.isInstance(proxy));
        }
    }

    @Test
    void testInvoke() throws Throwable {
        assertNull(this.adapter.invoke(this, findMethod(RequestPredicateVisitorAdapter.class, "method", Set.class), ofArray(emptySet())));
    }

    @Test
    void testVisitOnVisitorSupported() {
        if (VISITOR_CLASS != null) {
            testVisit(true);
        }
    }

    @Test
    void testVisitOnVisitorNotSupported() {
        testVisit(false);
    }

    void testVisit(final boolean isVisitorSupported) {
        RequestPredicate methodPredicate = method(GET);
        RequestPredicate pathPredicate = path(TEST_ROOT_PATH);
        RequestPredicate pathExtensionPredicate = pathExtension(TEST_PATH_EXTENSION);
        RequestPredicate headersPredicate = headers(headers -> true);
        RequestPredicate acceptPredicate = accept(APPLICATION_JSON);
        RequestPredicate contentTypePredicate = contentType(APPLICATION_PDF);
        RequestPredicate queryParamPredicate = queryParam(PARAM_NAME, PARAM_VALUE);
        RequestPredicate andPredicate = methodPredicate.and(pathPredicate);
        RequestPredicate orPredicate = methodPredicate.or(pathPredicate);
        RequestPredicate negatePredicate = methodPredicate.negate();
        RequestPredicate unknownPredicate = all();

        ThreadLocal<RequestPredicate> threadLocal = new ThreadLocal<>();

        this.adapter = new RequestPredicateVisitorAdapter() {
            @Override
            public boolean isVisitorSupported() {
                return isVisitorSupported;
            }

            @Override
            public void method(Set<HttpMethod> methods) {
                assertEquals(ofSet(GET), methods);
            }

            @Override
            public void path(String pattern) {
                assertEquals(TEST_ROOT_PATH, pattern);
            }

            @Override
            public void pathExtension(String extension) {
                assertEquals(TEST_PATH_EXTENSION, extension);
            }

            @Override
            public void header(String name, String value) {
                if (ACCEPT.equals(name)) {
                    assertEquals(APPLICATION_JSON_VALUE, value);
                } else if (CONTENT_TYPE.equals(name)) {
                    assertEquals(APPLICATION_PDF_VALUE, value);
                }
            }

            @Override
            public void queryParam(String name, String value) {
                assertEquals(PARAM_NAME, name);
                assertEquals(PARAM_VALUE, value);
            }

            @Override
            public void startAnd() {
                assertSame(threadLocal.get(), andPredicate);
            }

            @Override
            public void and() {
                assertSame(threadLocal.get(), andPredicate);
            }

            @Override
            public void endAnd() {
                assertSame(threadLocal.get(), andPredicate);
            }

            @Override
            public void startOr() {
                assertSame(threadLocal.get(), orPredicate);
            }

            @Override
            public void or() {
                assertSame(threadLocal.get(), orPredicate);
            }

            @Override
            public void endOr() {
                assertSame(threadLocal.get(), orPredicate);
            }

            @Override
            public void startNegate() {
                assertSame(threadLocal.get(), negatePredicate);
            }

            @Override
            public void endNegate() {
                assertSame(threadLocal.get(), negatePredicate);
            }

            @Override
            public void unknown(RequestPredicate predicate) {
                assertNotNull(predicate);
            }
        };

        this.adapter.visit(methodPredicate);
        this.adapter.visit(pathPredicate);
        this.adapter.visit(pathExtensionPredicate);
        this.adapter.visit(headersPredicate);
        this.adapter.visit(acceptPredicate);
        this.adapter.visit(contentTypePredicate);
        this.adapter.visit(queryParamPredicate);

        threadLocal.set(andPredicate);
        this.adapter.visit(andPredicate);
        threadLocal.remove();

        threadLocal.set(orPredicate);
        this.adapter.visit(orPredicate);
        threadLocal.remove();

        threadLocal.set(negatePredicate);
        this.adapter.visit(negatePredicate);
        threadLocal.remove();

        this.adapter.visit(unknownPredicate);
    }

}