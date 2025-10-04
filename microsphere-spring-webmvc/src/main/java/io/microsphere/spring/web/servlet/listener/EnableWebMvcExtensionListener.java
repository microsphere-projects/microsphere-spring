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

package io.microsphere.spring.web.servlet.listener;

import io.microsphere.logging.Logger;
import io.microsphere.spring.web.util.RequestContextStrategy;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.FrameworkServlet;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpServletRequest;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.CollectionUtils.isNotEmpty;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static java.lang.String.valueOf;
import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;
import static org.springframework.util.StringUtils.uncapitalize;

/**
 * {@link ServletContainerInitializer} for {@link EnableWebMvcExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtension#requestContextStrategy()
 * @see ServletContainerInitializer
 * @since 1.0.0
 */
@HandlesTypes(EnableWebMvcExtension.class)
public class EnableWebMvcExtensionListener implements ServletContainerInitializer {

    private static final Logger logger = getLogger(EnableWebMvcExtensionListener.class);

    private static final Class<FrameworkServlet> FRAMEWORK_SERVLET_CLASS = FrameworkServlet.class;

    private static final Class<RequestContextFilter> REQUEST_CONTEXT_FILTER_CLASS = RequestContextFilter.class;

    /**
     * The class name of {@link DispatcherServlet}
     */
    private static final String DISPATCHER_SERVLET_CLASS_NAME = DispatcherServlet.class.getName();

    /**
     * The class name of {@link RequestContextFilter}
     */
    private static final String REQUEST_CONTEXT_FILTER_CLASS_NAME = REQUEST_CONTEXT_FILTER_CLASS.getName();

    /**
     * The name of {@link RequestContextFilter}
     */
    private static final String REQUEST_CONTEXT_FILTER_NAME = uncapitalize(REQUEST_CONTEXT_FILTER_CLASS.getSimpleName());

    /**
     * The dispatcher types of {@link RequestContextFilter}
     */
    private static final EnumSet<DispatcherType> REQUEST_CONTEXT_FILTER_DISPATCHER_TYPES = of(REQUEST);

    /**
     * The URL pattern of {@link RequestContextFilter}
     */
    private static final String REQUEST_CONTEXT_FILTER_URL_PATTERN = "/*";

    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext servletContext) {
        if (isNotEmpty(classes)) {
            processAnnotatedClasses(classes, servletContext);
        }
    }

    private void processAnnotatedClasses(Set<Class<?>> annotatedClasses, ServletContext servletContext) {
        for (Class<?> annotatedClass : annotatedClasses) {
            processAnnotatedClass(annotatedClass, servletContext);
        }
    }

    private void processAnnotatedClass(Class<?> annotatedClass, ServletContext servletContext) {
        EnableWebMvcExtension enableWebMvcExtension = annotatedClass.getAnnotation(EnableWebMvcExtension.class);
        processAnnotatedClass(enableWebMvcExtension, servletContext);
    }

    private void processAnnotatedClass(EnableWebMvcExtension enableWebMvcExtension, ServletContext servletContext) {
        processRequestContextStrategy(enableWebMvcExtension, servletContext);
    }

    private void processRequestContextStrategy(EnableWebMvcExtension enableWebMvcExtension, ServletContext servletContext) {
        RequestContextStrategy requestContextStrategy = enableWebMvcExtension.requestContextStrategy();
        boolean threadContextInheritable = false;
        switch (requestContextStrategy) {
            case DEFAULT:
                return;
            case THREAD_LOCAL:
                threadContextInheritable = false;
                break;
            case INHERITABLE_THREAD_LOCAL:
                threadContextInheritable = true;
                break;
        }
        processFrameworkServlet(servletContext, threadContextInheritable);
        processRequestContextFilter(servletContext, threadContextInheritable);
    }

    /**
     * Process {@link FrameworkServlet}
     *
     * @param servletContext           {@link ServletContext}
     * @param threadContextInheritable whether to expose the LocaleContext as inheritable
     *                                 for child threads (using an {@link InheritableThreadLocal})
     * @see FrameworkServlet#initContextHolders(HttpServletRequest, LocaleContext, RequestAttributes)
     * @see FrameworkServlet#threadContextInheritable
     */
    private void processFrameworkServlet(ServletContext servletContext, boolean threadContextInheritable) {
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        for (Map.Entry<String, ? extends ServletRegistration> entry : servletRegistrations.entrySet()) {
            ServletRegistration servletRegistration = entry.getValue();
            String servletClassName = servletRegistration.getClassName();
            if (isFrameworkServlet(servletClassName, servletContext)) {
                String name = "threadContextInheritable";
                String value = valueOf(threadContextInheritable);
                boolean set = servletRegistration.setInitParameter(name, value);
                logger.trace("Set ServletConfig '{}' of {} to be {} for {}", name, servletClassName, value, set);
            }
        }
    }

    boolean isFrameworkServlet(String servletClassName, ServletContext servletContext) {
        if (DISPATCHER_SERVLET_CLASS_NAME.equals(servletClassName)) { // the most case
            return true;
        } else {
            ClassLoader classLoader = servletContext.getClassLoader();
            Class<?> servletClass = resolveClass(servletClassName, classLoader);
            if (isAssignableFrom(FRAMEWORK_SERVLET_CLASS, servletClass)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process {@link RequestContextFilter}
     *
     * @param servletContext           {@link ServletContext}
     * @param threadContextInheritable whether to expose the LocaleContext as inheritable
     *                                 for child threads (using an {@link InheritableThreadLocal})
     * @see RequestContextFilter
     * @see RequestContextFilter#setThreadContextInheritable(boolean)
     */
    private void processRequestContextFilter(ServletContext servletContext, boolean threadContextInheritable) {
        if (hasRequestContextFilterRegistration(servletContext)) {
            return;
        }
        registerRequestContextFilter(servletContext, threadContextInheritable);
    }

    boolean hasRequestContextFilterRegistration(ServletContext servletContext) {
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            FilterRegistration filterRegistration = entry.getValue();
            String filterClassName = filterRegistration.getClassName();
            if (isRequestContextFilter(filterClassName)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRequestContextFilter(String filterClassName) {
        return REQUEST_CONTEXT_FILTER_CLASS_NAME.equals(filterClassName);
    }

    private void registerRequestContextFilter(ServletContext servletContext, boolean threadContextInheritable) {
        RequestContextFilter filter = new RequestContextFilter();
        filter.setThreadContextInheritable(threadContextInheritable);

        FilterRegistration filterRegistration = servletContext.addFilter(REQUEST_CONTEXT_FILTER_NAME, filter);
        filterRegistration.addMappingForUrlPatterns(REQUEST_CONTEXT_FILTER_DISPATCHER_TYPES, true, REQUEST_CONTEXT_FILTER_URL_PATTERN);
    }
}
