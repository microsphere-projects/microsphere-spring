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
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static io.microsphere.spring.web.rule.WebRequestParamExpression.parseExpressions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebRequestParamExpression} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebRequestParamExpression
 * @since 1.0.0
 */
public class WebRequestParamExpressionTest extends BaseNameValueExpressionTest<WebRequestParamExpression> {

    WebRequestParamExpression nameOnlyExpression;

    WebRequestParamExpression expression;

    WebRequestParamExpression negatedExpression;

    MockServletWebRequest request;

    @BeforeEach
    void setUp() {
        List<WebRequestParamExpression> expressions = parseExpressions("name", "name=Mercy", "name!=Mercy");
        this.nameOnlyExpression = expressions.get(0);
        this.expression = expressions.get(1);
        this.negatedExpression = expressions.get(2);
        this.request = new MockServletWebRequest();
        MockHttpServletRequest mockHttpServletRequest = this.request.getMockHttpServletRequest();
        mockHttpServletRequest.addParameter("name", "Mercy");
    }

    @Override
    void testGetName() {
        assertEquals("name", nameOnlyExpression.getName());
        assertEquals("name", expression.getName());
        assertEquals("name", negatedExpression.getName());
    }

    @Override
    void testGetValue() {
        assertNull(nameOnlyExpression.getValue());
        assertEquals("Mercy", expression.getValue());
        assertEquals("Mercy", negatedExpression.getValue());
    }

    @Override
    void testIsNegated() {
        assertFalse(nameOnlyExpression.isNegated);
        assertFalse(expression.isNegated);
        assertTrue(negatedExpression.isNegated);
    }

    @Override
    void testMatch() {
        assertTrue(nameOnlyExpression.match(request));
        assertTrue(expression.match(request));
        assertFalse(negatedExpression.match(request));
    }

    @Override
    void testIsCaseSensitiveName() {
        assertTrue(this.nameOnlyExpression.isCaseSensitiveName());
        assertTrue(this.expression.isCaseSensitiveName());
        assertTrue(this.negatedExpression.isCaseSensitiveName());
    }

    @Override
    void testParseValue() {
        assertEquals("test", this.nameOnlyExpression.parseValue("test"));
        assertEquals("test", this.expression.parseValue("test"));
        assertEquals("test", this.negatedExpression.parseValue("test"));
    }

    @Override
    void testMatchName() {
        assertTrue(nameOnlyExpression.matchName(request));
        assertTrue(expression.matchName(request));
        assertTrue(negatedExpression.matchName(request));
    }

    @Override
    void testMatchValue() {
        assertFalse(nameOnlyExpression.matchValue(request));
        assertTrue(expression.matchValue(request));
        assertTrue(negatedExpression.matchValue(request));
    }

    @Override
    void testEquals() {
        assertNotEquals(this, this.nameOnlyExpression);
        assertNotEquals(this.nameOnlyExpression, this.expression);
        assertNotEquals(this.expression, this.negatedExpression);

        assertEquals(this.nameOnlyExpression, new WebRequestParamExpression("name"));
        assertEquals(this.expression, new WebRequestParamExpression("name=Mercy"));
        assertEquals(this.negatedExpression, new WebRequestParamExpression("name!=Mercy"));
    }

    @Override
    void testHashCode() {
        assertEquals(this.nameOnlyExpression.hashCode(), new WebRequestParamExpression("name").hashCode());
        assertEquals(this.expression.hashCode(), new WebRequestParamExpression("name=Mercy").hashCode());
        assertEquals(this.negatedExpression.hashCode(), new WebRequestParamExpression("name!=Mercy").hashCode());
    }

    @Override
    void testToString() {
        assertEquals(this.nameOnlyExpression.toString(), "name");
        assertEquals(this.expression.toString(), "name=Mercy");
        assertEquals(this.negatedExpression.toString(), "name!=Mercy");
    }
}