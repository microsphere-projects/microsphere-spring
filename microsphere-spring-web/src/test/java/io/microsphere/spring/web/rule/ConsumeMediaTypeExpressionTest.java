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

import org.junit.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static io.microsphere.spring.web.rule.ConsumeMediaTypeExpression.parseExpressions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_XML;
import static org.springframework.http.MediaType.valueOf;

/**
 * {@link ConsumeMediaTypeExpression} Test
 *
 * @since 1.0.0
 */
public class ConsumeMediaTypeExpressionTest {

    @Test
    public void testMatchWhenPositiveMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(contentType, false);
        assertTrue("Should match the same media type", expression.match(contentType));
    }

    @Test
    public void testMatchWhenNegativeMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(APPLICATION_XML, false);
        assertFalse("Should not match different media types", expression.match(contentType));
    }

    @Test
    public void testMatchWithWildcard() {
        MediaType contentType = valueOf("application/json");
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(valueOf("application/*"), false);
        assertTrue("Wildcard should match any subtype", expression.match(contentType));
    }

    @Test
    public void testMatchWhenNegatedMatch() {
        MediaType contentType = APPLICATION_JSON;
        ConsumeMediaTypeExpression expression = new ConsumeMediaTypeExpression(contentType, true);
        assertFalse("Negated expression should return false when media types match", expression.match(contentType));
    }

    @Test
    public void testParseExpressionsWithHeaders() {
        String[] headers = {"Content-Type=application/json"};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(null, headers);
        assertFalse("Should parse Content-Type from headers", expressions.isEmpty());
        assertTrue("Parsed expression should match application/json", expressions.get(0).match(APPLICATION_JSON));
    }

    @Test
    public void testParseExpressionsWithConsumes() {
        String[] consumes = {"application/xml"};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, null);
        assertFalse("Should parse media types from consumes array", expressions.isEmpty());
        assertTrue("Parsed expression should match application/xml", expressions.get(0).match(APPLICATION_XML));
    }

    @Test
    public void testParseExpressionsWithHeadersAndConsumes() {
        String[] headers = {"Content-Type=application/json"};
        String[] consumes = {"application/xml"};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertEquals("Should parse both headers and consumes", 2, expressions.size());
        assertTrue("First expression should match application/json", expressions.get(0).match(APPLICATION_JSON));
        assertTrue("Second expression should match application/xml", expressions.get(1).match(APPLICATION_XML));
    }

    @Test
    public void testParseExpressionsWithNoValueHeaders() {
        String[] headers = {"Content-Type"};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertTrue("No expression should be parsed", expressions.isEmpty());
    }

    @Test
    public void testParseExpressionsWithMultipleHeaders() {
        String[] headers = {"Content-Type=application/json", "Content-Type=application/xml"};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertEquals("Should parse both headers and consumes", 2, expressions.size());
        assertTrue("First expression should match application/json", expressions.get(0).match(APPLICATION_JSON));
        assertTrue("Second expression should match application/xml", expressions.get(1).match(APPLICATION_XML));
    }

    @Test
    public void testParseExpressionsWhenNoContentTypeHeader() {
        String[] headers = {"Content=application/json"};
        String[] consumes = {};
        List<ConsumeMediaTypeExpression> expressions = parseExpressions(consumes, headers);
        assertTrue("No expression should be parsed", expressions.isEmpty());
    }
}
