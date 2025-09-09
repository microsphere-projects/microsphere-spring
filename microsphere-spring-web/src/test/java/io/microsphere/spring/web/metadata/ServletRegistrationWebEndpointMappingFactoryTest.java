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

package io.microsphere.spring.web.metadata;


import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import java.util.Collection;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.SERVLET_CLASS;
import static io.microsphere.spring.test.web.servlet.TestServlet.SERVLET_CLASS_NAME;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link ServletRegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletRegistrationWebEndpointMappingFactory
 * @since 1.0.0
 */
public class ServletRegistrationWebEndpointMappingFactoryTest {

    private ServletContext servletContext;

    private ServletRegistrationWebEndpointMappingFactory factory;

    static class MyHttpServlet extends HttpServlet {
    }

    @Before
    public void setUp() {
        this.servletContext = new TestServletContext();
        this.factory = new ServletRegistrationWebEndpointMappingFactory(this.servletContext);
        addTestServlet(this.servletContext);
    }

    @Test
    public void testSupports() {
        assertTrue(factory.supports(DEFAULT_SERVLET_NAME));
        assertFalse(factory.supports(DEFAULT_FILTER_NAME));
    }

    @Test
    public void testGetMethods() {
        ServletRegistration servletRegistration = factory.getRegistration(DEFAULT_SERVLET_NAME, servletContext);
        Collection<String> methods = this.factory.getMethods(servletRegistration);
        assertMethodsOfTestServlet(methods);
    }

    @Test
    public void testGetMethodsWithTestServletClass() {
        Collection<String> methods = this.factory.getMethods(SERVLET_CLASS);
        assertMethodsOfTestServlet(methods);
    }

    @Test
    public void testGetMethodsWithServletClass() {
        Collection<String> methods = this.factory.getMethods(Servlet.class);
        assertSame(ALL_HTTP_METHODS, methods);
    }

    @Test
    public void testGetMethodsWithMyHttpServletClass() {
        Collection<String> methods = this.factory.getMethods(MyHttpServlet.class);
        assertSame(ALL_HTTP_METHODS, methods);
    }

    @Test
    public void testGetRegistration() {
        ServletRegistration servletRegistration = factory.getRegistration(DEFAULT_SERVLET_NAME, servletContext);
        assertNotNull(servletRegistration);
        assertEquals(DEFAULT_SERVLET_NAME, servletRegistration.getName());
        assertEquals(SERVLET_CLASS_NAME, servletRegistration.getClassName());
        assertArrayEquals(ofArray(DEFAULT_SERVLET_URL_PATTERN), servletRegistration.getMappings().toArray(EMPTY_STRING_ARRAY));
    }

    @Test
    public void testGetPatterns() {
        ServletRegistration servletRegistration = factory.getRegistration(DEFAULT_SERVLET_NAME, servletContext);
        assertEquals(newLinkedHashSet(DEFAULT_SERVLET_URL_PATTERN), newLinkedHashSet(factory.getPatterns(servletRegistration)));
    }

    void assertMethodsOfTestServlet(Collection<String> methods) {
        assertTrue(methods.contains("GET"));
        assertTrue(methods.contains("HEAD"));
        assertTrue(methods.contains("POST"));
        assertTrue(methods.contains("PUT"));
        assertTrue(methods.contains("DELETE"));
    }
}