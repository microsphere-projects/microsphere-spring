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

import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletClass;
import static io.microsphere.spring.test.web.servlet.TestServletContextTest.testServletName;

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
    public void setUp() throws Exception {
        TestServletContext servletContext = new TestServletContext();
        this.registration = (TestServletRegistration) servletContext.addServlet(testServletName, testServletClass);
    }

    @Test
    public void testSetLoadOnStartup() {
    }

    @Test
    public void testSetServletSecurity() {
    }

    @Test
    public void testSetMultipartConfig() {
    }

    @Test
    public void testSetRunAsRole() {
    }

    @Test
    public void testSetAsyncSupported() {
    }

    @Test
    public void testAddMapping() {
    }

    @Test
    public void testGetMappings() {
    }

    @Test
    public void testGetRunAsRole() {
    }

    @Test
    public void testGetName() {
    }

    @Test
    public void testGetClassName() {
    }

    @Test
    public void testSetInitParameter() {
    }

    @Test
    public void testGetInitParameter() {
    }

    @Test
    public void testSetInitParameters() {
    }

    @Test
    public void testGetInitParameters() {
    }

    @Test
    public void testGetServletSecurityElement() {
    }

    @Test
    public void testGetMultipartConfig() {
    }

    @Test
    public void testGetServletName() {
    }

    @Test
    public void testGetServletClassName() {
    }

    @Test
    public void testGetServlet() {
    }

    @Test
    public void testGetUrlPatterns() {
    }

    @Test
    public void testGetLoadOnStartup() {
    }

    @Test
    public void testGetRoleName() {
    }

    @Test
    public void testIsAsyncSupported() {
    }
}