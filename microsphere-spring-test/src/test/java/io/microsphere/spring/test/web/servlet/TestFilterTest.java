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


import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockFilterConfig;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestFilter.FILTER_CLASS_NAME;
import static org.junit.jupiter.api.Assertions.assertSame;

;

/**
 * {@link TestFilter} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestFilter
 * @since 1.0.0
 */
public class TestFilterTest {

    private TestFilter testFilter;

    @BeforeEach
    void setUp() throws Exception {
        this.testFilter = new TestFilter();
    }

    @Test
    void testConstants() {
        assertSame("testFilter", DEFAULT_FILTER_NAME);
        assertSame("/testFilter", DEFAULT_FILTER_URL_PATTERN);
        assertSame("io.microsphere.spring.test.web.servlet.TestFilter", FILTER_CLASS_NAME);
    }

    @Test
    void testInit() throws ServletException {
        this.testFilter.init(new MockFilterConfig());
    }

    @Test
    void testDoFilter() throws ServletException, IOException {
        this.testFilter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());
    }

    @Test
    void testDestroy() {
        this.testFilter.destroy();
    }
}