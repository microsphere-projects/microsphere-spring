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
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestHeaderExpression.parseExpressions;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebRequestHeaderExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestHeaderExpression
 * @since 1.0.0
 */
public class WebRequestHeaderExpressionTest extends BaseNameValueExpressionTest<WebRequestHeaderExpression> {

    WebRequestHeaderExpression nameOnlyExpression;

    WebRequestHeaderExpression expression;

    WebRequestHeaderExpression negatedExpression;

    MockServletWebRequest request;

    @Before
    public void before() {
        List<WebRequestHeaderExpression> expressions = parseExpressions("name", "name=Mercy", "name!=Mercy");
        this.nameOnlyExpression = expressions.get(0);
        this.expression = expressions.get(1);
        this.negatedExpression = expressions.get(2);
        this.request = new MockServletWebRequest();
        MockHttpServletRequest mockHttpServletRequest = this.request.getMockHttpServletRequest();
        mockHttpServletRequest.addHeader("name", "Mercy");
    }

    @Override
    public void testGetName() {
        assertEquals("name", nameOnlyExpression.getName());
        assertEquals("name", expression.getName());
        assertEquals("name", negatedExpression.getName());
    }

    @Override
    public void testGetValue() {
        assertNull(nameOnlyExpression.getValue());
        assertEquals("Mercy", expression.getValue());
        assertEquals("Mercy", negatedExpression.getValue());
    }

    @Override
    public void testIsNegated() {
        assertFalse(nameOnlyExpression.isNegated);
        assertFalse(expression.isNegated);
        assertTrue(negatedExpression.isNegated);
    }

    @Override
    public void testMatch() {
        assertTrue(nameOnlyExpression.match(request));
        assertTrue(expression.match(request));
        assertFalse(negatedExpression.match(request));
    }

    @Override
    public void testIsCaseSensitiveName() {
        assertFalse(this.nameOnlyExpression.isCaseSensitiveName());
        assertFalse(this.expression.isCaseSensitiveName());
        assertFalse(this.negatedExpression.isCaseSensitiveName());
    }

    @Override
    public void testParseValue() {
        assertEquals("test", this.nameOnlyExpression.parseValue("test"));
        assertEquals("test", this.expression.parseValue("test"));
        assertEquals("test", this.negatedExpression.parseValue("test"));
    }

    @Override
    public void testMatchName() {
        assertTrue(nameOnlyExpression.matchName(request));
        assertTrue(expression.matchName(request));
        assertTrue(negatedExpression.matchName(request));
    }

    @Override
    public void testMatchValue() {
        assertFalse(nameOnlyExpression.matchValue(request));
        assertTrue(expression.matchValue(request));
        assertTrue(negatedExpression.matchValue(request));
    }

    @Override
    public void testEquals() {
        assertNotEquals(this, this.nameOnlyExpression);
        assertNotEquals(this.nameOnlyExpression, this.expression);
        assertNotEquals(this.expression, this.negatedExpression);

        assertEquals(this.nameOnlyExpression, new WebRequestHeaderExpression("name"));
        assertEquals(this.expression, new WebRequestHeaderExpression("name=Mercy"));
        assertEquals(this.negatedExpression, new WebRequestHeaderExpression("name!=Mercy"));
    }

    @Override
    public void testHashCode() {
        assertEquals(this.nameOnlyExpression.hashCode(), new WebRequestHeaderExpression("name").hashCode());
        assertEquals(this.expression.hashCode(), new WebRequestHeaderExpression("name=Mercy").hashCode());
        assertEquals(this.negatedExpression.hashCode(), new WebRequestHeaderExpression("name!=Mercy").hashCode());
    }

    @Override
    public void testToString() {
        assertEquals(this.nameOnlyExpression.toString(), "name");
        assertEquals(this.expression.toString(), "name=Mercy");
        assertEquals(this.negatedExpression.toString(), "name!=Mercy");
    }
}