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

import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.spring.test.util.SpringTestWebUtils.createWebRequest;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;

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
        assertTrue(rule.matches(request));
    }

    @Test
    public void testAllRulesMatch() {
        WebRequestRule rule1 = request -> true;
        WebRequestRule rule2 = request -> true;

        CompositeWebRequestRule rule = new CompositeWebRequestRule(rule1, rule2);
        NativeWebRequest request = createWebRequest();
        assertTrue(rule.matches(request));
    }

    @Test
    public void testPartialMatchFailure() {
        WebRequestRule rule1 = request -> true;
        WebRequestRule rule2 = request -> false;

        CompositeWebRequestRule rule = new CompositeWebRequestRule(rule1, rule2);
        NativeWebRequest request = createWebRequest();
        assertFalse(rule.matches(request));
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
        assertFalse(rule.matches(request));
    }

    @Override
    void doTestGetToStringInfix() {
        CompositeWebRequestRule rule = new CompositeWebRequestRule();
        assertNotNull(rule);
    }

    @Override
    protected void doTestEquals() {
        WebRequestProducesRule rule1 = new WebRequestProducesRule(APPLICATION_JSON_VALUE);
        WebRequestConsumesRule rule2 = new WebRequestConsumesRule(APPLICATION_XML_VALUE);

        CompositeWebRequestRule composite = new CompositeWebRequestRule(rule1, rule2);

        assertEquals(composite, composite);
        assertEquals(composite, new CompositeWebRequestRule(rule1, rule2));

        assertNotEquals(composite, new CompositeWebRequestRule(rule1));
        assertNotEquals(composite, this);
        assertNotEquals(composite, null);
    }

    @Override
    protected void doTestHashCode() {
        WebRequestProducesRule rule1 = new WebRequestProducesRule(APPLICATION_JSON_VALUE);
        WebRequestConsumesRule rule2 = new WebRequestConsumesRule(APPLICATION_XML_VALUE);

        CompositeWebRequestRule composite = new CompositeWebRequestRule(rule1, rule2);

        assertEquals(composite.hashCode(), ofList(rule1, rule2).hashCode());
    }

    @Override
    protected void doTestToString() {
        WebRequestProducesRule rule1 = new WebRequestProducesRule(APPLICATION_JSON_VALUE);
        WebRequestConsumesRule rule2 = new WebRequestConsumesRule(APPLICATION_XML_VALUE);

        CompositeWebRequestRule composite = new CompositeWebRequestRule(rule1, rule2);

        assertEquals("[[application/json], [application/xml]]", composite.toString());
    }
}