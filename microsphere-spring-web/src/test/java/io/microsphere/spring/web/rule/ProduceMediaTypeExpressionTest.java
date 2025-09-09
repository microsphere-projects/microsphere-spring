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

import java.util.ArrayList;
import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.web.rule.ProduceMediaTypeExpression.parseExpressions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.TEXT_XML;
import static org.springframework.http.MediaType.TEXT_XML_VALUE;
import static org.springframework.http.MediaType.parseMediaType;

/**
 * {@link ProduceMediaTypeExpression} Test
 */
public class ProduceMediaTypeExpressionTest {

    // Test basic match without negation
    @Test
    public void testMatchPositive() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression(TEXT_PLAIN_VALUE);
        List<MediaType> acceptedTypes = ofList(TEXT_PLAIN);
        assertTrue(expr.match(acceptedTypes));
    }

    // Test negation case
    @Test
    public void testNegatedExpression() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression(TEXT_PLAIN, true);
        List<MediaType> acceptedTypes = ofList(TEXT_PLAIN);
        assertFalse(expr.match(acceptedTypes));
    }

    // Test parameter matching
    @Test
    public void testMatchWithParameters() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;charset=utf-8");
        MediaType acceptedType = parseMediaType("text/plain;charset=UTF-8");
        assertTrue(expr.match(ofList(acceptedType)));
    }

    // Test parameter mismatch
    @Test
    public void testParameterMismatch() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;version=1");
        MediaType acceptedType = parseMediaType("text/plain;version=2");
        assertFalse(expr.match(ofList(acceptedType)));
    }

    // Test empty input handling
    @Test
    public void testParseEmptyExpressions() {
        List<ProduceMediaTypeExpression> result = parseExpressions(null, null);
        assertTrue(result.isEmpty());
    }

    // Test header parsing with negation
    @Test
    public void testParseHeaderWithNegation() {
        String[] headers = {"Accept!=application/json"};
        List<ProduceMediaTypeExpression> result = parseExpressions(null, headers);

        assertEquals(1, result.size());
        assertTrue(result.get(0).isNegated());
        assertEquals(APPLICATION_JSON, result.get(0).getMediaType());
    }

    // Test combined produces and headers
    @Test
    public void testParseCombinedSources() {
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
}