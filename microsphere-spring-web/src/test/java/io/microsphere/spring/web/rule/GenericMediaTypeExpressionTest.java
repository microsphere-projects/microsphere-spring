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
import org.springframework.http.MediaType;

import static io.microsphere.spring.web.rule.GenericMediaTypeExpression.of;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;


/**
 * {@link GenericMediaTypeExpression} test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class GenericMediaTypeExpressionTest {

    @Test
    public void testConstructor() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN, false);
        assertEquals(TEXT_PLAIN, expression.getMediaType());
        assertEquals(false, expression.isNegated());
    }

    // ==================== getMediaType() ====================
    @Test
    public void testGetMediaType() {
        MediaType expected = TEXT_PLAIN;
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(expected, expression.getMediaType());
    }

    // ==================== isNegated() ====================
    @Test
    public void testIsNegatedWhenNotNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expression.isNegated());
    }

    @Test
    public void testIsNegatedWhenNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("!text/plain");
        assertTrue(expression.isNegated());
    }

    // ==================== compareTo() ====================
    @Test
    public void testCompareToWhenMoreSpecific() {
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        assertTrue(specific.compareTo(general) < 0);
    }

    @Test
    public void testCompareToWhenLessSpecific() {
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(general.compareTo(specific) > 0);
    }

    @Test
    public void testCompareToWhenEqualSpecificity() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(0, expr1.compareTo(expr2));
    }

    // ==================== equals() ====================
    @Test
    public void testEqualsSameInstance() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(expr.equals(expr));
    }

    @Test
    public void testEqualsNull() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expr.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expr.equals(TEXT_PLAIN_VALUE));
    }

    @Test
    public void testEqualsDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(APPLICATION_JSON_VALUE);
        assertFalse(expr1.equals(expr2));
    }

    @Test
    public void testEqualsDifferentNegatedFlag() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("!text/plain");
        assertFalse(expr1.equals(expr2));
    }

    @Test
    public void testEqualsSameProperties() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(expr1.equals(expr2));
    }

    // ==================== hashCode() ====================
    @Test
    public void testHashCodeSameMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(expr1.hashCode(), expr2.hashCode());
    }

    @Test
    public void testHashCodeDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(APPLICATION_JSON_VALUE);
        assertNotEquals(expr1.hashCode(), expr2.hashCode());
    }

    // ==================== toString() ====================
    @Test
    public void testToStringWhenNotNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(TEXT_PLAIN_VALUE, expr.toString());
    }

    @Test
    public void testToStringWhenNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("!text/plain");
        assertEquals("!text/plain", expr.toString());
    }

    // ==================== static of() ====================
    @Test
    public void testOfWhenNotNegated() {
        GenericMediaTypeExpression expr = of(TEXT_PLAIN_VALUE);
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertFalse(expr.isNegated());
    }

    @Test
    public void testOfWhenNegated() {
        GenericMediaTypeExpression expr = of("!text/plain");
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertTrue(expr.isNegated());
    }
}
