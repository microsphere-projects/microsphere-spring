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

package io.microsphere.spring.web.servlet.util;


import io.microsphere.spring.test.web.servlet.TestFilter;
import io.microsphere.spring.test.web.servlet.TestServlet;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletRegistration;
import java.util.Map;

import static io.microsphere.spring.web.servlet.util.WebUtils.findFilterRegistrations;
import static io.microsphere.spring.web.servlet.util.WebUtils.findServletRegistrations;
import static io.microsphere.spring.web.servlet.util.WebUtils.getServletContext;
import static io.microsphere.spring.web.servlet.util.WebUtils.isRunningBelowServlet3Container;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebUtils
 * @since 1.0.0
 */
public class WebUtilsTest {

    static final String testServletName = "testServlet";

    static final Class<? extends Servlet> testServletClass = TestServlet.class;

    static final String testServletClassName = testServletClass.getName();

    static final String testFilterName = "testFilter";

    static final Class<? extends Filter> testFilterClass = TestFilter.class;

    static final String testFilterClassName = testFilterClass.getName();

    private TestServletContext servletContext;

    @Before
    public void setUp() {
        this.servletContext = new TestServletContext();
    }

    @Test
    public void testIsRunningBelowServlet3Container() {
        servletContext.setMajorVersion(3);
        assertFalse(isRunningBelowServlet3Container(servletContext));

        servletContext.setMajorVersion(2);
        assertTrue(isRunningBelowServlet3Container(servletContext));
    }

    @Test
    public void testGetServletContext() {
        MockHttpServletRequest request = new MockHttpServletRequest(this.servletContext);
        assertSame(this.servletContext, getServletContext(request));
    }

    @Test
    public void testFindFilterRegistrations() {
        FilterRegistration.Dynamic filterRegistration = this.servletContext.addFilter(testFilterName, testFilterClass);
        Map<String, ? extends FilterRegistration> filterRegistrations = findFilterRegistrations(this.servletContext, testFilterClass);
        assertTrue(filterRegistrations.containsKey(testFilterName));
        assertTrue(filterRegistrations.containsValue(filterRegistration));
    }

    @Test
    public void testFindFilterRegistrationsOnEmptyServletContext() {
        assertSame(emptyMap(), findFilterRegistrations(this.servletContext, testFilterClass));
    }

    @Test
    public void testFindFilterRegistrationsOnNotFound() {
        testFindFilterRegistrations();
        assertEquals(emptyMap(), findFilterRegistrations(this.servletContext, OncePerRequestFilter.class));
    }

    @Test
    public void testFindServletRegistrations() {
        ServletRegistration.Dynamic servletRegistration = this.servletContext.addServlet(testServletName, testServletClass);
        Map<String, ? extends ServletRegistration> servletRegistrations = findServletRegistrations(this.servletContext, testServletClass);
        assertTrue(servletRegistrations.containsKey(testServletName));
        assertTrue(servletRegistrations.containsValue(servletRegistration));
    }

    @Test
    public void testFindServletRegistrationsOnEmptyServletContext() {
        assertSame(emptyMap(), findServletRegistrations(this.servletContext, testServletClass));
    }

    @Test
    public void testFindServletRegistrationsOnNotFound() {
        testFindServletRegistrations();
        assertEquals(emptyMap(), findServletRegistrations(this.servletContext, FrameworkServlet.class));
    }
}