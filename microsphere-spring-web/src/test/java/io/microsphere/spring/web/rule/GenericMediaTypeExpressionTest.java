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
import org.springframework.http.MediaType;

import static io.microsphere.spring.web.rule.GenericMediaTypeExpression.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.TEXT_PLAIN;


/**
 * {@link GenericMediaTypeExpression} test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class GenericMediaTypeExpressionTest {

    @Test
    void testConstructor() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN, false);
        assertEquals(TEXT_PLAIN, expression.getMediaType());
        assertEquals(false, expression.isNegated());
    }

    // ==================== getMediaType() ====================
    @Test
    void testGetMediaType() {
        MediaType expected = TEXT_PLAIN;
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("text/plain");
        assertEquals(expected, expression.getMediaType());
    }

    // ==================== isNegated() ====================
    @Test
    void testIsNegatedWhenNotNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("text/plain");
        assertFalse(expression.isNegated());
    }

    @Test
    void testIsNegatedWhenNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("!text/plain");
        assertTrue(expression.isNegated());
    }

    // ==================== compareTo() ====================
    @Test
    void testCompareToWhenMoreSpecific() {
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        assertTrue(specific.compareTo(general) < 0);
    }

    @Test
    void testCompareToWhenLessSpecific() {
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression("text/plain");
        assertTrue(general.compareTo(specific) > 0);
    }

    @Test
    void testCompareToWhenEqualSpecificity() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertEquals(0, expr1.compareTo(expr2));
    }

    // ==================== equals() ====================
    @Test
    void testEqualsSameInstance() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertTrue(expr.equals(expr));
    }

    @Test
    void testEqualsNull() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertFalse(expr.equals(null));
    }

    @Test
    void testEqualsDifferentType() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertFalse(expr.equals("text/plain"));
    }

    @Test
    void testEqualsDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("application/json");
        assertFalse(expr1.equals(expr2));
    }

    @Test
    void testEqualsDifferentNegatedFlag() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("!text/plain");
        assertFalse(expr1.equals(expr2));
    }

    @Test
    void testEqualsSameProperties() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertTrue(expr1.equals(expr2));
    }

    // ==================== hashCode() ====================
    @Test
    void testHashCodeSameMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertEquals(expr1.hashCode(), expr2.hashCode());
    }

    @Test
    void testHashCodeDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("application/json");
        assertNotEquals(expr1.hashCode(), expr2.hashCode());
    }

    // ==================== toString() ====================
    @Test
    void testToStringWhenNotNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertEquals("text/plain", expr.toString());
    }

    @Test
    void testToStringWhenNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("!text/plain");
        assertEquals("!text/plain", expr.toString());
    }

    // ==================== static of() ====================
    @Test
    void testOfWhenNotNegated() {
        GenericMediaTypeExpression expr = of("text/plain");
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertFalse(expr.isNegated());
    }

    @Test
    void testOfWhenNegated() {
        GenericMediaTypeExpression expr = of("!text/plain");
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertTrue(expr.isNegated());
    }
}
