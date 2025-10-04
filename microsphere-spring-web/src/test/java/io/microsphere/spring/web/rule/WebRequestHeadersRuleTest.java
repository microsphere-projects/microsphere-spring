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

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithHeaders;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.ACCEPT;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;

/**
 * {@link WebRequestHeadersRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestHeadersRule
 * @since 1.0.0
 */
class WebRequestHeadersRuleTest extends BaseWebRequestRuleTest {

    @Test
    void testGetContent() {
        WebRequestHeadersRule rule1 = new WebRequestHeadersRule();
        assertTrue(rule1.getContent().isEmpty());

        rule1 = new WebRequestHeadersRule("Accept=text/plain", "Content-Type=application/json");
        assertTrue(rule1.getContent().isEmpty());

        WebRequestHeadersRule rule2 = new WebRequestHeadersRule("header1=value1", "header2=value2");
        assertEquals(2, rule2.getContent().size());
    }

    @Test
    void doTestGetToStringInfix() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule();
        assertEquals(" && ", rule.getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("header1=value1", "header2=value2");

        assertEquals(rule, rule);
        assertEquals(rule, new WebRequestHeadersRule("header1=value1", "header2=value2"));

        assertNotEquals(rule, new WebRequestHeadersRule("Accept=text/xml"));
        assertNotEquals(rule, this);
        assertNotEquals(rule, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("header1=value1", "header2=value2");
        assertEquals(rule.hashCode(), rule.getContent().hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("header1=value1", "header2=value2");
        assertEquals("[header1=value1 && header2=value2]", rule.toString());
    }

    @Test
    void testEmptyExpressionsShouldMatch() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule();
        NativeWebRequest request = createWebRequestWithHeaders();
        assertTrue(rule.matches(request));
    }

    @Test
    void testAllExpressionsMatch() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("header1=value1", "header2=value2");
        NativeWebRequest request = createWebRequestWithHeaders("header1", "value1", "header2", "value2");
        assertTrue(rule.matches(request));
    }

    @Test
    void testPartialMatchFailure() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("header1=value1", "header2=value2");
        NativeWebRequest request = createWebRequestWithHeaders("header1", "value1", "header2", "value");
        assertFalse(rule.matches(request));
    }

    @Test
    void testShortCircuitEvaluation() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("Invalid-Header=value", "Accept=text/plain");

        NativeWebRequest request = createWebRequestWithHeaders(ACCEPT, TEXT_PLAIN_VALUE);
        assertFalse(rule.matches(request));
    }

    @Test
    void testSpecialHeadersHandling() {
        WebRequestHeadersRule rule = new WebRequestHeadersRule("Accept=text/plain", "Content-Type=application/json");
        NativeWebRequest request = createWebRequestWithHeaders(ACCEPT, TEXT_PLAIN_VALUE, CONTENT_TYPE, APPLICATION_JSON_VALUE);
        assertTrue(rule.matches(request));
    }

}