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
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestParamExpression.parseExpressions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * {@link WebRequestParamExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestParamExpression
 * @since 1.0.0
 */
class WebRequestParamExpressionTest extends BaseNameValueExpressionTest<WebRequestParamExpression> {

    WebRequestParamExpression nameOnlyExpression;

    WebRequestParamExpression negatedNameOnlyExpression;

    WebRequestParamExpression expression;

    WebRequestParamExpression negatedExpression;

    MockServletWebRequest request;

    @BeforeEach
    void setUp() {
        List<WebRequestParamExpression> expressions = parseExpressions("name", "!name", "name=Mercy", "name!=Mercy");
        this.nameOnlyExpression = expressions.get(0);
        this.negatedNameOnlyExpression = expressions.get(1);
        this.expression = expressions.get(2);
        this.negatedExpression = expressions.get(3);
        this.request = new MockServletWebRequest();
        MockHttpServletRequest mockHttpServletRequest = this.request.getMockHttpServletRequest();
        mockHttpServletRequest.addParameter("name", "Mercy");
    }

    @Test
    void testGetName() {
        assertEquals("name", nameOnlyExpression.getName());
        assertEquals("name", negatedNameOnlyExpression.getName());
        assertEquals("name", expression.getName());
        assertEquals("name", negatedExpression.getName());
    }

    @Test
    void testGetValue() {
        assertNull(nameOnlyExpression.getValue());
        assertNull(negatedNameOnlyExpression.getValue());
        assertEquals("Mercy", expression.getValue());
        assertEquals("Mercy", negatedExpression.getValue());
    }

    @Test
    void testIsNegated() {
        assertFalse(nameOnlyExpression.isNegated());
        assertTrue(negatedNameOnlyExpression.isNegated());
        assertFalse(expression.isNegated());
        assertTrue(negatedExpression.isNegated());
    }

    @Test
    void testMatch() {
        assertTrue(nameOnlyExpression.match(request));
        assertFalse(negatedNameOnlyExpression.match(request));
        assertTrue(expression.match(request));
        assertFalse(negatedExpression.match(request));
    }

    @Test
    void testIsCaseSensitiveName() {
        assertTrue(this.nameOnlyExpression.isCaseSensitiveName());
        assertTrue(this.negatedNameOnlyExpression.isCaseSensitiveName());
        assertTrue(this.expression.isCaseSensitiveName());
        assertTrue(this.negatedExpression.isCaseSensitiveName());
    }

    @Test
    void testParseValue() {
        assertEquals("test", this.nameOnlyExpression.parseValue("test"));
        assertEquals("test", this.negatedNameOnlyExpression.parseValue("test"));
        assertEquals("test", this.expression.parseValue("test"));
        assertEquals("test", this.negatedExpression.parseValue("test"));
    }

    @Test
    void testMatchName() {
        assertTrue(nameOnlyExpression.matchName(request));
        assertTrue(negatedNameOnlyExpression.matchName(request));
        assertTrue(expression.matchName(request));
        assertTrue(negatedExpression.matchName(request));

        assertFalse(negatedExpression.matchName(mock(NativeWebRequest.class)));
    }

    @Test
    void testMatchValue() {
        assertFalse(nameOnlyExpression.matchValue(request));
        assertFalse(negatedNameOnlyExpression.matchValue(request));
        assertTrue(expression.matchValue(request));
        assertTrue(negatedExpression.matchValue(request));
    }

    @Test
    void testGetExpression() {
        assertEquals("name", this.nameOnlyExpression.getExpression());
        assertEquals("!name", this.negatedNameOnlyExpression.getExpression());
        assertEquals("name=Mercy", this.expression.getExpression());
        assertEquals("name!=Mercy", this.negatedExpression.getExpression());
    }

    @Test
    void testEquals() {
        assertEquals(this.nameOnlyExpression, this.nameOnlyExpression);
        assertEquals(this.nameOnlyExpression, new WebRequestParamExpression("name"));
        assertNotEquals(this.nameOnlyExpression, this.expression);
        assertNotEquals(this.nameOnlyExpression, this);
        assertNotEquals(this.nameOnlyExpression, null);

        assertEquals(this.negatedNameOnlyExpression, this.negatedNameOnlyExpression);
        assertEquals(this.negatedNameOnlyExpression, new WebRequestParamExpression("!name"));
        assertNotEquals(this.negatedNameOnlyExpression, this.nameOnlyExpression);
        assertNotEquals(this.nameOnlyExpression, this);
        assertNotEquals(this.nameOnlyExpression, null);

        assertEquals(this.expression, this.expression);
        assertEquals(this.expression, new WebRequestParamExpression("name=Mercy"));
        assertNotEquals(this.expression, this.negatedExpression);
        assertNotEquals(this.expression, this);
        assertNotEquals(this.expression, null);

        assertEquals(this.negatedExpression, this.negatedExpression);
        assertEquals(this.negatedExpression, new WebRequestParamExpression("name!=Mercy"));
        assertNotEquals(this.negatedExpression, this.nameOnlyExpression);
        assertNotEquals(this.negatedExpression, this);
        assertNotEquals(this.negatedExpression, null);
    }

    @Test
    void testHashCode() {
        assertEquals(this.nameOnlyExpression.hashCode(), new WebRequestParamExpression("name").hashCode());
        assertEquals(this.negatedNameOnlyExpression.hashCode(), new WebRequestParamExpression("!name").hashCode());
        assertEquals(this.expression.hashCode(), new WebRequestParamExpression("name=Mercy").hashCode());
        assertEquals(this.negatedExpression.hashCode(), new WebRequestParamExpression("name!=Mercy").hashCode());
    }

    @Test
    void testToString() {
        assertEquals(this.nameOnlyExpression.toString(), "name");
        assertEquals(this.negatedNameOnlyExpression.toString(), "!name");
        assertEquals(this.expression.toString(), "name=Mercy");
        assertEquals(this.negatedExpression.toString(), "name!=Mercy");
    }
}