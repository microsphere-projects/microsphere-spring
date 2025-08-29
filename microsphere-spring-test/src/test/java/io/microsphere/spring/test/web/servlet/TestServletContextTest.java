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


import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.EventListener;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * {@link TestServletContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletContext
 * @since 1.0.0
 */
public class TestServletContextTest {

    static final String testServletName = "testServlet";

    static final Class<? extends Servlet> testServletClass = TestServlet.class;

    static final String testServletClassName = testServletClass.getName();

    static final String testFilterName = "testFilter";

    static final Class<? extends Filter> testFilterClass = TestFilter.class;

    static final String testFilterClassName = testFilterClass.getName();

    static final Class<? extends EventListener> testListenerClass = TestServletContextListener.class;

    static final String testListenerClassName = testListenerClass.getName();

    private TestServletContext servletContext;

    @BeforeEach
    void setUp() throws Exception {
        this.servletContext = new TestServletContext();
    }

    @Test
    void testAddServletWithNameAndClassName() {
        ServletRegistration.Dynamic registration = servletContext.addServlet(testServletName, testServletClassName);
        assertServletRegistration(registration);
    }

    @Test
    void testAddServletWithNameAndInstance() throws ServletException {
        ServletRegistration.Dynamic registration = servletContext.addServlet(testServletName, servletContext.createServlet(testServletClass));
        assertServletRegistration(registration);
    }

    @Test
    void testAddServletWithNameAndClass() {
        ServletRegistration.Dynamic registration = servletContext.addServlet(testServletName, testServletClass);
        assertServletRegistration(registration);
    }

    @Test
    void testCreateServlet() throws ServletException {
        assertNotNull(servletContext.createServlet(testServletClass));
    }


    @Test
    void testGetServletRegistration() {
        testAddServletWithNameAndClassName();
        assertServletRegistration(servletContext.getServletRegistration(testServletName));
    }

    @Test
    void testGetServletRegistrations() {
        testAddServletWithNameAndClassName();
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        assertServletRegistration(servletRegistrations.get(testServletName));
    }

    @Test
    void testAddFilterWithNameAndClassName() {
        FilterRegistration.Dynamic registration = servletContext.addFilter(testFilterName, testFilterClassName);
        assertFilterRegistration(registration);
    }

    @Test
    void testTestAddFilterWithNameAndInstance() throws ServletException {
        FilterRegistration.Dynamic registration = servletContext.addFilter(testFilterName, servletContext.createFilter(testFilterClass));
        assertFilterRegistration(registration);
    }

    @Test
    void testTestAddFilterWithNameAndClass() {
        FilterRegistration.Dynamic registration = servletContext.addFilter(testFilterName, testFilterClass);
        assertFilterRegistration(registration);
    }

    @Test
    void testCreateFilter() throws ServletException {
        assertNotNull(servletContext.createFilter(testFilterClass));
    }

    @Test
    void testGetFilterRegistration() {
        testAddFilterWithNameAndClassName();
        assertFilterRegistration(servletContext.getFilterRegistration(testFilterName));
    }

    @Test
    void testGetFilterRegistrations() {
        testAddFilterWithNameAndClassName();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        assertFilterRegistration(filterRegistrations.get(testFilterName));
    }

    @Test
    void testAddListenerWithClass() {
        servletContext.addListener(testListenerClass);
    }

    @Test
    void testAddListenerWithClassName() {
        servletContext.addListener(testListenerClassName);
    }

    @Test
    void testAddListenerWithInstance() throws ServletException {
        servletContext.addListener(servletContext.createListener(testListenerClass));
    }

    @Test
    void testCreateListener() throws ServletException {
        assertNotNull(servletContext.createListener(testListenerClass));
    }

    @Test
    void testCreateInstance() {
        assertThrows(RuntimeException.class, () -> servletContext.createInstance(EventListener.class.getName()));
    }

    @Test
    void testLoadClass() {
        assertSame(testServletClass, servletContext.loadClass(testServletClassName));
        assertSame(testFilterClass, servletContext.loadClass(testFilterClassName));
        assertSame(testListenerClass, servletContext.loadClass(testListenerClassName));
    }

    void assertServletRegistration(ServletRegistration registration) {
        assertEquals(testServletName, registration.getName());
        assertEquals(testServletClassName, registration.getClassName());
        TestServletRegistration testServletRegistration = (TestServletRegistration) registration;
        assertNotNull(testServletRegistration.getServlet());
    }

    void assertFilterRegistration(FilterRegistration registration) {
        assertEquals(testFilterName, registration.getName());
        assertEquals(testFilterClassName, registration.getClassName());
        TestFilterRegistration testFilterRegistration = (TestFilterRegistration) registration;
        assertNotNull(testFilterRegistration.getFilter());
    }
}