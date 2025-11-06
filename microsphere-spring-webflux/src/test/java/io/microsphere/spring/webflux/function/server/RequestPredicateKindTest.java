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


import io.microsphere.lang.MutableInteger;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerRequest.Headers;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.collection.ListUtils.ofList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.ACCEPT;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.AND;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.CONTENT_TYPE;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.HEADERS;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.METHOD;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.NEGATE;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.OR;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.PATH;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.PATH_EXTENSION;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.QUERY_PARAM;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.UNKNOWN;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.acceptVisitor;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.getComposeExpression;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.parseRequestPredicate;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.toExpression;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.valueOf;
import static io.microsphere.spring.webflux.test.WebTestUtils.TEST_ROOT_PATH;
import static io.microsphere.spring.webflux.test.WebTestUtils.mockServerWebExchange;
import static java.lang.ThreadLocal.withInitial;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.values;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.all;
import static org.springframework.web.reactive.function.server.RequestPredicates.contentType;
import static org.springframework.web.reactive.function.server.RequestPredicates.headers;
import static org.springframework.web.reactive.function.server.RequestPredicates.method;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.pathExtension;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;

/**
 * {@link RequestPredicateKind} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestPredicateKind
 * @since 1.0.0
 */
class RequestPredicateKindTest {

    public static final String TEST_PATH_EXTENSION = "txt";

    public static final String TEST_EXTENSION_PATH = TEST_ROOT_PATH + DOT + TEST_PATH_EXTENSION;

    // Test RequestPredicateKind#accept(RequestPredicate, RequestPredicateVisitorAdapter)

