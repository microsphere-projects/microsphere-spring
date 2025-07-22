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
package io.microsphere.spring.web.servlet;

import io.microsphere.annotation.Nullable;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Test {@link ServletContext}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class TestServletContext extends MockServletContext {

    private Map<String, ServletRegistration.Dynamic> servletRegistrations = new HashMap<>();

    private Map<String, FilterRegistration.Dynamic> filterRegistrations = new HashMap<>();

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        ServletRegistration.Dynamic registration = new TestServletRegistration(servletName, className);
        servletRegistrations.put(servletName, registration);
        return registration;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName());
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    @Override
    @Nullable
    public ServletRegistration getServletRegistration(String servletName) {
        return servletRegistrations.get(servletName);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        return unmodifiableMap(this.servletRegistrations);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        FilterRegistration.Dynamic filterRegistration = new TestFilterRegistration(filterName, className);
        filterRegistrations.put(filterName, filterRegistration);
        return filterRegistration;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass());
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filterRegistrations.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return unmodifiableMap(filterRegistrations);
    }
}
