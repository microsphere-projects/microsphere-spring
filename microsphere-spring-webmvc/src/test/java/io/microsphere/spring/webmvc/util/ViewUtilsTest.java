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

package io.microsphere.spring.webmvc.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.spring.web.util.WebScope.REQUEST;
import static io.microsphere.spring.webmvc.util.ViewUtils.getPathVariables;
import static io.microsphere.spring.webmvc.util.ViewUtils.getResponseStatus;
import static io.microsphere.spring.webmvc.util.ViewUtils.getSelectedContentType;
import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.servlet.View.PATH_VARIABLES;
import static org.springframework.web.servlet.View.RESPONSE_STATUS_ATTRIBUTE;
import static org.springframework.web.servlet.View.SELECTED_CONTENT_TYPE;

/**
 * {@link ViewUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ViewUtils
 * @since 1.0.0
 */
class ViewUtilsTest {

    private MockHttpServletRequest servletRequest;

    private RequestAttributes requestAttributes;

    @BeforeEach
    void setUp() {
        this.servletRequest = new MockHttpServletRequest();
        this.requestAttributes = new ServletWebRequest(this.servletRequest);
    }

    @Test
    void testGetResponseStatus() {
        assertNull(getResponseStatus(requestAttributes));
        REQUEST.setAttribute(requestAttributes, RESPONSE_STATUS_ATTRIBUTE, OK);
        assertSame(OK, getResponseStatus(requestAttributes));
    }

    @Test
    void testGetPathVariables() {
        assertNull(getPathVariables(requestAttributes));
        REQUEST.setAttribute(requestAttributes, PATH_VARIABLES, emptyMap());
        assertSame(emptyMap(), getPathVariables(requestAttributes));
    }

    @Test
    void testGetSelectedContentType() {
        assertNull(getSelectedContentType(requestAttributes));
        REQUEST.setAttribute(requestAttributes, SELECTED_CONTENT_TYPE, APPLICATION_JSON);
        assertSame(APPLICATION_JSON, getSelectedContentType(requestAttributes));
    }
}