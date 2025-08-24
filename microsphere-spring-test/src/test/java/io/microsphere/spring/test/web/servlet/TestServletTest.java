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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.SERVLET_CLASS_NAME;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

/**
 * {@link TestServlet} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServlet
 * @since 1.0.0
 */
public class TestServletTest {

    private MockHttpServletRequest request;

    private MockHttpServletResponse response;

    private TestServlet testServlet;

    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.testServlet = new TestServlet();
    }

    @Test
    public void testConstants() {
        assertSame("testServlet", DEFAULT_SERVLET_NAME);
        assertSame("io.microsphere.spring.test.web.servlet.TestServlet", SERVLET_CLASS_NAME);
        assertSame("/testServlet", DEFAULT_SERVLET_URL_PATTERN);
    }

    @Test
    public void testDoGet() throws ServletException, IOException {
        this.testServlet.doGet(this.request, this.response);
        assertResponse();
    }

    @Test
    public void testDoHead() throws ServletException, IOException {
        this.testServlet.doHead(this.request, this.response);
        assertResponse();
    }

    @Test
    public void testDoPost() throws ServletException, IOException {
        this.testServlet.doPost(this.request, this.response);
        assertResponse();
    }

    @Test
    public void testDoPut() throws ServletException, IOException {
        this.testServlet.doPut(this.request, this.response);
        assertResponse();
    }

    @Test
    public void testDoDelete() throws ServletException, IOException {
        this.testServlet.doDelete(this.request, this.response);
        assertResponse();
    }

    @Test
    public void testDoService() throws ServletException, IOException {
        this.testServlet.doService(this.request, this.response);
        assertResponse();
    }

    private void assertResponse() throws UnsupportedEncodingException {
        assertEquals(SC_OK, response.getStatus());
        assertEquals("Hello World!", this.response.getContentAsString());
    }
}