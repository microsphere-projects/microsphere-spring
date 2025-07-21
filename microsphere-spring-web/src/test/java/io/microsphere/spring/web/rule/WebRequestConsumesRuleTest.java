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
import org.springframework.web.context.request.NativeWebRequest;

import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebRequestConsumesRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestConsumesRule
 * @since 1.0.0
 */
public class WebRequestConsumesRuleTest extends BaseWebRequestRuleTest {

    // Test when request is preflight (should return false)
    @Test
    public void testMatchesOnPreflightRequest() {
        NativeWebRequest request = createPreFightRequest();
        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/json");
        assertFalse("Preflight request should return false", rule.matches(request));
    }

    // Test with empty expressions (should return false)
    @Test
    public void testMatchesOnEmptyExpressions() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("application/json");
        });
        WebRequestConsumesRule rule = new WebRequestConsumesRule();
        assertFalse("Empty expressions should return false", rule.matches(request));
    }

    // Test with invalid content type (should return false)
    @Test
    public void testMatchesOnInvalidContentType() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("invalid/type");
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/json");
        assertFalse("Invalid content type should return false", rule.matches(request));
    }

    // Test when no content type header (should use default)
    @Test
    public void testMatchesOnNoContentTypeHeader() {
        NativeWebRequest request = createWebRequest();
        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/octet-stream");
        assertTrue("Should match default octet-stream type", rule.matches(request));
    }

    // Test when all expressions match (should return true)
    @Test
    public void testMatchesOnAllExpressionsMatch() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("application/json");
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("*/*");
        assertTrue("All expressions should match", rule.matches(request));
    }

    // Test when any expression fails (should return false)
    @Test
    public void testMatchesOnAnyExpressionFails() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("application/xml");
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/json", "text/plain");
        assertFalse("Should fail when any expression doesn't match", rule.matches(request));
    }

    // Test with wildcard media type matching
    @Test
    public void testMatchesOnWildcardMediaType() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("application/json");
        });

        WebRequestConsumesRule rule = new WebRequestConsumesRule("application/*");
        assertTrue("Wildcard should match any application subtype", rule.matches(request));
    }

    // Test with multiple expressions where one is negated
    @Test
    public void testMatchesOnWithNegatedExpression() {
        NativeWebRequest request = createWebRequest(req -> {
            req.setContentType("application/xml");
        });

        // Expression: !application/json AND application/xml
        WebRequestConsumesRule rule = new WebRequestConsumesRule(
                ofArray("application/xml"),
                "Content-Type=!application/json");
        assertTrue("Should match when negated expression passes", rule.matches(request));
    }

    @Override
    public void testGetToStringInfix() {
        assertEquals("Should return ' || '", " || ", new WebRequestConsumesRule().getToStringInfix());
    }
}