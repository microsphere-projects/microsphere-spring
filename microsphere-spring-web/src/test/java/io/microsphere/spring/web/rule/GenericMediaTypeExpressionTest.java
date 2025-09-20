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

import java.util.Map;

import static io.microsphere.collection.MapUtils.ofMap;
import static io.microsphere.spring.web.rule.GenericMediaTypeExpression.matchParameters;
import static io.microsphere.spring.web.rule.GenericMediaTypeExpression.of;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.TEXT_PLAIN;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.MediaType.parseMediaType;


/**
 * {@link GenericMediaTypeExpression} test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class GenericMediaTypeExpressionTest {

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
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(expected, expression.getMediaType());
    }

    // ==================== isNegated() ====================
    @Test
    void testIsNegatedWhenNotNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expression.isNegated());
    }

    @Test
    void testIsNegatedWhenNegated() {
        GenericMediaTypeExpression expression = new GenericMediaTypeExpression("!" + TEXT_PLAIN_VALUE);
        assertTrue(expression.isNegated());
    }

    // ==================== compareTo() ====================
    @Test
    void testCompareToWhenMoreSpecific() {
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        assertTrue(specific.compareTo(general) < 0);
    }

    @Test
    void testCompareToWhenLessSpecific() {
        GenericMediaTypeExpression general = new GenericMediaTypeExpression("text/*");
        GenericMediaTypeExpression specific = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(general.compareTo(specific) > 0);
    }

    @Test
    void testCompareToWhenEqualSpecificity() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(0, expr1.compareTo(expr2));
    }

    // ==================== equals() ====================
    @Test
    void testEqualsSameInstance() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(expr.equals(expr));
    }

    @Test
    void testEqualsNull() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expr.equals(null));
    }

    @Test
    void testEqualsDifferentType() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertFalse(expr.equals(TEXT_PLAIN_VALUE));
    }

    @Test
    void testEqualsDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(APPLICATION_JSON_VALUE);
        assertFalse(expr1.equals(expr2));
    }

    @Test
    void testEqualsDifferentNegatedFlag() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression("!" + TEXT_PLAIN_VALUE);
        assertFalse(expr1.equals(expr2));
    }

    @Test
    void testEqualsSameProperties() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertTrue(expr1.equals(expr2));
    }

    // ==================== hashCode() ====================
    @Test
    void testHashCodeSameMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(expr1.hashCode(), expr2.hashCode());
    }

    @Test
    void testHashCodeDifferentMediaType() {
        GenericMediaTypeExpression expr1 = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        GenericMediaTypeExpression expr2 = new GenericMediaTypeExpression(APPLICATION_JSON_VALUE);
        assertNotEquals(expr1.hashCode(), expr2.hashCode());
    }

    // ==================== toString() ====================
    @Test
    void testToStringWhenNotNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression(TEXT_PLAIN_VALUE);
        assertEquals(TEXT_PLAIN_VALUE, expr.toString());
    }

    @Test
    void testToStringWhenNegated() {
        GenericMediaTypeExpression expr = new GenericMediaTypeExpression("!" + TEXT_PLAIN_VALUE);
        assertEquals("!text/plain", expr.toString());
    }

    // ==================== static of() ====================
    @Test
    void testOfWhenNotNegated() {
        GenericMediaTypeExpression expr = of(TEXT_PLAIN_VALUE);
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertFalse(expr.isNegated());
    }

    @Test
    void testOfWhenNegated() {
        GenericMediaTypeExpression expr = of("!" + TEXT_PLAIN_VALUE);
        assertEquals(TEXT_PLAIN, expr.getMediaType());
        assertTrue(expr.isNegated());
    }

    // ==================== matchParameters() ====================
    @Test
    void testMatchParameters() {
        ProduceMediaTypeExpression expr = new ProduceMediaTypeExpression("text/plain;charset=utf-8");
        MediaType acceptedMediaType = parseMediaType("text/plain;charset=UTF-8");
        assertTrue(expr.matchParameters(acceptedMediaType));

        expr = new ProduceMediaTypeExpression("text/plain;charset=GBK");
        assertFalse(expr.matchParameters(acceptedMediaType));

        acceptedMediaType = parseMediaType("text/plain;charset=UTF-16");
        assertFalse(expr.matchParameters(acceptedMediaType));
    }

    // ==================== static matchParameters() ====================
    @Test
    void testMatchParametersOnParameterMatch() {
        Map<String, String> sourceParameters = ofMap("charset", "UTF-8");
        Map<String, String> targetParameters = ofMap("charset", "UTF-8");
        assertTrue(matchParameters(sourceParameters, targetParameters));
    }

    @Test
    void testMatchParametersOnNullEntry() {
        Map<String, String> sourceParameters = ofMap("charset", null);
        Map<String, String> targetParameters = ofMap("charset", "UTF-16");
        assertTrue(matchParameters(sourceParameters, targetParameters));
    }

    @Test
    void testMatchParametersOnParameterMissing() {
        Map<String, String> sourceParameters = ofMap("charset", "UTF-8");
        Map<String, String> targetParameters = emptyMap();
        assertTrue(matchParameters(sourceParameters, targetParameters));
    }

    @Test
    void testMatchParametersOnParameterNotMatch() {
        Map<String, String> sourceParameters = ofMap("charset", "UTF-8");
        Map<String, String> targetParameters = ofMap("charset", "UTF-16");
        assertFalse(matchParameters(sourceParameters, targetParameters));
    }
}