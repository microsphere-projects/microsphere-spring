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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link AbstractNameValueExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractNameValueExpression
 * @since 1.0.0
 */

public class AbstractNameValueExpressionTest {

    // Concrete implementation for testing
    static class TestExpression extends AbstractNameValueExpression<String> {
        private final boolean matchNameResult;
        private final boolean matchValueResult;

        public TestExpression(String expression, boolean matchNameResult, boolean matchValueResult) {
            super(expression);
            this.matchNameResult = matchNameResult;
            this.matchValueResult = matchValueResult;
        }

        public TestExpression(String expression, boolean caseSensitiveName,
                              boolean matchNameResult, boolean matchValueResult) {
            super(expression, caseSensitiveName);
            this.matchNameResult = matchNameResult;
            this.matchValueResult = matchValueResult;
        }

        @Override
        protected String parseValue(String valueExpression) {
            return valueExpression;
        }

        @Override
        protected boolean matchName(NativeWebRequest request) {
            return matchNameResult;
        }

        @Override
        protected boolean matchValue(NativeWebRequest request) {
            return matchValueResult;
        }
    }

    // Test expression parsing without value
    @Test
    public void testExpressionParsingWithoutValue() {
        TestExpression expr = new TestExpression("!name", false, false);
        assertEquals("name", expr.getName());
        assertNull(expr.getValue());
        assertTrue(expr.isNegated());
    }

    // Test expression parsing with value
    @Test
    public void testExpressionParsingWithValue() {
        TestExpression expr = new TestExpression("key=value", true, false, false);
        assertEquals("key", expr.getName());
        assertEquals("value", expr.getValue());
        assertFalse(expr.isNegated());
    }

    // Test negated expression with value
    @Test
    public void testNegatedExpressionWithValue() {
        TestExpression expr = new TestExpression("header!=value", false, false);
        assertEquals("header", expr.getName());
        assertEquals("value", expr.getValue());
        assertTrue(expr.isNegated());
    }

    // Test match when value is present and matches
    @Test
    public void testMatchWithValuePresentAndMatches() {
        TestExpression expr = new TestExpression("param=test", false, false, true);
        assertTrue(expr.match(null)); // request not used in test implementation
    }

    // Test negated match when value is present but doesn't match
    @Test
    public void testNegatedMatchWhenValueDoesNotMatch() {
        TestExpression expr = new TestExpression("!param=test", false, false, false);
        assertFalse(expr.match(null));
    }

    // Test match when no value is present and name matches
    @Test
    public void testMatchWithNoValueAndNameMatches() {
        TestExpression expr = new TestExpression("name", true, true, false);
        assertTrue(expr.match(null));
    }

    // Test equals with case-sensitive names
    @Test
    public void testEqualsWithCaseSensitiveNames() {
        TestExpression expr1 = new TestExpression("Name=value", true, false, false);
        TestExpression expr2 = new TestExpression("name=value", true, false, false);
        assertTrue(expr1.equals(expr1));
        assertFalse(expr1.equals(null));
        assertFalse(expr1.equals(new Object()));
        assertFalse(expr1.equals(expr2));
    }

    // Test equals with case-insensitive names
    @Test
    public void testEqualsWithCaseInsensitiveNames() {
        TestExpression expr1 = new TestExpression("Name=value", false, false, false);
        TestExpression expr2 = new TestExpression("name=value", false, false, false);
        assertTrue(expr1.equals(expr2));
    }

    // Test equals with different values
    @Test
    public void testEqualsWithDifferentValues() {
        TestExpression expr1 = new TestExpression("key=value1", false, false, false);
        TestExpression expr2 = new TestExpression("key=value2", false, false, false);
        assertFalse(expr1.equals(expr2));
    }

    // Test equals with different negation
    @Test
    public void testEqualsWithDifferentNegation() {
        TestExpression expr1 = new TestExpression("key=value", false, false, false);
        TestExpression expr2 = new TestExpression("!key=value", false, false, false);
        assertFalse(expr1.equals(expr2));
    }

    // Test hashCode consistency
    @Test
    public void testHashCodeConsistency() {
        TestExpression expr1 = new TestExpression("test=123", false, false, false);
        TestExpression expr2 = new TestExpression("test=123", false, false, false);
        assertEquals(expr1.hashCode(), expr2.hashCode());
    }

    // Test toString with value and negation
    @Test
    public void testToStringWithValueAndNegation() {
        TestExpression expr = new TestExpression("!param=value", false, false, false);
        assertEquals("!param=value", expr.toString());

        expr = new TestExpression("param!=value", true, false, false);
        assertEquals("param!=value", expr.toString());

        expr = new TestExpression("!param", true, false, false);
        assertEquals("!param", expr.toString());
    }

    // Test toString without value
    @Test
    public void testToStringWithoutValue() {
        TestExpression expr = new TestExpression("header", true, false, false);
        assertEquals("header", expr.toString());
    }

    @Test
    public void testGetExpression() {
        TestExpression expr = new TestExpression("header", false, false);
        assertEquals("header", expr.getExpression());
    }
}
