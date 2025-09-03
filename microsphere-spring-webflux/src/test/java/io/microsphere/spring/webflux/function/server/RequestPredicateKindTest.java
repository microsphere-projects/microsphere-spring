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


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.server.RequestPredicate;

import static io.microsphere.constants.SymbolConstants.DOT;
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
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.parseRequestPredicate;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.toExpression;
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.valueOf;
import static io.microsphere.spring.webflux.test.WebTestUtils.TEST_ROOT_PATH;
import static java.net.URI.create;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.values;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.mock.web.reactive.function.server.MockServerRequest.builder;
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
    }

    @Test
    void testAcceptOnPath() {
    }

    @Test
    void testAcceptOnPathExtension() {
    }

    @Test
    void testAcceptOnHeaders() {
    }

    @Test
    void testAcceptOnQueryParam() {
    }

    @Test
    void testAcceptOnAccept() {
    }

    @Test
    void testAcceptOnContentType() {
    }

    @Test
    void testAcceptOnAnd() {
    }

    @Test
    void testAcceptOnOr() {
    }

    @Test
    void testAcceptOnNegate() {
    }

    @Test
    void testAcceptOnUnknown() {
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
        assertFalse(HEADERS.matches("Accept"));
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
        assertTrue(OR.matches("(GET || /test)"));
        assertFalse(OR.matches("UNKNOWN"));
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
            assertTrue(predicate.test(builder().method(httpMethod).build()));
        }
    }

    @Test
    void testPredicateOnMethodOnInvalidMethod() {
        assertThrows(IllegalArgumentException.class, () -> METHOD.predicate("A"));
    }

    @Test
    void testPredicateOnPath() {
        RequestPredicate predicate = PATH.predicate(TEST_ROOT_PATH);
        assertTrue(predicate.test(builder().uri(create(TEST_ROOT_PATH)).build()));
    }

    @Test
    void testPredicateOnPathExtension() {
        RequestPredicate predicate = PATH_EXTENSION.predicate(TEST_EXTENSION_PATH);
        assertTrue(predicate.test(builder().uri(create(TEST_EXTENSION_PATH)).build()));
    }

    @Test
    void testPredicateOnHeaders() {
        RequestPredicate predicate = HEADERS.predicate("");
        assertFalse(predicate.test(builder().method(POST).build()));
    }

    @Test
    void testPredicateOnQueryParam() {
        RequestPredicate predicate = QUERY_PARAM.predicate("?name == value");
        assertTrue(predicate.test(builder().queryParam("name", "value").build()));
    }

    @Test
    void testPredicateOnAccept() {
        RequestPredicate predicate = ACCEPT.predicate("Accept: " + APPLICATION_JSON_VALUE);
        assertTrue(predicate.test(builder().header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE).build()));

        predicate = ACCEPT.predicate("Accept: " + "[" + APPLICATION_JSON_VALUE + "]");
        assertTrue(predicate.test(builder().header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE).build()));

        predicate = ACCEPT.predicate("Accept: " + "[" + APPLICATION_JSON_VALUE + "," + APPLICATION_PDF_VALUE + "]");
        assertTrue(predicate.test(builder().header(HttpHeaders.ACCEPT, APPLICATION_JSON_VALUE).build()));
        assertTrue(predicate.test(builder().header(HttpHeaders.ACCEPT, APPLICATION_PDF_VALUE).build()));
    }

    @Test
    void testPredicateOnContentType() {
        RequestPredicate predicate = CONTENT_TYPE.predicate("Content-Type: " + APPLICATION_JSON_VALUE);
        assertTrue(predicate.test(builder().header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE).build()));

        predicate = CONTENT_TYPE.predicate("Content-Type: " + "[" + APPLICATION_JSON_VALUE + "]");
        assertTrue(predicate.test(builder().header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE).build()));

        predicate = CONTENT_TYPE.predicate("Content-Type: " + "[" + APPLICATION_JSON_VALUE + "," + APPLICATION_PDF_VALUE + "]");
        assertTrue(predicate.test(builder().header(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON_VALUE).build()));
        assertTrue(predicate.test(builder().header(HttpHeaders.CONTENT_TYPE, APPLICATION_PDF_VALUE).build()));
    }

    @Test
    void testPredicateOnAnd() {
        RequestPredicate predicate = AND.predicate("(GET && /test)");
        assertTrue(predicate.test(builder().method(GET).uri(create(TEST_ROOT_PATH)).build()));
    }

    @Test
    void testPredicateOnOr() {
        RequestPredicate predicate = OR.predicate("(GET || /test)");
        assertTrue(predicate.test(builder().method(GET).build()));
        assertTrue(predicate.test(builder().uri(create(TEST_ROOT_PATH)).build()));
    }

    @Test
    void testPredicateOnNegate() {
        RequestPredicate predicate = NEGATE.predicate("!GET");
        assertTrue(predicate.test(builder().method(POST).build()));
        assertTrue(predicate.test(builder().method(PUT).build()));
        assertTrue(predicate.test(builder().method(DELETE).build()));
    }

    @Test
    void testPredicateOnUnknown() {
        RequestPredicate predicate = UNKNOWN.predicate("!GET");
        assertFalse(predicate.test(builder().method(POST).build()));
    }

    // Test RequestPredicateKind#expression(RequestPredicate)

    @Test
    void testExpressionOnMethod() {
    }

    @Test
    void testExpressionOnPath() {
    }

    @Test
    void testExpressionOnPathExtension() {
    }

    @Test
    void testExpressionOnHeaders() {
    }

    @Test
    void testExpressionOnQueryParam() {
    }

    @Test
    void testExpressionOnAccept() {
    }

    @Test
    void testExpressionOnContentType() {
    }

    @Test
    void testExpressionOnAnd() {
    }

    @Test
    void testExpressionOnOr() {
    }

    @Test
    void testExpressionOnNegate() {
    }

    @Test
    void testExpressionOnUnknown() {
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

    private void assertRequestPredicate(RequestPredicate source) {
        String expression = toExpression(source);
        RequestPredicate predicate = parseRequestPredicate(expression);
        assertEquals(expression, toExpression(predicate));
    }
}