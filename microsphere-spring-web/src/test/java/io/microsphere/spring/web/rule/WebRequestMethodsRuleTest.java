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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.spring.test.util.SpringTestWebUtils.createPreFightRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * {@link WebRequestMethodsRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class WebRequestMethodsRuleTest extends BaseWebRequestRuleTest {

    @Test
    void testConstructorWithRequestMethods() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule(GET, POST);
        assertEquals(2, rule.getContent().size());
        assertTrue(rule.getContent().contains("GET"));
        assertTrue(rule.getContent().contains("POST"));
    }

    @Test
    void testConstructorWithStringMethods() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("PUT", "DELETE");
        assertEquals(2, rule.getContent().size());
        assertTrue(rule.getContent().contains("PUT"));
        assertTrue(rule.getContent().contains("DELETE"));
    }

    @Test
    void testGetContentOnEmpty() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule();
        assertTrue(rule.getContent().isEmpty());
    }

    @Test
    void doTestGetToStringInfix() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET");
        assertEquals(" || ", rule.getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET");

        assertEquals(rule, rule);
        assertEquals(rule, new WebRequestMethodsRule("GET"));

        assertNotEquals(rule, new WebRequestMethodsRule("POST"));
        assertNotEquals(rule, this);
        assertNotEquals(rule, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET");
        assertEquals(rule.hashCode(), rule.getContent().hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET");
        assertEquals("[GET]", rule.toString());
    }

    @Test
    void testMatchesOnPreflightRequest() {
        NativeWebRequest request = createPreFightRequest();
        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET", "POST");
        assertFalse(rule.matches(request));
    }

    @Test
    void testMatchesOnMatchingRequestMethod() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("POST");
        NativeWebRequest request = new ServletWebRequest(mockRequest);

        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET", "POST");
        assertTrue(rule.matches(request));
    }

    @Test
    void testMatchesOnNonMatchingRequestMethod() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.setMethod("PUT");
        NativeWebRequest request = new ServletWebRequest(mockRequest);

        WebRequestMethodsRule rule = new WebRequestMethodsRule("GET", "POST");
        assertFalse(rule.matches(request));
    }

    @Test
    void testMatchesStringMethodOnEmptyRuleWithOptions() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule();
        assertFalse(rule.matches("OPTIONS"));
    }

    @Test
    void testMatchesStringMethodOnEmptyRuleWithGet() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule();
        assertTrue(rule.matches("GET"));
    }

    @Test
    void testMatchesStringMethodOnMatchingMethod() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("DELETE", "PATCH");
        assertTrue(rule.matches("DELETE"));
    }

    @Test
    void testMatchesStringMethodOnNonMatchingMethod() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("HEAD", "TRACE");
        assertFalse(rule.matches("PUT"));
    }

    @Test
    void testMatchRequestMethodOnCaseInsensitive() {
        WebRequestMethodsRule rule = new WebRequestMethodsRule("get", "post");
        assertTrue(rule.matches("GET"));
        assertTrue(rule.matches("Post"));
    }
}
