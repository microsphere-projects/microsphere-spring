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

package io.microsphere.spring.test.util;


import io.microsphere.spring.test.web.servlet.TestFilter;
import io.microsphere.spring.test.web.servlet.TestServlet;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletRegistration;

import static io.microsphere.spring.test.util.ServletTestUtils.addTestFilter;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * {@link ServletTestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletTestUtils
 * @since 1.0.0
 */
public class ServletTestUtilsTest {

    private TestServletContext servletContext;

    @Before
    public void setUp() {
        this.servletContext = new TestServletContext();
    }

    @Test
    public void testAddTestServlet() {
        ServletRegistration.Dynamic servletRegistration = addTestServlet(this.servletContext);
        assertEquals(DEFAULT_SERVLET_NAME, servletRegistration.getName());
        assertArrayEquals(ofArray(DEFAULT_SERVLET_URL_PATTERN), servletRegistration.getMappings().toArray(EMPTY_STRING_ARRAY));
        assertEquals(TestServlet.class.getName(), servletRegistration.getClassName());
        assertEquals(emptyMap(), servletRegistration.getInitParameters());
    }

    @Test
    public void testAddTestFilter() {
        FilterRegistration.Dynamic filterRegistration = addTestFilter(this.servletContext);
        assertEquals(DEFAULT_FILTER_NAME, filterRegistration.getName());
        assertArrayEquals(ofArray(DEFAULT_FILTER_URL_PATTERN), filterRegistration.getUrlPatternMappings().toArray(EMPTY_STRING_ARRAY));
        assertArrayEquals(ofArray(DEFAULT_SERVLET_NAME), filterRegistration.getServletNameMappings().toArray(EMPTY_STRING_ARRAY));
        assertEquals(TestFilter.class.getName(), filterRegistration.getClassName());
        assertEquals(emptyMap(), filterRegistration.getInitParameters());
    }
}