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
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static io.microsphere.collection.Lists.ofList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link AbstractWebRequestRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractWebRequestRule
 * @since 1.0.0
 */
public class AbstractWebRequestRuleTest {

    private AbstractWebRequestRule<String> createRule(Collection<String> content, String infix) {
        return new AbstractWebRequestRule<String>() {
            @Override
            public boolean matches(NativeWebRequest request) {
                return true;
            }

            @Override
            protected Collection<String> getContent() {
                return content;
            }

            @Override
            protected String getToStringInfix() {
                return infix;
            }
        };
    }

    // isEmpty() ==============================
    @Test
    public void testIsEmptyWhenContentIsEmpty() {
        AbstractWebRequestRule<String> rule = createRule(emptyList(), "||");
        assertTrue(rule.isEmpty());
    }

    @Test
    public void testIsEmptyWhenContentIsNotEmpty() {
        AbstractWebRequestRule<String> rule = createRule(singleton("test"), "||");
        assertFalse(rule.isEmpty());
    }

    // getContent() ============================
    @Test
    public void testGetContentWhenEmpty() {
        Collection<String> content = emptyList();
        AbstractWebRequestRule<String> rule = createRule(content, "||");
        assertTrue(rule.getContent().isEmpty());
    }

    @Test
    public void testGetContentWhenNotEmpty() {
        Collection<String> content = ofList("a", "b");
        AbstractWebRequestRule<String> rule = createRule(content, "||");
        assertEquals(2, rule.getContent().size());
    }

    // getToStringInfix() =====================
    @Test
    public void testGetToStringInfix() {
        AbstractWebRequestRule<String> rule = createRule(emptyList(), "&&");
        assertEquals("&&", rule.getToStringInfix());
    }

    // equals() ================================
    @Test
    public void testEqualsWithSameInstance() {
        AbstractWebRequestRule<String> rule = createRule(singleton("a"), "||");
        assertTrue(rule.equals(rule));
    }

    @Test
    public void testEqualsWithNull() {
        AbstractWebRequestRule<String> rule = createRule(singleton("a"), "||");
        assertFalse(rule.equals(null));
    }

    @Test
    public void testEqualsWithDifferentClass() {
        AbstractWebRequestRule<String> rule = createRule(singleton("a"), "||");
        Object other = new Object();
        assertFalse(rule.equals(other));
    }

    @Test
    public void testEqualsWithSameClassAndSameContent() {
        AbstractWebRequestRule<String> rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRule<String> rule2 = createRule(ofList("a", "b"), "||");
        assertTrue(rule1.equals(rule2));
    }

    @Test
    public void testEqualsWithSameClassButDifferentContent() {
        AbstractWebRequestRule<String> rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRule<String> rule2 = createRule(ofList("c", "d"), "||");
        assertFalse(rule1.equals(rule2));
    }

    // hashCode() ==============================
    @Test
    public void testHashCodeForEqualObjects() {
        AbstractWebRequestRule<String> rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRule<String> rule2 = createRule(ofList("a", "b"), "||");
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    public void testHashCodeForUnequalObjects() {
        AbstractWebRequestRule<String> rule1 = createRule(singleton("a"), "||");
        AbstractWebRequestRule<String> rule2 = createRule(singleton("b"), "||");
        assertNotEquals(rule1.hashCode(), rule2.hashCode());
    }

    // toString() ==============================
    @Test
    public void testToStringWithEmptyContent() {
        AbstractWebRequestRule<String> rule = createRule(emptyList(), "||");
        assertEquals("[]", rule.toString());
    }

    @Test
    public void testToStringWithSingleContent() {
        AbstractWebRequestRule<String> rule = createRule(singleton("test"), "||");
        assertEquals("[test]", rule.toString());
    }

    @Test
    public void testToStringWithMultipleContents() {
        AbstractWebRequestRule<String> rule = createRule(ofList("a", "b"), "||");
        assertEquals("[a||b]", rule.toString());
    }

    @Test
    public void testToStringWithCustomInfix() {
        AbstractWebRequestRule<String> rule = createRule(ofList("x", "y"), " && ");
        assertEquals("[x && y]", rule.toString());
    }
}

