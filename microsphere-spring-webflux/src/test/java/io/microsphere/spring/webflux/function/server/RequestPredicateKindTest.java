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
import org.springframework.web.reactive.function.server.RequestPredicate;

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
import static io.microsphere.spring.webflux.function.server.RequestPredicateKind.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_PDF;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
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

    @Test
    void testValueOfOnMethod() {
        assertSame(METHOD, valueOf(method(GET)));
    }

    @Test
    void testValueOfOnPath() {
        assertSame(PATH, valueOf(path("/test")));
    }

    @Test
    void testValueOfOnPathExtension() {
        assertSame(PATH_EXTENSION, valueOf(pathExtension("/test")));
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
        assertSame(AND, valueOf(method(GET).and(path("/test"))));
    }

    @Test
    void testValueOfOnOr() {
        assertSame(OR, valueOf(method(GET).or(path("/test"))));
    }

    @Test
    void testValueOfOnNegate() {
        assertSame(NEGATE, valueOf(method(GET).negate()));
    }

    @Test
    void testValueOfOnUnknown() {
        assertSame(UNKNOWN, valueOf(request -> false));
    }

    @Test
    void testAcceptVisitor() {
        RequestPredicate predicate = GET("/test")
                .and(method(PUT))
                .and(method(POST))
                .and(accept(APPLICATION_JSON))
                .and(contentType(APPLICATION_JSON))
                .or(queryParam("name", "test"));

        RequestPredicateVisitorAdapter visitor = new RequestPredicateVisitorAdapter() {
        };
        acceptVisitor(predicate, visitor);
    }

    @Test
    void testParseRequestPredicate() {
        String expression = "(!((/root && GET) && Accept: application/json) || (?name test && Content-Type: application/json))";
        RequestPredicate predicate = parseRequestPredicate(expression);
        assertEquals(expression, predicate.toString());
    }

    @Test
    void testParseRequestPredicateBySource() {
        RequestPredicate predicate = path("/test");
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
        String expression = source.toString();
        RequestPredicate predicate = parseRequestPredicate(expression);
        assertEquals(expression, predicate.toString());
    }
}