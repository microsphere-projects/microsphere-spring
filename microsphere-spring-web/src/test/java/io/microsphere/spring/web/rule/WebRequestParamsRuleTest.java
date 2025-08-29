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

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithParams;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebRequestParamsRule} Test
 */
public class WebRequestParamsRuleTest extends BaseWebRequestRuleTest {


    // ==================== Constructor ====================
    @Test
    public void testConstructorWithParams() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        assertEquals(2, rule.getContent().size());
    }

    @Test
    public void testConstructorWithEmptyParams() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        assertTrue(rule.getContent().isEmpty());
    }

    // ==================== getContent() ====================
    @Test
    public void testGetContent() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1", "param2=value2");
        assertEquals("Should return correct number of expressions", 2, rule.getContent().size());
    }

    // ==================== getToStringInfix() ====================
    @Test
    public void testGetToStringInfix() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        assertEquals(" && ", rule.getToStringInfix());
    }

    // ==================== matches() ====================
    @Test
    public void testMatchesAllConditions() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        NativeWebRequest request = createWebRequestWithParams("param1", "value1", "param2", new String[]{"value2"});
        assertTrue(rule.matches(request));
    }

    @Test
    public void testMatchesWithOneMismatch() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        NativeWebRequest request = createWebRequestWithParams("param1", "wrongValue");
        assertFalse(rule.matches(request));
    }

    @Test
    public void testMatchesWithEmptyExpressions() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        NativeWebRequest request = createWebRequestWithParams("anyParam", "anyValue");
        assertTrue(rule.matches(request));
    }

    @Test
    public void testMatchesWithMissingParameter() {
        WebRequestParamsRule rule = new WebRequestParamsRule("requiredParam=value");
        NativeWebRequest request = createWebRequestWithParams("otherParam", "value");
        assertFalse(rule.matches(request));
    }

    @Test
    public void testMatchesWithNegatedExpression() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1!=value1");
        NativeWebRequest request = createWebRequestWithParams("param1", "value1");
        assertFalse(rule.matches(request));
    }

    @Test
    public void testMatchesWithComplexExpressions() {
        WebRequestParamsRule rule = new WebRequestParamsRule(
                "param1=value1",
                "param2",
                "!param3"
        );

        NativeWebRequest request = createWebRequestWithParams("param1", "value1", "param2", new String[]{"anyValue"});
        assertTrue(rule.matches(request));
    }
}
