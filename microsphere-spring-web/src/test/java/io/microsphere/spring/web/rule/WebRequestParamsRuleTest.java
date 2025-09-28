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

import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequestWithParams;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebRequestParamsRule} Test
 */
class WebRequestParamsRuleTest extends BaseWebRequestRuleTest {


    // ==================== Constructor ====================
    @Test
    void testConstructorWithParams() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        assertEquals(2, rule.getContent().size());
    }

    @Test
    void testConstructorWithEmptyParams() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        assertTrue(rule.getContent().isEmpty());
    }

    // ==================== getContent() ====================
    @Test
    void testGetContent() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1", "param2=value2");
        assertEquals(2, rule.getContent().size());
    }

    // ==================== getToStringInfix() ====================
    @Test
    void doTestGetToStringInfix() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        assertEquals(" && ", rule.getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");

        assertEquals(rule, rule);
        assertEquals(rule, new WebRequestParamsRule("param1=value1", "param2=value2"));

        assertNotEquals(rule, new WebRequestParamsRule("param1=value1"));
        assertNotEquals(rule, this);
        assertNotEquals(rule, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        assertEquals(rule.hashCode(), rule.getContent().hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        assertEquals("[param1=value1 && param2=value2]", rule.toString());
    }

    // ==================== matches() ====================
    @Test
    void testMatchesAllConditions() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        NativeWebRequest request = createWebRequestWithParams("param1", "value1", "param2", new String[]{"value2"});
        assertTrue(rule.matches(request));
    }

    @Test
    void testMatchesWithOneMismatch() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1=value1", "param2=value2");
        NativeWebRequest request = createWebRequestWithParams("param1", "wrongValue");
        assertFalse(rule.matches(request));
    }

    @Test
    void testMatchesWithEmptyExpressions() {
        WebRequestParamsRule rule = new WebRequestParamsRule();
        NativeWebRequest request = createWebRequestWithParams("anyParam", "anyValue");
        assertTrue(rule.matches(request));
    }

    @Test
    void testMatchesWithMissingParameter() {
        WebRequestParamsRule rule = new WebRequestParamsRule("requiredParam=value");
        NativeWebRequest request = createWebRequestWithParams("otherParam", "value");
        assertFalse(rule.matches(request));
    }

    @Test
    void testMatchesWithNegatedExpression() {
        WebRequestParamsRule rule = new WebRequestParamsRule("param1!=value1");
        NativeWebRequest request = createWebRequestWithParams("param1", "value1");
        assertFalse(rule.matches(request));
    }

    @Test
    void testMatchesWithComplexExpressions() {
        WebRequestParamsRule rule = new WebRequestParamsRule(
                "param1=value1",
                "param2",
                "!param3"
        );

        NativeWebRequest request = createWebRequestWithParams("param1", "value1", "param2", new String[]{"anyValue"});
        assertTrue(rule.matches(request));
    }
}
