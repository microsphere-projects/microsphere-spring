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
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * {@link WebRequestConsumesRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestConsumesRule
 * @since 1.0.0
 */
class WebRequestConsumesRuleTest extends BaseWebRequestRuleTest {

    // Test when request is preflight (should return false)
    @Test
    void testMatchesOnPreflightRequest() {
        NativeWebRequest request = createPreFightRequest();
        WebRequestConsumesRule rule = new WebRequestConsumesRule(APPLICATION_JSON_VALUE);
        assertFalse(rule.matches(request));
    }

    // Test with empty expressions (should return false)
    @Test
    void testMatchesOnEmptyExpressions() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType(APPLICATION_JSON_VALUE);
        });
        WebRequestConsumesRule rule = new WebRequestConsumesRule();
        assertFalse(rule.matches(request));
    }

    // Test with invalid content type (should return false)
    @Test
    void testMatchesOnInvalidContentType() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("invalid/type");
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule(APPLICATION_JSON_VALUE);
        assertFalse(rule.matches(request));
    }

    // Test when no content type header (should use default)
    @Test
    void testMatchesOnNoContentTypeHeader() {
        NativeWebRequest request = createWebRequest();
        WebRequestConsumesRule rule = new WebRequestConsumesRule(APPLICATION_JSON_VALUE);
        assertTrue(rule.matches(request));
    }

    // Test when all expressions match (should return true)
    @Test
    void testMatchesOnAllExpressionsMatch() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType(APPLICATION_JSON_VALUE);
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("*/*");
        assertTrue(rule.matches(request));
    }

    // Test when any expression fails (should return false)
    @Test
    void testMatchesOnAnyExpressionFails() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType(APPLICATION_XML_VALUE);
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule(APPLICATION_JSON_VALUE, TEXT_PLAIN_VALUE);
        assertFalse(rule.matches(request));
    }

    // Test with wildcard media type matching
    @Test
    void testMatchesOnWildcardMediaType() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType(APPLICATION_JSON_VALUE);
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/*");
        assertTrue(rule.matches(request));
    }

    // Test with multiple expressions where one is negated
    @Test
    void testMatchesOnWithNegatedExpression() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType(APPLICATION_XML_VALUE);
        });

        // Expression: !application/json AND application/xml
        WebRequestConsumesRule rule = new WebRequestConsumesRule(
                ofArray(APPLICATION_XML_VALUE),
                "Content-Type=!application/json");
        assertTrue(rule.matches(request));
    }

    @Override
    void doTestGetToStringInfix() {
        assertEquals("Should return ' || '", " || ", new WebRequestConsumesRule().getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestConsumesRule rule = new WebRequestConsumesRule(APPLICATION_JSON_VALUE);
    }

    @Override
    protected void doTestHashCode() {

    }

    @Override
    protected void doTestToString() {

    }
}