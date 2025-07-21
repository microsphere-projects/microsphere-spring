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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * {@link CompositeWebRequestRule} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see CompositeWebRequestRule
 * @since 1.0.0
 */
public class CompositeWebRequestRuleTest extends BaseWebRequestRuleTest {

    @Test
    public void testEmptyRules() {
        CompositeWebRequestRule rule = new CompositeWebRequestRule();
        NativeWebRequest request = createWebRequest();
        assertTrue("Empty rules should always match", rule.matches(request));
    }

    @Test
    public void testAllRulesMatch() {
        WebRequestRule rule1 = request -> true;
        WebRequestRule rule2 = request -> true;

        CompositeWebRequestRule rule = new CompositeWebRequestRule(rule1, rule2);
        NativeWebRequest request = createWebRequest();
        assertTrue("Should match when all rules are true", rule.matches(request));
    }

    @Test
    public void testPartialMatchFailure() {
        WebRequestRule rule1 = request -> true;
        WebRequestRule rule2 = request -> false;

        CompositeWebRequestRule rule = new CompositeWebRequestRule(rule1, rule2);
        NativeWebRequest request = createWebRequest();
        assertFalse("Should fail when any rule fails", rule.matches(request));
    }

    @Test
    public void testNullRules() {
        WebRequestRule validRule = request -> true;
        CompositeWebRequestRule rule = new CompositeWebRequestRule(validRule, null);
        NativeWebRequest request = createWebRequest();
        assertThrows(NullPointerException.class, () -> rule.matches(request));
    }

    @Test
    public void testShortCircuitEvaluation() {
        WebRequestRule failRule = request -> false;
        WebRequestRule neverCalledRule = request -> {
            fail("This rule should not be called");
            return true;
        };

        CompositeWebRequestRule rule = new CompositeWebRequestRule(failRule, neverCalledRule);
        NativeWebRequest request = createWebRequest();
        assertFalse("Should short-circuit on first failure", rule.matches(request));
    }
}