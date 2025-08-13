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

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;

/**
 * The utility class for Servlet Testing
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext
 * @since 1.0.0
 */
public abstract class ServletTestUtils {

    /**
     * Adds a test servlet to the provided {@link ServletContext}.
     * <p>
     * This method registers a {@link TestServlet} with the default servlet name
     * {@value TestServlet#DEFAULT_SERVLET_NAME} and maps it to the default URL pattern
     * {@value TestServlet#DEFAULT_SERVLET_URL_PATTERN}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ServletContext servletContext = ...; // Obtain the ServletContext
     * ServletTestUtils.addTestServlet(servletContext);
     * }</pre>
     *
     * @param servletContext the {@link ServletContext} to which the test servlet will be added.
     * @return the {@link ServletRegistration.Dynamic} for the added servlet.
     * @see TestServlet
     * @see ServletContext#addServlet(String, javax.servlet.Servlet)
     */
    public static ServletRegistration.Dynamic addTestServlet(ServletContext servletContext) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(DEFAULT_SERVLET_NAME, new TestServlet());
        servletRegistration.addMapping(DEFAULT_SERVLET_URL_PATTERN);
        return servletRegistration;
    }

    /**
     * Adds a test filter to the provided {@link ServletContext}.
     * <p>
     * This method registers a {@link TestFilter} with the default filter name
     * {@value TestFilter#DEFAULT_FILTER_NAME} and maps it to the default URL pattern
     * {@value TestFilter#DEFAULT_FILTER_URL_PATTERN}. Additionally, it adds a test servlet
     * to ensure the filter can be mapped to it.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     * ServletContext servletContext = ...; // Obtain the ServletContext
     * ServletTestUtils.addTestFilter(servletContext);
     * }</pre>
     *
     * @param servletContext the {@link ServletContext} to which the test filter will be added.
     * @return the {@link FilterRegistration.Dynamic} for the added filter.
     * @see TestFilter
     * @see ServletContext#addFilter(String, javax.servlet.Filter)
     */
    public static FilterRegistration.Dynamic addTestFilter(ServletContext servletContext) {
        ServletRegistration.Dynamic servletRegistration = servletContext.addServlet(DEFAULT_SERVLET_NAME, new TestServlet());
        servletRegistration.addMapping(DEFAULT_SERVLET_URL_PATTERN);

        FilterRegistration.Dynamic filterRegistration = servletContext.addFilter(DEFAULT_FILTER_NAME, new TestFilter());
        filterRegistration.addMappingForServletNames(of(REQUEST), true, DEFAULT_SERVLET_NAME);
        filterRegistration.addMappingForUrlPatterns(of(REQUEST), true, DEFAULT_FILTER_URL_PATTERN);
        return filterRegistration;
    }

    private ServletTestUtils() {
    }
}
