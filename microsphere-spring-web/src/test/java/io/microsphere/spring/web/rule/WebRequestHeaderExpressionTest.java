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
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestHeaderExpression.parseExpressions;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * {@link WebRequestHeaderExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestHeaderExpression
 * @since 1.0.0
 */
public class WebRequestHeaderExpressionTest extends BaseNameValueExpressionTest<WebRequestHeaderExpression> {

    WebRequestHeaderExpression nameOnlyExpression;

    WebRequestHeaderExpression negatedNameOnlyExpression;

    WebRequestHeaderExpression expression;

    WebRequestHeaderExpression negatedExpression;

    MockServletWebRequest request;

    @Before
    public void setUp() {
        List<WebRequestHeaderExpression> expressions = parseExpressions("name", "!name", "name=Mercy", "name!=Mercy");
        this.nameOnlyExpression = expressions.get(0);
        this.negatedNameOnlyExpression = expressions.get(1);
        this.expression = expressions.get(2);
        this.negatedExpression = expressions.get(3);
        this.request = new MockServletWebRequest();
        MockHttpServletRequest mockHttpServletRequest = this.request.getMockHttpServletRequest();
        mockHttpServletRequest.addHeader("name", "Mercy");
    }

    @Test
    public void testGetName() {
        assertEquals("name", nameOnlyExpression.getName());
        assertEquals("name", negatedNameOnlyExpression.getName());
        assertEquals("name", expression.getName());
        assertEquals("name", negatedExpression.getName());
    }

    @Test
    public void testGetValue() {
        assertNull(nameOnlyExpression.getValue());
        assertNull(negatedNameOnlyExpression.getValue());
        assertEquals("Mercy", expression.getValue());
        assertEquals("Mercy", negatedExpression.getValue());
    }

    @Test
    public void testIsNegated() {
        assertFalse(nameOnlyExpression.isNegated());
        assertTrue(negatedNameOnlyExpression.isNegated());
        assertFalse(expression.isNegated());
        assertTrue(negatedExpression.isNegated());
    }

    @Test
    public void testMatch() {
        assertTrue(nameOnlyExpression.match(request));
        assertFalse(negatedNameOnlyExpression.match(request));
        assertTrue(expression.match(request));
        assertFalse(negatedExpression.match(request));
    }

    @Test
    public void testIsCaseSensitiveName() {
        assertFalse(this.nameOnlyExpression.isCaseSensitiveName());
        assertFalse(this.negatedNameOnlyExpression.match(request));
        assertFalse(this.expression.isCaseSensitiveName());
        assertFalse(this.negatedExpression.isCaseSensitiveName());
    }

    @Test
    public void testParseValue() {
        assertEquals("test", this.nameOnlyExpression.parseValue("test"));
        assertEquals("test", this.negatedNameOnlyExpression.parseValue("test"));
        assertEquals("test", this.expression.parseValue("test"));
        assertEquals("test", this.negatedExpression.parseValue("test"));
    }

    @Test
    public void testMatchName() {
        assertTrue(nameOnlyExpression.matchName(request));
        assertTrue(negatedNameOnlyExpression.matchName(request));
        assertTrue(expression.matchName(request));
        assertTrue(negatedExpression.matchName(request));

        assertFalse(negatedExpression.matchName(mock(NativeWebRequest.class)));
    }

    @Test
    public void testMatchValue() {
        assertFalse(nameOnlyExpression.matchValue(request));
        assertFalse(negatedNameOnlyExpression.matchValue(request));
        assertTrue(expression.matchValue(request));
        assertTrue(negatedExpression.matchValue(request));
    }

    @Test
    public void testGetExpression() {
        assertEquals("name", this.nameOnlyExpression.getExpression());
        assertEquals("!name", this.negatedNameOnlyExpression.getExpression());
        assertEquals("name=Mercy", this.expression.getExpression());
        assertEquals("name!=Mercy", this.negatedExpression.getExpression());
    }

    @Test
    public void testEquals() {
        assertEquals(this.nameOnlyExpression, this.nameOnlyExpression);
        assertEquals(this.nameOnlyExpression, new WebRequestHeaderExpression("name"));
        assertNotEquals(this.nameOnlyExpression, new WebRequestHeaderExpression("otherName"));
        assertNotEquals(this.nameOnlyExpression, this.negatedNameOnlyExpression);
        assertNotEquals(this.nameOnlyExpression, this.expression);
        assertNotEquals(this.nameOnlyExpression, this.negatedExpression);
        assertNotEquals(this.nameOnlyExpression, new Object());
        assertNotEquals(this.nameOnlyExpression, null);

        assertEquals(this.negatedNameOnlyExpression, this.negatedNameOnlyExpression);
        assertEquals(this.negatedNameOnlyExpression, new WebRequestHeaderExpression("!name"));
        assertNotEquals(this.negatedNameOnlyExpression, new WebRequestHeaderExpression("!otherName"));
        assertNotEquals(this.negatedNameOnlyExpression, this.nameOnlyExpression);
        assertNotEquals(this.negatedNameOnlyExpression, this.expression);
        assertNotEquals(this.negatedNameOnlyExpression, this.negatedExpression);
        assertNotEquals(this.nameOnlyExpression, new Object());
        assertNotEquals(this.nameOnlyExpression, null);

        assertEquals(this.expression, this.expression);
        assertEquals(this.expression, new WebRequestHeaderExpression("name=Mercy"));
        assertNotEquals(this.expression, new WebRequestHeaderExpression("name=Ma"));
        assertNotEquals(this.expression, this.nameOnlyExpression);
        assertNotEquals(this.expression, this.negatedNameOnlyExpression);
        assertNotEquals(this.expression, this.negatedExpression);
        assertNotEquals(this.expression, new Object());
        assertNotEquals(this.expression, null);

        assertEquals(this.negatedExpression, this.negatedExpression);
        assertEquals(this.negatedExpression, new WebRequestHeaderExpression("name!=Mercy"));
        assertNotEquals(this.negatedExpression, new WebRequestHeaderExpression("name!=Ma"));
        assertNotEquals(this.negatedExpression, this.nameOnlyExpression);
        assertNotEquals(this.negatedExpression, this.negatedNameOnlyExpression);
        assertNotEquals(this.negatedExpression, this.expression);
        assertNotEquals(this.negatedExpression, new Object());
        assertNotEquals(this.negatedExpression, null);
    }

    @Test
    public void testHashCode() {
        assertEquals(this.nameOnlyExpression.hashCode(), new WebRequestHeaderExpression("name").hashCode());
        assertEquals(this.negatedNameOnlyExpression.hashCode(), new WebRequestHeaderExpression("!name").hashCode());
        assertEquals(this.expression.hashCode(), new WebRequestHeaderExpression("name=Mercy").hashCode());
        assertEquals(this.negatedExpression.hashCode(), new WebRequestHeaderExpression("name!=Mercy").hashCode());
    }

    @Test
    public void testToString() {
        assertEquals(this.nameOnlyExpression.toString(), "name");
        assertEquals(this.negatedNameOnlyExpression.toString(), "!name");
        assertEquals(this.expression.toString(), "name=Mercy");
        assertEquals(this.negatedExpression.toString(), "name!=Mercy");
    }

    @Test
    public void testConstructorWithoutName() {
        WebRequestHeaderExpression expression = new WebRequestHeaderExpression("=Mercy");
        assertFalse(expression.isNegated());
        assertEquals(EMPTY_STRING, expression.getName());
        assertEquals("Mercy", expression.getValue());
    }
}