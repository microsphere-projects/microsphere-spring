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


import org.junit.Before;
import org.junit.Test;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.EventListener;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * {@link TestServletContext} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletContext
 * @since 1.0.0
 */
public class TestServletContextTest {

    private static final String servletName = "testServlet";

    private static final Class<? extends Servlet> servletClass = TestServlet.class;

    private static final String servletClassName = servletClass.getName();

    private static final String filterName = "testFilter";

    private static final Class<? extends Filter> filterClass = TestFilter.class;

    private static final String filterClassName = filterClass.getName();

    private static final Class<? extends EventListener> listenerClass = TestServletContextListener.class;

    private static final String listenerClassName = listenerClass.getName();

    private TestServletContext servletContext;

    @Before
    public void setUp() throws Exception {
        this.servletContext = new TestServletContext();
    }

    @Test
    public void testAddServletWithNameAndClassName() {
        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, servletClassName);
        assertServletRegistration(registration);
    }

    @Test
    public void testAddServletWithNameAndInstance() throws ServletException {
        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, servletContext.createServlet(servletClass));
        assertServletRegistration(registration);
    }

    @Test
    public void testAddServletWithNameAndClass() {
        ServletRegistration.Dynamic registration = servletContext.addServlet(servletName, servletClass);
        assertServletRegistration(registration);
    }

    @Test
    public void testCreateServlet() throws ServletException {
        assertNotNull(servletContext.createServlet(servletClass));
    }


    @Test
    public void testGetServletRegistration() {
        testAddServletWithNameAndClassName();
        assertServletRegistration(servletContext.getServletRegistration(servletName));
    }

    @Test
    public void testGetServletRegistrations() {
        testAddServletWithNameAndClassName();
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        assertServletRegistration(servletRegistrations.get(servletName));
    }

    @Test
    public void testAddFilterWithNameAndClassName() {
        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filterClassName);
        assertFilterRegistration(registration);
    }

    @Test
    public void testTestAddFilterWithNameAndInstance() throws ServletException {
        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, servletContext.createFilter(filterClass));
        assertFilterRegistration(registration);
    }

    @Test
    public void testTestAddFilterWithNameAndClass() {
        FilterRegistration.Dynamic registration = servletContext.addFilter(filterName, filterClass);
        assertFilterRegistration(registration);
    }

    @Test
    public void testCreateFilter() throws ServletException {
        assertNotNull(servletContext.createFilter(filterClass));
    }

    @Test
    public void testGetFilterRegistration() {
        testAddFilterWithNameAndClassName();
        assertFilterRegistration(servletContext.getFilterRegistration(filterName));
    }

    @Test
    public void testGetFilterRegistrations() {
        testAddFilterWithNameAndClassName();
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        assertFilterRegistration(filterRegistrations.get(filterName));
    }

    @Test
    public void testAddListenerWithClass() {
        servletContext.addListener(listenerClass);
    }

    @Test
    public void testAddListenerWithClassName() {
        servletContext.addListener(listenerClassName);
    }

    @Test
    public void testAddListenerWithInstance() throws ServletException {
        servletContext.addListener(servletContext.createListener(listenerClass));
    }

    @Test
    public void testCreateListener() throws ServletException {
        assertNotNull(servletContext.createListener(listenerClass));
    }

    @Test
    public void testCreateInstance() {
        assertThrows(RuntimeException.class, () -> servletContext.createInstance(EventListener.class.getName()));
    }

    @Test
    public void testLoadClass() {
        assertSame(servletClass, servletContext.loadClass(servletClassName));
        assertSame(filterClass, servletContext.loadClass(filterClassName));
        assertSame(listenerClass, servletContext.loadClass(listenerClassName));
    }

    void assertServletRegistration(ServletRegistration registration) {
        assertEquals(servletName, registration.getName());
        assertEquals(servletClassName, registration.getClassName());
        TestServletRegistration testServletRegistration = (TestServletRegistration) registration;
        assertNotNull(testServletRegistration.getServlet());
    }

    void assertFilterRegistration(FilterRegistration registration) {
        assertEquals(filterName, registration.getName());
        assertEquals(filterClassName, registration.getClassName());
        TestFilterRegistration testFilterRegistration = (TestFilterRegistration) registration;
        assertNotNull(testFilterRegistration.getFilter());
    }
}