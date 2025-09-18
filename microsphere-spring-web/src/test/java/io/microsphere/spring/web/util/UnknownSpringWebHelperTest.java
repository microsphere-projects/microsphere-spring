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

package io.microsphere.spring.web.util;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;

import static io.microsphere.spring.web.util.SpringWebType.UNKNOWN;
import static io.microsphere.spring.web.util.UnknownSpringWebHelper.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link UnknownSpringWebHelper} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see UnknownSpringWebHelper
 * @since 1.0.0
 */
class UnknownSpringWebHelperTest {

    private MockHttpServletRequest servletRequest;

    private ServletWebRequest request;

    private UnknownSpringWebHelper helper;

    @BeforeEach
    void setUp() {
        this.servletRequest = new MockHttpServletRequest();
        this.request = new ServletWebRequest(this.servletRequest);
        this.helper = INSTANCE;
    }

    @Test
    void testGetBestMatchingHandler() {
        assertNull(helper.getBestMatchingHandler(this.request));
        assertNull(helper.getBestMatchingHandler(null));
    }

    @Test
    void testGetPathWithinHandlerMapping() {
        assertNull(helper.getPathWithinHandlerMapping(this.request));
        assertNull(helper.getPathWithinHandlerMapping(null));
    }

    @Test
    void testGetBestMatchingPattern() {
        assertNull(helper.getBestMatchingPattern(this.request));
        assertNull(helper.getBestMatchingPattern(null));
    }

    @Test
    void testGetUriTemplateVariables() {
        assertNull(helper.getUriTemplateVariables(this.request));
        assertNull(helper.getUriTemplateVariables(null));
    }

    @Test
    void testGetProducibleMediaTypes() {
        assertNull(helper.getProducibleMediaTypes(this.request));
        assertNull(helper.getProducibleMediaTypes(null));
    }

    @Test
    void testGetType() {
        assertSame(UNKNOWN, helper.getType());
    }
}