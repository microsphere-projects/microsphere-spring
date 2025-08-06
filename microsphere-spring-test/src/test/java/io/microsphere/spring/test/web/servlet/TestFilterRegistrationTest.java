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

package io.microsphere.spring.test.web.servlet;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testFilterClass;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testFilterClassName;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testFilterName;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletName;
import static jakarta.servlet.DispatcherType.REQUEST;
import static java.util.EnumSet.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

;

/**
 * {@link TestFilterRegistration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestFilterRegistration
 * @since 1.0.0
 */
public class TestFilterRegistrationTest {

    private TestFilterRegistration registration;

    @BeforeEach
    public void setUp() throws Exception {
        TestServletContext servletContext = new TestServletContext();
        this.registration = (TestFilterRegistration) servletContext.addFilter(testFilterName, testFilterClass);
    }

    @Test
    public void testSetAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
        this.registration.setAsyncSupported(true);
        assertTrue(this.registration.isAsyncSupported());
    }

    @Test
    public void testGetName() {
        assertEquals(testFilterName, this.registration.getName());
    }

    @Test
    public void testGetClassName() {
        assertEquals(testFilterClassName, this.registration.getClassName());
    }

    @Test
    public void testSetInitParameter() {
        assertTrue(this.registration.setInitParameter("paramName", "paramValue"));
        assertFalse(this.registration.setInitParameter("paramName", "paramValue"));
        assertEquals("paramValue", this.registration.getInitParameter("paramName"));
    }

    @Test
    public void testGetInitParameter() {
        assertNull(this.registration.getInitParameter("paramName"));
    }

    @Test
    public void testSetInitParameters() {
        Set<String> parameterNames = this.registration.setInitParameters(of("paramName", "paramValue"));
        assertTrue(parameterNames.contains("paramName"));
        assertEquals("paramValue", this.registration.getInitParameter("paramName"));
    }

    @Test
    public void testGetInitParameters() {
        assertEquals(0, this.registration.getInitParameters().size());
    }

    @Test
    public void testAddMappingForServletNames() {
        this.registration.addMappingForServletNames(of(REQUEST), true, testServletName);
        assertTrue(this.registration.getServletNameMappings().contains(testServletName));
    }

    @Test
    public void testGetServletNameMappings() {
        assertTrue(this.registration.getServletNameMappings().isEmpty());
    }

    @Test
    public void testAddMappingForUrlPatterns() {
        this.registration.addMappingForUrlPatterns(of(REQUEST), true, "/*");
        assertTrue(this.registration.getUrlPatternMappings().contains("/*"));
    }

    @Test
    public void testGetUrlPatternMappings() {
        assertTrue(this.registration.getUrlPatternMappings().isEmpty());
    }

    @Test
    public void testGetFilter() {
        assertNotNull(this.registration.getFilter());
    }

    @Test
    public void testIsAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
    }
}