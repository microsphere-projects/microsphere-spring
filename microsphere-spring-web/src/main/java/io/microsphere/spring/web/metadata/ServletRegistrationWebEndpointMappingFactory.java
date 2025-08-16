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
package io.microsphere.spring.web.metadata;

import org.springframework.http.HttpMethod;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Maps.ofMap;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static io.microsphere.util.ClassUtils.isAssignableFrom;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.http.HttpMethod.OPTIONS;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.http.HttpMethod.TRACE;

/**
 * {@link WebEndpointMappingFactory} from {@link ServletRegistration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext#getServletRegistrations()
 * @see ServletRegistration
 * @since 1.0.0
 */
public class ServletRegistrationWebEndpointMappingFactory extends RegistrationWebEndpointMappingFactory<ServletRegistration> {

    /**
     * Key : The method names of {@link HttpServlet}
     * Value : The name of {@link HttpMethod}
     */
    private static final Map<String, String> methodNamesToHttpMethods = ofMap(
            "doGet", GET.name(),
            "doPost", POST.name(),
            "doPut", PUT.name(),
            "doDelete", DELETE.name(),
            "doHead", HEAD.name(),
            "doOptions", OPTIONS.name(),
            "doTrace", TRACE.name()
    );

    public ServletRegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        super(servletContext);
    }

    @Override
    protected Collection<String> getMethods(ServletRegistration registration) {
        String className = registration.getClassName();
        ServletContext servletContext = this.servletContext;
        ClassLoader classLoader = servletContext.getClassLoader();
        Class<?> servletClass = loadClass(classLoader, className);
        return getMethods(servletClass);
    }

    protected Collection<String> getMethods(Class<?> servletClass) {
        if (isAssignableFrom(HttpServlet.class, servletClass)) {
            return getMethodsFromHttpServlet(servletClass);
        } else {
            return ALL_HTTP_METHODS;
        }
    }

    protected Collection<String> getMethodsFromHttpServlet(Class<?> servletClass) {
        Collection<String> methods = newLinkedList();
        for (Entry<String, String> entry : methodNamesToHttpMethods.entrySet()) {
            String methodName = entry.getKey();
            Method method = findMethod(servletClass, methodName, HttpServletRequest.class, HttpServletResponse.class);
            if (method.getDeclaringClass() == servletClass) {
                methods.add(entry.getValue());
            }
        }
        if (methods.isEmpty()) {
            methods = ALL_HTTP_METHODS;
        }
        return methods;
    }

    @Override
    protected ServletRegistration getRegistration(String name, ServletContext servletContext) {
        return servletContext.getServletRegistration(name);
    }

    @Override
    protected Collection<String> getPatterns(ServletRegistration registration) {
        return registration.getMappings();
    }
}
