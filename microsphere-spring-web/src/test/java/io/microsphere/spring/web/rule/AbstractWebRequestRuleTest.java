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
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Collection;

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.collection.Sets.ofSet;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AbstractWebRequestRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractWebRequestRule
 * @since 1.0.0
 */
class AbstractWebRequestRuleTest {

    static class AbstractWebRequestRuleImpl extends AbstractWebRequestRule<String> {

        private final Collection<String> content;

        private final String infix;

        public AbstractWebRequestRuleImpl(Collection<String> content, String infix) {
            this.content = content;
            this.infix = infix;
        }

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

        @Override
        public boolean isEmpty() {
            return super.isEmpty();
        }

        @Override
        public boolean equals(Object other) {
            return super.equals(other);
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public String toString() {
            return super.toString();
        }
    }

    AbstractWebRequestRuleImpl createRule(Collection<String> content, String infix) {
        return new AbstractWebRequestRuleImpl(content, infix);
    }

    // isEmpty() ==============================
    @Test
    void testIsEmptyWhenContentIsEmpty() {
        AbstractWebRequestRuleImpl rule = createRule(emptyList(), "||");
        assertTrue(rule.isEmpty());
    }

    @Test
    void testIsEmptyWhenContentIsNotEmpty() {
        AbstractWebRequestRuleImpl rule = createRule(ofList("test"), "||");
        assertFalse(rule.isEmpty());
    }

    // getContent() ============================
    @Test
    void testGetContentWhenEmpty() {
        Collection<String> content = emptyList();
        AbstractWebRequestRuleImpl rule = createRule(content, "||");
        assertTrue(rule.getContent().isEmpty());
    }

    @Test
    void testGetContentWhenNotEmpty() {
        Collection<String> content = ofList("a", "b");
        AbstractWebRequestRuleImpl rule = createRule(content, "||");
        assertEquals(2, rule.getContent().size());
    }

    // getToStringInfix() =====================
    @Test
    void testGetToStringInfix() {
        AbstractWebRequestRuleImpl rule = createRule(emptyList(), "&&");
        assertEquals("&&", rule.getToStringInfix());
    }

    // equals() ================================
    @Test
    void testEqualsWithSameInstance() {
        AbstractWebRequestRuleImpl rule = createRule(ofSet("a"), "||");
        assertTrue(rule.equals(rule));
    }

    @Test
    void testEqualsWithNull() {
        AbstractWebRequestRuleImpl rule = createRule(ofSet("a"), "||");
        assertFalse(rule.equals(null));
    }

    @Test
    void testEqualsWithDifferentClass() {
        AbstractWebRequestRuleImpl rule = createRule(ofSet("a"), "||");
        Object other = new Object();
        assertFalse(rule.equals(other));
    }

    @Test
    void testEqualsWithSameClassAndSameContent() {
        AbstractWebRequestRuleImpl rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRuleImpl rule2 = createRule(ofList("a", "b"), "||");
        assertTrue(rule1.equals(rule2));
    }

    @Test
    void testEqualsWithSameClassButDifferentContent() {
        AbstractWebRequestRuleImpl rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRuleImpl rule2 = createRule(ofList("c", "d"), "||");
        assertFalse(rule1.equals(rule2));
    }

    // hashCode() ==============================
    @Test
    void testHashCodeForEqualObjects() {
        AbstractWebRequestRuleImpl rule1 = createRule(ofList("a", "b"), "||");
        AbstractWebRequestRuleImpl rule2 = createRule(ofList("a", "b"), "||");
        assertEquals(rule1.hashCode(), rule2.hashCode());
    }

    @Test
    void testHashCodeForUnequalObjects() {
        AbstractWebRequestRuleImpl rule1 = createRule(ofSet("a"), "||");
        AbstractWebRequestRuleImpl rule2 = createRule(ofSet("b"), "||");
        assertNotEquals(rule1.hashCode(), rule2.hashCode());
    }

    // toString() ==============================
    @Test
    void testToStringWithEmptyContent() {
        AbstractWebRequestRuleImpl rule = createRule(emptyList(), "||");
        assertEquals("[]", rule.toString());
    }

    @Test
    void testToStringWithSingleContent() {
        AbstractWebRequestRuleImpl rule = createRule(ofSet("test"), "||");
        assertEquals("[test]", rule.toString());
    }

    @Test
    void testToStringWithMultipleContents() {
        AbstractWebRequestRuleImpl rule = createRule(ofList("a", "b"), "||");
        assertEquals("[a||b]", rule.toString());
    }

    @Test
    void testToStringWithCustomInfix() {
        AbstractWebRequestRuleImpl rule = createRule(ofList("x", "y"), " && ");
        assertEquals("[x && y]", rule.toString());
    }
}

