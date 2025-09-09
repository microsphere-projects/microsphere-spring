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


import io.microsphere.spring.web.context.request.MockServletWebRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestHeaderExpression.parseExpressions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebRequestHeaderExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestHeaderExpression
 * @since 1.0.0
 */
class WebRequestHeaderExpressionTest extends BaseNameValueExpressionTest<WebRequestHeaderExpression> {

    WebRequestHeaderExpression nameOnlyExpression;

    WebRequestHeaderExpression expression;

    WebRequestHeaderExpression negatedExpression;

    MockServletWebRequest request;

    @BeforeEach
    void setUp() {
        List<WebRequestHeaderExpression> expressions = parseExpressions("name", "name=Mercy", "name!=Mercy");
        this.nameOnlyExpression = expressions.get(0);
        this.expression = expressions.get(1);
        this.negatedExpression = expressions.get(2);
        this.request = new MockServletWebRequest();
        MockHttpServletRequest mockHttpServletRequest = this.request.getMockHttpServletRequest();
        mockHttpServletRequest.addHeader("name", "Mercy");
    }

    @Test
    void testGetName() {
        assertEquals("name", nameOnlyExpression.getName());
        assertEquals("name", expression.getName());
        assertEquals("name", negatedExpression.getName());
    }

    @Test
    void testGetValue() {
        assertNull(nameOnlyExpression.getValue());
        assertEquals("Mercy", expression.getValue());
        assertEquals("Mercy", negatedExpression.getValue());
    }

    @Test
    void testIsNegated() {
        assertFalse(nameOnlyExpression.isNegated());
        assertFalse(expression.isNegated());
        assertTrue(negatedExpression.isNegated());
    }

    @Test
    void testMatch() {
        assertTrue(nameOnlyExpression.match(request));
        assertTrue(expression.match(request));
        assertFalse(negatedExpression.match(request));
    }

    @Test
    void testIsCaseSensitiveName() {
        assertFalse(this.nameOnlyExpression.isCaseSensitiveName());
        assertFalse(this.expression.isCaseSensitiveName());
        assertFalse(this.negatedExpression.isCaseSensitiveName());
    }

    @Test
    void testParseValue() {
        assertEquals("test", this.nameOnlyExpression.parseValue("test"));
        assertEquals("test", this.expression.parseValue("test"));
        assertEquals("test", this.negatedExpression.parseValue("test"));
    }

    @Test
    void testMatchName() {
        assertTrue(nameOnlyExpression.matchName(request));
        assertTrue(expression.matchName(request));
        assertTrue(negatedExpression.matchName(request));
    }

    @Test
    void testMatchValue() {
        assertFalse(nameOnlyExpression.matchValue(request));
        assertTrue(expression.matchValue(request));
        assertTrue(negatedExpression.matchValue(request));
    }

    @Test
    void testGetExpression() {
        assertEquals("name", this.nameOnlyExpression.getExpression());
        assertEquals("name=Mercy", this.expression.getExpression());
        assertEquals("name!=Mercy", this.negatedExpression.getExpression());
    }

    @Test
    void testEquals() {
        assertEquals(this.nameOnlyExpression, this.nameOnlyExpression);
        assertEquals(this.nameOnlyExpression, new WebRequestHeaderExpression("name"));
        assertNotEquals(this.nameOnlyExpression, this.expression);
        assertNotEquals(this.nameOnlyExpression, this);
        assertNotEquals(this.nameOnlyExpression, null);

        assertEquals(this.expression, this.expression);
        assertEquals(this.expression, new WebRequestHeaderExpression("name=Mercy"));
        assertNotEquals(this.expression, this.negatedExpression);
        assertNotEquals(this.expression, this);
        assertNotEquals(this.expression, null);


        assertEquals(this.negatedExpression, this.negatedExpression);
        assertEquals(this.negatedExpression, new WebRequestHeaderExpression("name!=Mercy"));
        assertNotEquals(this.negatedExpression, this.nameOnlyExpression);
        assertNotEquals(this.negatedExpression, this);
        assertNotEquals(this.negatedExpression, null);
    }

    @Test
    void testHashCode() {
        assertEquals(this.nameOnlyExpression.hashCode(), new WebRequestHeaderExpression("name").hashCode());
        assertEquals(this.expression.hashCode(), new WebRequestHeaderExpression("name=Mercy").hashCode());
        assertEquals(this.negatedExpression.hashCode(), new WebRequestHeaderExpression("name!=Mercy").hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.nameOnlyExpression.toString(), "name");
        assertEquals(this.expression.toString(), "name=Mercy");
        assertEquals(this.negatedExpression.toString(), "name!=Mercy");
    }
}