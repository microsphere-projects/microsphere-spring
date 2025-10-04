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

import java.util.List;

import static io.microsphere.spring.web.rule.ConsumeMediaTypeExpression.parseExpressions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.MediaType.valueOf;

/**
 * {@link ConsumeMediaTypeExpression} Test
 *
 * @since 1.0.0
 */
class ConsumeMediaTypeExpressionTest {

    @Test
    void testMatchWhenPositiveMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(contentType, false);
        assertTrue(expression.match(contentType));
    }

    @Test
    void testMatchWhenNegativeMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(APPLICATION_XML, false);
        assertFalse(expression.match(contentType));
    }

    @Test
    void testMatchWithWildcard() {
        MediaType contentType = valueOf(APPLICATION_JSON_VALUE);
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(valueOf("application/*"), false);
        assertTrue(expression.match(contentType));
    }

    @Test
    void testMatchWhenNegatedMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(contentType, true);
        assertFalse(expression.match(contentType));
    }

    @Test
    void testMatchWhenParametersNoMatch() {
        MediaType contentType = parseMediaType("application/json;charset=UTF-8");
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(contentType, false);
        assertFalse(expression.match(parseMediaType("application/json;charset=GBK")));
    }

    @Test
    void testParseExpressionsWithHeaders() {
        String[] headers = {"Content-Type=application/json"};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(null, headers);
        assertFalse(expressions.isEmpty());
        assertTrue(expressions.get(0).match(APPLICATION_JSON));
    }

    @Test
    void testParseExpressionsWithConsumes() {
        String[] consumes = {APPLICATION_XML_VALUE};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, null);
        assertFalse(expressions.isEmpty());
        assertTrue(expressions.get(0).match(APPLICATION_XML));
    }

    @Test
    void testParseExpressionsWithHeadersAndConsumes() {
        String[] headers = {"Content-Type=application/json"};
        String[] consumes = {APPLICATION_XML_VALUE};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertEquals(2, expressions.size());
        assertTrue(expressions.get(0).match(APPLICATION_JSON));
        assertTrue(expressions.get(1).match(APPLICATION_XML));
    }

    @Test
    void testParseExpressionsWithNoValueHeaders() {
        String[] headers = {CONTENT_TYPE};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertTrue(expressions.isEmpty());
    }

    @Test
    void testParseExpressionsWithMultipleHeaders() {
        String[] headers = {"Content-Type=application/json", "Content-Type=application/xml"};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertEquals(2, expressions.size());
        assertTrue(expressions.get(0).match(APPLICATION_JSON));
        assertTrue(expressions.get(1).match(APPLICATION_XML));
    }

    @Test
    void testParseExpressionsWhenNoContentTypeHeader() {
        String[] headers = {"Content=application/json"};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertTrue(expressions.isEmpty());
    }
}
