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


import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletSecurityElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletClass;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletClassName;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletName;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

;

/**
 * {@link TestServletRegistration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletRegistration
 * @since 1.0.0
 */
public class TestServletRegistrationTest {

    private TestServletRegistration registration;

    @BeforeEach
    void setUp() throws Exception {
        TestServletContext servletContext = new TestServletContext();
        this.registration = (TestServletRegistration) servletContext.addServlet(testServletName, testServletClass);
    }

    @Test
    void testSetLoadOnStartup() {
        registration.setLoadOnStartup(1);
        assertEquals(1, registration.getLoadOnStartup());
    }

    @Test
    void testSetServletSecurity() {
        assertSame(emptySet(), registration.setServletSecurity(new ServletSecurityElement()));
    }

    @Test
    void testSetMultipartConfig() {
        MultipartConfigElement multipartConfig = new MultipartConfigElement("/test");
        this.registration.setMultipartConfig(multipartConfig);
        assertSame(multipartConfig, this.registration.getMultipartConfig());
    }

    @Test
    void testSetRunAsRole() {
        this.registration.setRunAsRole("admin");
        assertEquals("admin", this.registration.getRunAsRole());
    }

    @Test
    void testSetAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
        this.registration.setAsyncSupported(true);
        assertTrue(this.registration.isAsyncSupported());
    }

    @Test
    void testAddMapping() {
        String[] urlPatterns = ofArray("/a", "/b", "c");
        this.registration.addMapping(urlPatterns);
        assertArrayEquals(urlPatterns, this.registration.getMappings().toArray(EMPTY_STRING_ARRAY));
    }

    @Test
    void testGetMappings() {
    }

    @Test
    void testGetRunAsRole() {
    }

    @Test
    void testGetName() {
        assertEquals(testServletName, this.registration.getName());
    }

    @Test
    void testGetClassName() {
        assertEquals(testServletClassName, this.registration.getClassName());
    }

    @Test
    void testSetInitParameter() {
        assertTrue(this.registration.setInitParameter("paramName", "paramValue"));
        assertFalse(this.registration.setInitParameter("paramName", "paramValue"));
        assertEquals("paramValue", this.registration.getInitParameter("paramName"));
    }

    @Test
    void testGetInitParameter() {
        assertNull(this.registration.getInitParameter("paramName"));
    }

    @Test
    void testSetInitParameters() {
        Set<String> parameterNames = this.registration.setInitParameters(of("paramName", "paramValue"));
        assertTrue(parameterNames.contains("paramName"));
        assertEquals("paramValue", this.registration.getInitParameter("paramName"));
    }

    @Test
    void testGetInitParameters() {
        assertEquals(0, this.registration.getInitParameters().size());
    }

    @Test
    void testGetServletSecurityElement() {
        assertNull(this.registration.getServletSecurityElement());
    }

    @Test
    void testGetMultipartConfig() {
        assertNull(this.registration.getMultipartConfig());
    }

    @Test
    void testGetServlet() {
        assertNotNull(this.registration.getServlet());
    }

    @Test
    void testGetUrlPatterns() {
        assertTrue(this.registration.getUrlPatterns().isEmpty());
    }

    @Test
    void testGetLoadOnStartup() {
        assertEquals(0, this.registration.getLoadOnStartup());
    }

    @Test
    void testGetRoleName() {
        assertNull(this.registration.getRoleName());
    }

    @Test
    void testIsAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
    }
}