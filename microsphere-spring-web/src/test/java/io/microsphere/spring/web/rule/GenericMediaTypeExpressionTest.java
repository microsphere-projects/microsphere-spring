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
import static org.springframework.http.MediaType.TEXT_PLAIN;


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
        assertEquals("MediaType should match", TEXT_PLAIN, expression.getMediaType());
        assertEquals("Negated flag should be false", false, expression.isNegated());
    }

    // ==================== getMediaType() ====================
    @Test
    public void testGetMediaType() {
        MediaType expected = TEXT_PLAIN;
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("text/plain");
        assertEquals("MediaType should match", expected, expression.getMediaType());
    }

    // ==================== isNegated() ====================
    @Test
    public void testIsNegatedWhenNotNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("text/plain");
        assertFalse("Should not be negated", expression.isNegated());
    }

    @Test
    public void testIsNegatedWhenNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("!text/plain");
        assertTrue("Should be negated", expression.isNegated());
    }

    // ==================== compareTo() ====================
    @Test
    public void testCompareToWhenMoreSpecific() {
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        assertTrue("Specific type should have higher priority", specific.compareTo(general) < 0);
    }

    @Test
    public void testCompareToWhenLessSpecific() {
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression("text/plain");
        assertTrue("General type should have lower priority", general.compareTo(specific) > 0);
    }

    @Test
    public void testCompareToWhenEqualSpecificity() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertEquals("Equal specificity should return 0", 0, expr1.compareTo(expr2));
    }

    // ==================== equals() ====================
    @Test
    public void testEqualsSameInstance() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertTrue("Same instance should be equal", expr.equals(expr));
    }

    @Test
    public void testEqualsNull() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertFalse("Null comparison should return false", expr.equals(null));
    }

    @Test
    public void testEqualsDifferentType() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertFalse("Different object types should not be equal", expr.equals("text/plain"));
    }

    @Test
    public void testEqualsDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("application/json");
        assertFalse("Different media types should not be equal", expr1.equals(expr2));
    }

    @Test
    public void testEqualsDifferentNegatedFlag() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("!text/plain");
        assertFalse("Different negated flags should not be equal", expr1.equals(expr2));
    }

    @Test
    public void testEqualsSameProperties() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertTrue("Same properties should be equal", expr1.equals(expr2));
    }

    // ==================== hashCode() ====================
    @Test
    public void testHashCodeSameMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("text/plain");
        assertEquals("Same mediaType should produce same hash code",
                expr1.hashCode(), expr2.hashCode());
    }

    @Test
    public void testHashCodeDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression("text/plain");
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("application/json");
        assertNotEquals("Different mediaType should produce different hash codes",
                expr1.hashCode(), expr2.hashCode());
    }

    // ==================== toString() ====================
    @Test
    public void testToStringWhenNotNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("text/plain");
        assertEquals("Non-negated should return plain media type",
                "text/plain", expr.toString());
    }

    @Test
    public void testToStringWhenNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("!text/plain");
        assertEquals("Negated should include exclamation mark",
                "!text/plain", expr.toString());
    }

    // ==================== static of() ====================
    @Test
    public void testOfWhenNotNegated() {
        GenericMediaTypeExpression expr = of("text/plain");
        assertEquals("MediaType should match", TEXT_PLAIN, expr.getMediaType());
        assertFalse("Should not be negated", expr.isNegated());
    }

    @Test
    public void testOfWhenNegated() {
        GenericMediaTypeExpression expr = of("!text/plain");
        assertEquals("MediaType should match", TEXT_PLAIN, expr.getMediaType());
        assertTrue("Should be negated", expr.isNegated());
    }
}
