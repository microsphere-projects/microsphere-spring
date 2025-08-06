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

import io.microsphere.annotation.Nullable;
import io.microsphere.util.ClassLoaderUtils;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mock.web.MockRequestDispatcher;
import org.springframework.mock.web.MockServletContext;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static java.util.Collections.unmodifiableMap;
import static org.springframework.beans.BeanUtils.instantiateClass;

/**
 * Test {@link ServletContext} based on {@link MockServletContext}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MockServletContext
 * @since 1.0.0
 */
public class TestServletContext extends MockServletContext {

    private final Map<String, TestServletRegistration> servletRegistrations = newLinkedHashMap();

    private final Map<String, TestFilterRegistration> filterRegistrations = newLinkedHashMap();

    private final List<EventListener> listeners = newLinkedList();

    /**
     * Create a new {@code TestServletContext}, using no base path and a
     * {@link DefaultResourceLoader} (i.e. the classpath root as WAR root).
     *
     * @see DefaultResourceLoader
     */
    public TestServletContext() {
        this("");
    }

    /**
     * Create a new {@code TestServletContext}, using a {@link DefaultResourceLoader}.
     *
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @see DefaultResourceLoader
     */
    public TestServletContext(String resourceBasePath) {
        this(resourceBasePath, null);
    }

    /**
     * Create a new {@code TestServletContext} using the supplied resource base
     * path and resource loader.
     * <p>Registers a {@link MockRequestDispatcher} for the Servlet named
     * {@literal 'default'}.
     *
     * @param resourceBasePath the root directory of the WAR (should not end with a slash)
     * @param resourceLoader   the ResourceLoader to use (or null for the default)
     * @see #registerNamedDispatcher
     */
    public TestServletContext(String resourceBasePath, ResourceLoader resourceLoader) {
        super(resourceBasePath, resourceLoader);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        return addServlet(servletName, className, null);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        return addServlet(servletName, servlet.getClass().getName(), servlet);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        return addServlet(servletName, servletClass.getName());
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> c) throws ServletException {
        return createInstance(c);
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
        return addFilter(filterName, className, null);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        return addFilter(filterName, filter.getClass().getName(), filter);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        return addFilter(filterName, filterClass.getName());
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> c) throws ServletException {
        return createInstance(c);
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        return filterRegistrations.get(filterName);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        return unmodifiableMap(filterRegistrations);
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        EventListener listener = execute(() -> instantiateClass(listenerClass));
        addListener(listener);
    }

    @Override
    public void addListener(String className) {
        EventListener listener = createInstance(className);
        addListener(listener);
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        listeners.add(t);
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> c) throws ServletException {
        return createInstance(c);
    }

    protected ServletRegistration.Dynamic addServlet(String servletName, String servletClassName, @Nullable Servlet servlet) {
        Servlet actualServlet = servlet == null ? createInstance(servletClassName) : servlet;
        TestServletRegistration registration = new TestServletRegistration(servletName, servletClassName, actualServlet);
        servletRegistrations.put(servletName, registration);
        return registration;
    }

    protected FilterRegistration.Dynamic addFilter(String filterName, String filterClassName, @Nullable Filter filter) {
        Filter actualFilter = filter == null ? createInstance(filterClassName) : filter;
        TestFilterRegistration filterRegistration = new TestFilterRegistration(filterName, filterClassName, actualFilter);
        filterRegistrations.put(filterName, filterRegistration);
        return filterRegistration;
    }

    protected <T> T createInstance(String className) {
        Class<T> klass = loadClass(className);
        return execute(() -> createInstance(klass));
    }

    protected <T> T createInstance(Class<T> c) throws ServletException {
        T instance = null;
        try {
            instance = instantiateClass(c);
        } catch (Throwable e) {
            throw new ServletException(e);
        }
        return instance;
    }

    protected <T> Class<T> loadClass(String className) {
        ClassLoader classLoader = getClassLoader();
        return (Class<T>) ClassLoaderUtils.loadClass(classLoader, className);
    }
}
