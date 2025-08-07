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

package io.microsphere.spring.web.servlet.filter;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

import static io.microsphere.spring.web.servlet.filter.ContentCachingFilter.getResponseContentAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link ContentCachingFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ContentCachingFilter
 * @since 1.0.0
 */
public class ContentCachingFilterTest {

    private HttpServletRequest request;

    private HttpServletResponse response;

    private FilterChain filterChain;

    private ContentCachingFilter filter;

    @BeforeEach
    void setUp() {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();
        this.filterChain = new MockFilterChain();
        this.filter = new ContentCachingFilter();
    }

    @Test
    void testDoFilterInternal() throws ServletException, IOException {
        this.filter.doFilterInternal(request, response, filterChain);
    }

    @Test
    void testGetResponseContentAsString() throws IOException {
        assertNull(getResponseContentAsString(request, response));

        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        assertNull(getResponseContentAsString(request, responseWrapper));

        String content = "Hello,World";
        responseWrapper.getWriter().println(content);

        assertEquals(content, getResponseContentAsString(request, responseWrapper));
        // hits cache
        assertEquals(content, getResponseContentAsString(request, responseWrapper));
    }

    @Test
    void testGetResponseContentAsStringWithUnknownCharsetEncoding() throws IOException {
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        String content = "Hello,World";
        responseWrapper.getWriter().println(content);
        responseWrapper.setCharacterEncoding("Unknown");

        assertNull(getResponseContentAsString(request, responseWrapper));
    }
}