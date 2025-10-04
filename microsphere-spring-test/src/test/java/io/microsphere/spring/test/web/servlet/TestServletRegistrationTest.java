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

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletSecurityElement;
import java.util.Set;

import static io.microsphere.collection.MapUtils.of;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletClass;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletClassName;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletName;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link TestServletRegistration} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletRegistration
 * @since 1.0.0
 */
public class TestServletRegistrationTest {

    private TestServletRegistration registration;

    @Before
    public void setUp() {
        TestServletContext servletContext = new TestServletContext();
        this.registration = (TestServletRegistration) servletContext.addServlet(testServletName, testServletClass);
    }

    @Test
    public void testSetLoadOnStartup() {
        registration.setLoadOnStartup(1);
        assertEquals(1, registration.getLoadOnStartup());
    }

    @Test
    public void testSetServletSecurity() {
        assertSame(emptySet(), registration.setServletSecurity(new ServletSecurityElement()));
    }

    @Test
    public void testSetMultipartConfig() {
        MultipartConfigElement multipartConfig = new MultipartConfigElement("/test");
        this.registration.setMultipartConfig(multipartConfig);
        assertSame(multipartConfig, this.registration.getMultipartConfig());
    }

    @Test
    public void testSetRunAsRole() {
        this.registration.setRunAsRole("admin");
        assertEquals("admin", this.registration.getRunAsRole());
    }

    @Test
    public void testSetAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
        this.registration.setAsyncSupported(true);
        assertTrue(this.registration.isAsyncSupported());
    }

    @Test
    public void testAddMapping() {
        String[] urlPatterns = ofArray("/a", "/b", "c");
        this.registration.addMapping(urlPatterns);
        assertArrayEquals(urlPatterns, this.registration.getMappings().toArray(EMPTY_STRING_ARRAY));
    }

    @Test
    public void testGetMappings() {
        assertTrue(this.registration.getMappings().isEmpty());
    }

    @Test
    public void testGetRunAsRole() {
        assertNull(this.registration.getRunAsRole());
    }

    @Test
    public void testGetName() {
        assertEquals(testServletName, this.registration.getName());
    }

    @Test
    public void testGetClassName() {
        assertEquals(testServletClassName, this.registration.getClassName());
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
    public void testGetServletSecurityElement() {
        assertNull(this.registration.getServletSecurityElement());
    }

    @Test
    public void testGetMultipartConfig() {
        assertNull(this.registration.getMultipartConfig());
    }

    @Test
    public void testGetServlet() {
        assertNotNull(this.registration.getServlet());
    }

    @Test
    public void testGetUrlPatterns() {
        assertTrue(this.registration.getUrlPatterns().isEmpty());
    }

    @Test
    public void testGetLoadOnStartup() {
        assertEquals(0, this.registration.getLoadOnStartup());
    }

    @Test
    public void testGetRoleName() {
        assertNull(this.registration.getRoleName());
    }

    @Test
    public void testIsAsyncSupported() {
        assertFalse(this.registration.isAsyncSupported());
    }
}