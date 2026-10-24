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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.spring.web.rule.WebRequestPatternsRule.EMPTY_PATH_PATTERN;
import static io.microsphere.spring.web.rule.WebRequestPatternsRule.hasPattern;
import static io.microsphere.spring.web.rule.WebRequestPatternsRule.initPatterns;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link WebRequestPatternsRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class WebRequestPatternsRuleTest extends BaseWebRequestRuleTest {

    @Test
    public void testHasPattern() {
        assertFalse(hasPattern(null));
        assertFalse(hasPattern(EMPTY_STRING_ARRAY));
        assertFalse(hasPattern(ofArray(EMPTY_STRING)));
        assertTrue(hasPattern(ofArray("/*")));
    }

    @Test
    public void testInitPatterns() {
        assertSame(EMPTY_PATH_PATTERN, initPatterns(null));
        assertSame(EMPTY_PATH_PATTERN, initPatterns(EMPTY_STRING_ARRAY));
        assertSame(EMPTY_PATH_PATTERN, initPatterns(ofArray(EMPTY_STRING)));

        assertEquals(ofSet("/*", "/api"), initPatterns(ofArray("/*", "api")));
    }

    // Test empty patterns match all requests
    @Test
    void testEmptyPatternMatchesAllPaths() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule();
        assertFalse(rule.matches(createWebRequest("/any-path")));
        assertTrue(rule.matches(""));
    }

    // Test exact path matching
    @Test
    void testExactPathMatching() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/users");
        assertTrue(rule.matches(createWebRequest("/api/users")));
        assertFalse(rule.matches("/api/products"));
    }

    // Test wildcard pattern matching
    @Test
    void testWildcardPatternMatching() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/*");
        assertTrue(rule.matches(createWebRequest("/api/users")));
        assertTrue(rule.matches("/api/products"));
        assertFalse(rule.matches("/admin/users"));
    }

    // Test suffix pattern matching with extensions
    @Test
    void testSuffixPatternMatching() {
        List<String> extensions = ofList("json", ".xml");
        WebRequestPatternsRule rule = new WebRequestPatternsRule(
                new String[]{"/data"},
                null,
                true,
                true,
                extensions
        );

        assertTrue(rule.matches(createWebRequest("/data.json")));
        assertTrue(rule.matches("/data.xml"));
        assertFalse(rule.matches("/data.txt"));
    }

    // Test trailing slash matching
    @Test
    void testTrailingSlashMatching() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(
                new String[]{"/resources"},
                true,
                new AntPathMatcher()
        );

        assertTrue(rule.matches(createWebRequest("/resources/")));
        assertTrue(rule.matches("/resources"));
        assertFalse(rule.matches("/assets"));
    }

    // Test multiple matching patterns sorting
    @Test
    void testMultipleMatchingPatternsSorting() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(
                "/api/**",
                "/api/v1/users",
                "/api/v1/*"
        );

        List<String> matches = rule.getMatchingPatterns("/api/v1/users");
        assertEquals(3, matches.size());
        assertEquals("/api/v1/users", matches.get(0));  // Most specific
        assertEquals("/api/v1/*", matches.get(1));
        assertEquals("/api/**", matches.get(2));  // Least specific
    }

    // Test pre-flight request does not match
    @Test
    void testPreFlightRequestNotMatched() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/users");
        request.addHeader("Origin", "http://example.com");
        request.addHeader("Access-Control-Request-Method", "GET");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/*");
        assertFalse(rule.matches(webRequest));
    }

    // Test pattern initialization adds leading slash
    @Test
    void testPatternInitializationAddsLeadingSlash() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("admin", "users");
        assertTrue(rule.getContent().contains("/admin"));
        assertTrue(rule.getContent().contains("/users"));
    }

    @Test
    void testGetMatchingPatterns() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true);
        assertEquals("/api/*.*", rule.getMatchingPattern("/api/*", "/api/data.json"));
    }

    @Test
    public void testGetMatchingPatternOnWildcardExtensionMatched() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true);
        assertNull(rule.getMatchingPattern("/", "/data.json"));
    }

    @Test
    public void testGetMatchingPatternWithSuffix() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true);
        assertEquals("/*.json", rule.getMatchingPattern("/*.json", "/data.json"));
    }

    @Test
    public void testGetMatchingPatternWithoutTrailingSlashMatch() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, false);
        assertNull(rule.getMatchingPattern("/*.txt", "/data.json"));
    }

    @Test
    public void testGetMatchingPatternOnWildcardExtensionNotMatched() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true);
        assertNull(rule.getMatchingPattern("/", "/data.json"));
    }

    @Test
    public void testGetMatchingPatternOnFileExtensionMatched() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true, ofList("json"));
        assertEquals("/*.json", rule.getMatchingPattern("/*", "/data.json"));
    }

    @Test
    public void testGetMatchingPatternOnFileExtensionNotMatched() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule(EMPTY_STRING_ARRAY, null, true, true, ofList("json"));
        assertEquals("/*", rule.getMatchingPattern("/*", "/data"));
    }

    @Override
    void doTestGetToStringInfix() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule();
        assertEquals(" || ", rule.getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/*");

        assertEquals(rule, rule);
        assertEquals(rule, new WebRequestPatternsRule("/api/*"));

        assertNotEquals(rule, new WebRequestPatternsRule("/test"));
        assertNotEquals(rule, this);
        assertNotEquals(rule, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/*");
        assertEquals(rule.hashCode(), rule.getContent().hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestPatternsRule rule = new WebRequestPatternsRule("/api/*");
        assertEquals("[/api/*]", rule.toString());
    }
}