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

package io.microsphere.spring.web.rule;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.web.rule.ProduceMediaTypeExpression.parseExpressions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

/**
 * {@link ProduceMediaTypeExpression} Test
 */
class ProduceMediaTypeExpressionTest {

    // Test basic match without negation
    @Test
    void testMatchPositive() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression(TEXT_PLAIN_VALUE);
        List<MediaType> acceptedTypes = ofList(TEXT_PLAIN);
        assertTrue(expr.match(acceptedTypes));
    }

    // Test negation case
    @Test
    void testNegatedExpression() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression(TEXT_PLAIN, true);
        List<MediaType> acceptedTypes = ofList(TEXT_PLAIN);
        assertFalse(expr.match(acceptedTypes));
    }

    // Test parameter matching
    @Test
    void testMatchWithParameters() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;charset=utf-8");
        MediaType acceptedType = parseMediaType("text/plain;charset=UTF-8");
        assertTrue(expr.match(ofList(acceptedType)));
    }

    // Test parameter mismatch
    @Test
    void testParameterMismatch() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;version=1");
        MediaType acceptedType = parseMediaType("text/plain;version=2");
        assertFalse(expr.match(ofList(acceptedType)));
    }

    // Test empty input handling
    @Test
    void testParseEmptyExpressions() {
        List<ProduceMediaTypeExpression> result = parseExpressions(null, null);
        assertTrue(result.isEmpty());
    }

    // Test header parsing with negation
    @Test
    void testParseHeaderWithNegation() {
        String[] headers = {"Accept!=application/json"};
        List<ProduceMediaTypeExpression> result = parseExpressions(null, headers);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isNegated());
        assertEquals(APPLICATION_JSON, result.get(0).getMediaType());
    }

    // Test combined produces and headers
    @Test
    void testParseCombinedSources() {
        String[] produces = {TEXT_XML_VALUE};
        String[] headers = {"Accept=application/json"};

        List<ProduceMediaTypeExpression> result = parseExpressions(produces, headers);
        assertEquals(2, result.size());

        List<MediaType> types = new ArrayList<>();
        for (ProduceMediaTypeExpression expr : result) {
            types.add(expr.getMediaType());
        }
        assertTrue(types.contains(APPLICATION_JSON));
        assertTrue(types.contains(TEXT_XML));
    }

    @Test
    void testMatchParameters() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;charset=utf-8");
        MediaType acceptedMediaType = parseMediaType("text/plain;charset=UTF-8");
        assertTrue(expr.matchParameters(acceptedMediaType));

        expr = new ProduceMediaTypeExpression("text/plain;charset=GBK");
        assertFalse(expr.matchParameters(acceptedMediaType));

        acceptedMediaType = parseMediaType("text/plain;charset=UTF-16");
        assertFalse(expr.matchParameters(acceptedMediaType));
    }
}