    @Test
    void testAcceptOnMethod() {
        RequestPredicate predicate = method(GET);
        METHOD.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void method(Set<HttpMethod> methods) {
                assertEquals(ofSet(GET), methods);
            }
        });
    }

    @Test
    void testAcceptOnPath() {
        RequestPredicate predicate = path(TEST_ROOT_PATH);
        PATH.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void path(String pattern) {
                assertEquals(TEST_ROOT_PATH, pattern);
            }
        });
    }

    @Test
    void testAcceptOnPathExtension() {
        RequestPredicate predicate = pathExtension(TEST_PATH_EXTENSION);
        PATH_EXTENSION.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void pathExtension(String extension) {
                assertEquals(TEST_PATH_EXTENSION, extension);
            }
        });
    }

    @Test
    void testAcceptOnHeaders() {
        RequestPredicate predicate = headers(headers -> true);
        HEADERS.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void unknown(RequestPredicate p) {
                assertSame(predicate, p);
            }
        });
    }

    @Test
    void testAcceptOnQueryParam() {
        RequestPredicate predicate = queryParam("name", "value");
        QUERY_PARAM.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void queryParam(String name, String value) {
                assertEquals("name", name);
                assertEquals("value", value);
            }
        });
    }

    @Test
    void testAcceptOnAccept() {
        RequestPredicate predicate = accept(APPLICATION_JSON);
        ACCEPT.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void header(String name, String value) {
                assertEquals(HttpHeaders.ACCEPT, name);
                assertEquals(APPLICATION_JSON_VALUE, value);
            }
        });
    }

    @Test
    void testAcceptOnContentType() {
        RequestPredicate predicate = contentType(APPLICATION_JSON);
        CONTENT_TYPE.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void header(String name, String value) {
                assertEquals(HttpHeaders.CONTENT_TYPE, name);
                assertEquals(APPLICATION_JSON_VALUE, value);
            }
        });
    }

    @Test
    void testAcceptOnAnd() {
        RequestPredicate left = method(GET);
        RequestPredicate right = path(TEST_ROOT_PATH);
        RequestPredicate predicate = left.and(right);
        ThreadLocal<RequestPredicate> threadLocal = withInitial(() -> predicate);
        AND.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void startAnd() {
                assertSame(predicate, threadLocal.get());
            }

            @Override
            public void and() {
                assertSame(predicate, threadLocal.get());
            }

            @Override
            public void endAnd() {
                assertSame(predicate, threadLocal.get());
            }
        });

        threadLocal.remove();
    }

    @Test
    void testAcceptOnOr() {
        RequestPredicate left = method(GET);
        RequestPredicate right = path(TEST_ROOT_PATH);
        RequestPredicate predicate = left.or(right);
        ThreadLocal<RequestPredicate> threadLocal = withInitial(() -> predicate);
        OR.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void startOr() {
                assertSame(predicate, threadLocal.get());
            }

            @Override
            public void or() {
                assertSame(predicate, threadLocal.get());
            }

            @Override
            public void endOr() {
                assertSame(predicate, threadLocal.get());
            }
        });

        threadLocal.remove();
    }

    @Test
    void testAcceptOnNegate() {
        RequestPredicate predicate = method(GET).negate();
        ThreadLocal<RequestPredicate> threadLocal = withInitial(() -> predicate);
        NEGATE.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void startNegate() {
                assertSame(predicate, threadLocal.get());
            }

            @Override
            public void endNegate() {
                assertSame(predicate, threadLocal.get());
            }
        });

        threadLocal.remove();
    }

    @Test
    void testAcceptOnUnknown() {
        RequestPredicate predicate = method(GET);
        UNKNOWN.accept(predicate, new RequestPredicateVisitorAdapter() {
            @Override
            public void unknown(RequestPredicate p) {
                assertSame(predicate, p);
            }
        });
    }

    // Test RequestPredicateKind#matches(RequestPredicate)

    @Test
    void testMatchesWithPredicateOnMethod() {
        assertTrue(METHOD.matches(method(GET)));
        assertFalse(METHOD.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnPath() {
        assertTrue(PATH.matches(path(TEST_ROOT_PATH)));
        assertFalse(PATH.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnPathExtension() {
        assertTrue(PATH_EXTENSION.matches(pathExtension(TEST_PATH_EXTENSION)));
        assertFalse(PATH_EXTENSION.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnHeaders() {
        assertTrue(HEADERS.matches(headers(t -> true)));
        assertFalse(HEADERS.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnQueryParam() {
        assertTrue(QUERY_PARAM.matches(queryParam("name", "value")));
        assertFalse(QUERY_PARAM.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnAccept() {
        assertTrue(ACCEPT.matches(accept(APPLICATION_JSON)));
        assertFalse(ACCEPT.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnContentType() {
        assertTrue(CONTENT_TYPE.matches(contentType(APPLICATION_JSON)));
        assertFalse(CONTENT_TYPE.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnAnd() {
        assertTrue(AND.matches(method(GET).and(path(TEST_ROOT_PATH))));
        assertFalse(AND.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnOr() {
        assertTrue(OR.matches(method(GET).or(path(TEST_ROOT_PATH))));
        assertFalse(OR.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnNegate() {
        assertTrue(NEGATE.matches(method(GET).negate()));
        assertFalse(NEGATE.matches(all()));
    }

    @Test
    void testMatchesWithPredicateOnUnknown() {
        assertFalse(UNKNOWN.matches(request -> false));
        assertFalse(UNKNOWN.matches(all()));
    }

    // Test RequestPredicateKind#matches(String)

    @Test
    void testMatchesWithExpressionOnMethod() {
        for (HttpMethod httpMethod : values()) {
            assertTrue(METHOD.matches(httpMethod.name()));
        }
        assertFalse(METHOD.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnPath() {
        assertFalse(PATH.matches(TEST_ROOT_PATH));
    }

    @Test
    void testMatchesWithExpressionOnPathExtension() {
        assertTrue(PATH_EXTENSION.matches(TEST_EXTENSION_PATH));
        assertFalse(PATH_EXTENSION.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnHeaders() {
        assertFalse(HEADERS.matches(HttpHeaders.ACCEPT));
    }

    @Test
    void testMatchesWithExpressionOnQueryParam() {
        assertTrue(QUERY_PARAM.matches("?name value"));
        assertFalse(QUERY_PARAM.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnAccept() {
        assertTrue(ACCEPT.matches("Accept: application/json"));
        assertFalse(ACCEPT.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnContentType() {
        assertTrue(CONTENT_TYPE.matches("Content-Type: application/json"));
        assertFalse(CONTENT_TYPE.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnAnd() {
        assertTrue(AND.matches("(GET && /test)"));
        assertFalse(AND.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnOr() {
        assertFalse(OR.matches("("));
        assertFalse(OR.matches("(GET || "));
        assertFalse(OR.matches("(GET || /test"));
        assertTrue(OR.matches("(GET || /test)"));
    }

    @Test
    void testMatchesWithExpressionOnNegate() {
        assertTrue(NEGATE.matches("!GET"));
        assertFalse(NEGATE.matches("UNKNOWN"));
    }

    @Test
    void testMatchesWithExpressionOnUnknown() {
        assertFalse(UNKNOWN.matches(""));
        assertFalse(UNKNOWN.matches("UNKNOWN"));
    }

    // Test RequestPredicateKind#predicate(String)

    @Test
    void testPredicateOnMethod() {
        for (HttpMethod httpMethod : values()) {
            RequestPredicate predicate = METHOD.predicate(httpMethod.name());
            ServerRequest request = mockServerRequest();
            when(request.method()).thenReturn(httpMethod);
            assertTrue(predicate.test(request));
        }
    }

    @Test
    void testPredicateOnMethodOnInvalidMethod() {
        assertThrows(IllegalArgumentException.class, () -> METHOD.predicate("A"));
    }

    @Test
    void testPredicateOnPath() {
        ServerRequest request = mockServerRequest();
//        when(request.path()).thenReturn(parsePath(TEST_ROOT_PATH));
        RequestPredicate predicate = PATH.predicate(TEST_ROOT_PATH);
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnPathExtension() {
        ServerRequest request = mockServerRequest();
        when(request.path()).thenReturn(TEST_EXTENSION_PATH);
        RequestPredicate predicate = PATH_EXTENSION.predicate(TEST_EXTENSION_PATH);
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnHeaders() {
        ServerRequest request = mockServerRequest();
        when(request.headers()).thenReturn(null);
        RequestPredicate predicate = HEADERS.predicate("");
        assertFalse(predicate.test(request));
    }

    @Test
    void testPredicateOnQueryParam() {
        ServerRequest request = mockServerRequest();
        when(request.queryParam("name")).thenReturn(of("value"));
        RequestPredicate predicate = QUERY_PARAM.predicate("?name == value");
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnAccept() {
        ServerRequest request = mockServerRequest();
        Headers headers = mock(Headers.class);

        when(request.headers()).thenReturn(headers);
        when(headers.accept()).thenReturn(ofList(APPLICATION_JSON));

        RequestPredicate predicate = ACCEPT.predicate("Accept: " + APPLICATION_JSON_VALUE);
        assertTrue(predicate.test(request));

        predicate = ACCEPT.predicate("Accept: " + "[" + APPLICATION_JSON_VALUE + "]");
        assertTrue(predicate.test(request));

        predicate = ACCEPT.predicate("Accept: " + "[" + APPLICATION_JSON_VALUE + "," + APPLICATION_PDF_VALUE + "]");
        assertTrue(predicate.test(request));

        when(headers.accept()).thenReturn(ofList(APPLICATION_PDF));
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnContentType() {
        ServerRequest request = mockServerRequest();
        Headers headers = mock(Headers.class);

        when(request.headers()).thenReturn(headers);
        when(headers.contentType()).thenReturn(of(APPLICATION_JSON));

        RequestPredicate predicate = CONTENT_TYPE.predicate("Content-Type: " + APPLICATION_JSON_VALUE);
        assertTrue(predicate.test(request));

        predicate = CONTENT_TYPE.predicate("Content-Type: " + "[" + APPLICATION_JSON_VALUE + "]");
        assertTrue(predicate.test(request));

        predicate = CONTENT_TYPE.predicate("Content-Type: " + "[" + APPLICATION_JSON_VALUE + "," + APPLICATION_PDF_VALUE + "]");
        assertTrue(predicate.test(request));

        when(headers.contentType()).thenReturn(of(APPLICATION_PDF));
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnAnd() {
        ServerRequest request = mockServerRequest();
        when(request.method()).thenReturn(GET);
        RequestPredicate predicate = AND.predicate("(GET && /test)");
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnOr() {
        ServerRequest request = mockServerRequest();
        when(request.method()).thenReturn(GET);
        RequestPredicate predicate = OR.predicate("(GET || /test)");
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnNegate() {
        ServerRequest request = mockServerRequest();
        when(request.method()).thenReturn(POST);
        RequestPredicate predicate = NEGATE.predicate("!GET");
        assertTrue(predicate.test(request));
    }

    @Test
    void testPredicateOnUnknown() {
        ServerRequest request = mockServerRequest();
        RequestPredicate predicate = UNKNOWN.predicate("!GET");
        assertFalse(predicate.test(request));
    }

    ServerRequest mockServerRequest() {
        final Method exchangeMethod = findMethod(ServerRequest.class, "exchange");

        ServerRequest request = mock(ServerRequest.class, withSettings().defaultAnswer(invocation -> {
            Method method = invocation.getMethod();
            String methodName = method.getName();
            if (Objects.equals("exchange", methodName)) {
                return mockServerWebExchange();
            } else if (Objects.equals("requestPath", methodName)) {
                return mockServerWebExchange().getRequest().getPath();
            }
            return null;
        }));

        when(request.attributes()).thenReturn(newHashMap());
        when(request.pathVariables()).thenReturn(newHashMap());
        return request;
    }

    // Test RequestPredicateKind#expression(RequestPredicate)

    @Test
    void testExpressionOnMethod() {
        RequestPredicate predicate = method(GET);
        assertEquals(GET.name(), METHOD.expression(predicate));
    }

    @Test
    void testExpressionOnPath() {
        RequestPredicate predicate = path(TEST_ROOT_PATH);
        assertEquals(TEST_ROOT_PATH, PATH.expression(predicate));
    }

    @Test
    void testExpressionOnPathExtension() {
        RequestPredicate predicate = pathExtension(TEST_PATH_EXTENSION);
        assertEquals("*." + TEST_PATH_EXTENSION, PATH_EXTENSION.expression(predicate));
    }

    @Test
    void testExpressionOnHeaders() {
        RequestPredicate predicate = headers(headers -> true);
        assertTrue(HEADERS.expression(predicate).contains("@"));
    }

    @Test
    void testExpressionOnQueryParam() {
        RequestPredicate predicate = queryParam("name", "value");
        assertEquals("?name value", QUERY_PARAM.expression(predicate));
    }

    @Test
    void testExpressionOnAccept() {
        RequestPredicate predicate = accept(APPLICATION_JSON);
        assertEquals("Accept: " + APPLICATION_JSON_VALUE, ACCEPT.expression(predicate));
    }

    @Test
    void testExpressionOnContentType() {
        RequestPredicate predicate = contentType(APPLICATION_JSON);
        assertEquals("Content-Type: " + APPLICATION_JSON_VALUE, CONTENT_TYPE.expression(predicate));
    }

    @Test
    void testExpressionOnAnd() {
        RequestPredicate predicate = method(GET).and(path(TEST_ROOT_PATH));
        assertEquals("(GET && /test)", AND.expression(predicate));
    }

    @Test
    void testExpressionOnOr() {
        RequestPredicate predicate = method(GET).or(path(TEST_ROOT_PATH));
        assertEquals("(GET || /test)", OR.expression(predicate));
    }

    @Test
    void testExpressionOnNegate() {
        RequestPredicate predicate = method(GET).negate();
        assertEquals("!GET", NEGATE.expression(predicate));
    }

    @Test
    void testExpressionOnUnknown() {
        RequestPredicate predicate = r -> true;
        assertTrue(UNKNOWN.expression(predicate).contains("Lambda"));
    }

    // Test RequestPredicateKind#valueOf(RequestPredicate)

    @Test
    void testValueOfOnMethod() {
        assertSame(METHOD, valueOf(method(GET)));
    }

    @Test
    void testValueOfOnPath() {
        assertSame(PATH, valueOf(path(TEST_ROOT_PATH)));
    }

    @Test
    void testValueOfOnPathExtension() {
        assertSame(PATH_EXTENSION, valueOf(pathExtension(TEST_PATH_EXTENSION)));
    }

    @Test
    void testValueOfOnHeaders() {
        assertSame(HEADERS, valueOf(headers(headers -> headers.accept().isEmpty())));
    }

    @Test
    void testValueOfOnQueryParam() {
        assertSame(QUERY_PARAM, valueOf(queryParam("name", "value")));
    }

    @Test
    void testValueOfOnAccept() {
        assertSame(ACCEPT, valueOf(accept(APPLICATION_JSON)));
    }

    @Test
    void testValueOfOnContentType() {
        assertSame(CONTENT_TYPE, valueOf(contentType(APPLICATION_JSON)));
    }

    @Test
    void testValueOfOnAnd() {
        assertSame(AND, valueOf(method(GET).and(path(TEST_ROOT_PATH))));
    }

    @Test
    void testValueOfOnOr() {
        assertSame(OR, valueOf(method(GET).or(path(TEST_ROOT_PATH))));
    }

    @Test
    void testValueOfOnNegate() {
        assertSame(NEGATE, valueOf(method(GET).negate()));
    }

    @Test
    void testValueOfOnUnknown() {
        assertSame(UNKNOWN, valueOf(request -> false));
    }

    // Test RequestPredicateKind#acceptVisitor(RequestPredicate, RequestPredicateVisitor)

    @Test
    void testAcceptVisitor() {
        RequestPredicate predicate = GET(TEST_ROOT_PATH)
                .and(method(PUT))
                .and(method(POST))
                .and(accept(APPLICATION_JSON))
                .and(contentType(APPLICATION_JSON))
                .or(queryParam("name", "test"));

        RequestPredicateVisitorAdapter visitor = new RequestPredicateVisitorAdapter() {
        };
        acceptVisitor(predicate, visitor);
    }

    // Test RequestPredicateKind#toExpression(RequestPredicate)

    @Test
    void testToExpression() {
        String expression = "(!((/root && GET) && Accept: application/json) || (?name test && Content-Type: application/json))";
        RequestPredicate predicate = parseRequestPredicate(expression);
        assertEquals(expression, toExpression(predicate));
    }

    // Test RequestPredicateKind#parseRequestPredicate(String)

    @Test
    void testParseRequestPredicateBySource() {
        RequestPredicate predicate = path(TEST_ROOT_PATH);
        assertRequestPredicate(predicate);

        predicate = predicate.and(method(GET));
        assertRequestPredicate(predicate);

        predicate = predicate.negate();
        assertRequestPredicate(predicate);

        predicate = predicate.and(accept(APPLICATION_JSON));
        assertRequestPredicate(predicate);

        predicate = predicate.and(contentType(APPLICATION_PDF));
        assertRequestPredicate(predicate);

        predicate = predicate.or(predicate);
        assertRequestPredicate(predicate);

        predicate = predicate.negate();
        assertRequestPredicate(predicate);

        predicate = predicate.and(method(POST));
        assertRequestPredicate(predicate);
    }

    // RequestPredicateKind#getComposeExpression(StringBuilder,MutableInteger)

    @Test
    void testGetComposeExpression() {
        StringBuilder expression = new StringBuilder();
        MutableInteger index = MutableInteger.of(0);

        assertNull(getComposeExpression(new StringBuilder(")"), index));

        assertEquals("()", getComposeExpression(new StringBuilder("()"), index));

        assertNull(getComposeExpression(expression, index));

        expression.append("(GET && /test");
        assertNull(getComposeExpression(expression, index));

        expression.append(")");
        assertNotNull(getComposeExpression(expression, index));
    }

    @Test
    void testToString() {
        assertEquals("a", RequestPredicateKind.toString(ofList("a")));
        assertEquals("[a, b]", RequestPredicateKind.toString(ofList("a", "b")));
    }

    private void assertRequestPredicate(RequestPredicate source) {
        String expression = toExpression(source);
        RequestPredicate predicate = parseRequestPredicate(expression);
        assertEquals(expression, toExpression(predicate));
    }
}