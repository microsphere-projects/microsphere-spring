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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebRequestPattensRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class WebRequestPattensRuleTest extends BaseWebRequestRuleTest {

    // Test empty patterns match all requests
    @Test
    public void testEmptyPatternMatchesAllPaths() {
        WebRequestPattensRule rule = new WebRequestPattensRule();
        assertFalse(rule.matches(createWebRequest("/any-path")));
        assertTrue(rule.matches(""));
    }

    // Test exact path matching
    @Test
    public void testExactPathMatching() {
        WebRequestPattensRule rule = new WebRequestPattensRule("/api/users");
        assertTrue(rule.matches(createWebRequest("/api/users")));
        assertFalse(rule.matches("/api/products"));
    }

    // Test wildcard pattern matching
    @Test
    public void testWildcardPatternMatching() {
        WebRequestPattensRule rule = new WebRequestPattensRule("/api/*");
        assertTrue(rule.matches(createWebRequest("/api/users")));
        assertTrue(rule.matches("/api/products"));
        assertFalse(rule.matches("/admin/users"));
    }

    // Test suffix pattern matching with extensions
    @Test
    public void testSuffixPatternMatching() {
        List<String> extensions = ofList("json", ".xml");
        WebRequestPattensRule rule = new WebRequestPattensRule(
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
    public void testTrailingSlashMatching() {
        WebRequestPattensRule rule = new WebRequestPattensRule(
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
    public void testMultipleMatchingPatternsSorting() {
        WebRequestPattensRule rule = new WebRequestPattensRule(
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
    public void testPreFlightRequestNotMatched() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/users");
        request.addHeader("Origin", "http://example.com");
        request.addHeader("Access-Control-Request-Method", "GET");
        ServletWebRequest webRequest = new ServletWebRequest(request);

        WebRequestPattensRule rule = new WebRequestPattensRule("/api/*");
        assertFalse(rule.matches(webRequest));
    }

    // Test pattern initialization adds leading slash
    @Test
    public void testPatternInitializationAddsLeadingSlash() {
        WebRequestPattensRule rule = new WebRequestPattensRule("admin", "users");
        assertTrue(rule.getContent().contains("/admin"));
        assertTrue(rule.getContent().contains("/users"));
    }

    @Test
    public void testGetMatchingPatterns() {
        WebRequestPattensRule rule = new WebRequestPattensRule(EMPTY_STRING_ARRAY, null, true, true);
        assertEquals("/api/*.*", rule.getMatchingPattern("/api/*", "/api/data.json"));
    }

    @Override
    public void doTestGetToStringInfix() {
        WebRequestPattensRule rule = new WebRequestPattensRule();
        assertEquals(" || ", rule.getToStringInfix());
    }

    @Override
    protected void doTestEquals() {
        WebRequestPattensRule rule = new WebRequestPattensRule("/api/*");

        assertEquals(rule, rule);
        assertEquals(rule, new WebRequestPattensRule("/api/*"));

        assertNotEquals(rule, new WebRequestPattensRule("/test"));
        assertNotEquals(rule, this);
        assertNotEquals(rule, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestPattensRule rule = new WebRequestPattensRule("/api/*");
        assertEquals(rule.hashCode(), rule.getContent().hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestPattensRule rule = new WebRequestPattensRule("/api/*");
        assertEquals("[/api/*]", rule.toString());
    }
}

