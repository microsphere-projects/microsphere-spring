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

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.SetUtils.newLinkedHashSet;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;

/**
 * {@link WebEndpointMappingFactory} from {@link FilterRegistration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext#getFilterRegistrations()
 * @see FilterRegistration
 * @since 1.0.0
 */
public class FilterRegistrationWebEndpointMappingFactory extends RegistrationWebEndpointMappingFactory<FilterRegistration> {

    private final ServletRegistrationWebEndpointMappingFactory servletRegistrationWebEndpointMappingFactory;

    public FilterRegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        super(servletContext);
        this.servletRegistrationWebEndpointMappingFactory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
    }

    @Override
    protected Collection<String> getMethods(FilterRegistration registration) {
        Set<String> allMethods = newLinkedHashSet();

        // Add all HTTP methods when the url patterns are mapped.
        Collection<String> urlPatternMappings = registration.getUrlPatternMappings();
        if (!urlPatternMappings.isEmpty()) {
            allMethods.addAll(ALL_HTTP_METHODS);
        }

        // Add the methods from the ServletRegistration
        doInServletRegistration(registration, servletRegistration -> {
            Collection<String> methods = this.servletRegistrationWebEndpointMappingFactory.getMethods(servletRegistration);
            allMethods.addAll(methods);
        });
        return allMethods;
    }

    @Override
    protected FilterRegistration getRegistration(String name, ServletContext servletContext) {
        return servletContext.getFilterRegistration(name);
    }

    @Override
    protected Collection<String> getPatterns(FilterRegistration registration) {
        Collection<String> patterns = newLinkedList();
        // Add the URL patterns directly mapped to the Filter
        patterns.addAll(registration.getUrlPatternMappings());
        // Add the URL patterns mapped to the Servlet(s) which are associated with the Filter
        ServletContext servletContext = this.servletContext;
        ServletRegistrationWebEndpointMappingFactory factory = this.servletRegistrationWebEndpointMappingFactory;
        // Do in ServletRegistration
        doInServletRegistration(registration, servletRegistration -> {
            patterns.addAll(factory.getPatterns(servletRegistration));
        });

        return patterns;
    }

    protected void doInServletRegistration(FilterRegistration registration, Consumer<ServletRegistration> servletRegistrationConsumer) {
        Collection<String> servletNameMappings = registration.getServletNameMappings();
        for (String servletName : servletNameMappings) {
            ServletRegistration servletRegistration = servletContext.getServletRegistration(servletName);
            if (servletRegistration != null) {
                servletRegistrationConsumer.accept(servletRegistration);
            }
        }
    }
}
