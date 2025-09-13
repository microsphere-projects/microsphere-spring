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

import jakarta.servlet.Servlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * {@link Servlet} for Testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestServlet extends HttpServlet {

    /**
     * Default Servlet Name : "testServlet"
     */
    public static final String DEFAULT_SERVLET_NAME = "testServlet";

    /**
     * Servlet Class
     */
    public static final Class<? extends Servlet> SERVLET_CLASS = TestServlet.class;

    /**
     * Servlet Class Name
     */
    public static final String SERVLET_CLASS_NAME = SERVLET_CLASS.getName();

    /**
     * Default URL Pattern : "/testServlet"
     */
    public static final String DEFAULT_SERVLET_URL_PATTERN = "/" + DEFAULT_SERVLET_NAME;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doHead(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doService(request, response);
    }

    protected void doService(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().write("Hello World!");
    }
}
