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
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collection;

import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.SERVLET_CLASS;
import static io.microsphere.spring.test.web.servlet.TestServlet.SERVLET_CLASS_NAME;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static io.microsphere.util.ArrayUtils.ofArray;
import static io.microsphere.util.StringUtils.EMPTY_STRING_ARRAY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link ServletRegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletRegistrationWebEndpointMappingFactory
 * @since 1.0.0
 */
class ServletRegistrationWebEndpointMappingFactoryTest {

    private ServletContext servletContext;

    private ServletRegistrationWebEndpointMappingFactory factory;

    static class MyHttpServlet extends HttpServlet {

        @Override
        protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        this.servletContext = new TestServletContext();
        this.factory = new ServletRegistrationWebEndpointMappingFactory(this.servletContext);
        addTestServlet(this.servletContext);
    }

    @Test
    void testGetMethods() {
        ServletRegistration servletRegistration = factory.getRegistration(DEFAULT_SERVLET_NAME, servletContext);
        Collection<String> methods = this.factory.getMethods(servletRegistration);
        assertMethodsOfTestServlet(methods);
    }

    @Test
    void testGetMethodsWithTestServletClass() {
        Collection<String> methods = this.factory.getMethods(SERVLET_CLASS);
        assertMethodsOfTestServlet(methods);
    }

    @Test
    void testGetMethodsWithServletClass() {
        Collection<String> methods = this.factory.getMethods(Servlet.class);
        assertSame(ALL_HTTP_METHODS, methods);
    }

    @Test
    void testGetMethodsWithMyHttpServletClass() {
        Collection<String> methods = this.factory.getMethods(MyHttpServlet.class);
        assertSame(ALL_HTTP_METHODS, methods);
    }

    @Test
    void testGetRegistration() {
        ServletRegistration servletRegistration = factory.getRegistration(DEFAULT_SERVLET_NAME, servletContext);
        assertNotNull(servletRegistration);
        assertEquals(DEFAULT_SERVLET_NAME, servletRegistration.getName());
        assertEquals(SERVLET_CLASS_NAME, servletRegistration.getClassName());
        assertArrayEquals(ofArray(DEFAULT_SERVLET_URL_PATTERN), servletRegistration.getMappings().toArray(EMPTY_STRING_ARRAY));
    }

    @Test
    void testGetPatterns() {
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