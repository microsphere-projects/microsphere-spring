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

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static java.util.Collections.emptyList;

/**
 * {@link WebEndpointMappingResolver} based on Servlet Components
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext
 * @see WebEndpointMappingResolver
 * @since 1.0.0
 */
public class ServletWebEndpointMappingResolver implements WebEndpointMappingResolver {

    @Override
    public Collection<WebEndpointMapping> resolve(ApplicationContext context) {
        ServletContext servletContext = getServletContext(context);
        return resolve(servletContext);
    }

    public Collection<WebEndpointMapping> resolve(ServletContext servletContext) {
        if (servletContext == null || servletContext.getMajorVersion() < 3) {
            return emptyList();
        }
        List<WebEndpointMapping> webEndpointMappings = newLinkedList();
        resolve(servletContext, webEndpointMappings);
        return webEndpointMappings;
    }

    void resolve(ServletContext servletContext, List<WebEndpointMapping> webEndpointMappings) {
        resolveFromFilters(servletContext, webEndpointMappings);
        resolveFromServlets(servletContext, webEndpointMappings);
    }

    void resolveFromFilters(ServletContext servletContext, List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends FilterRegistration> filterRegistrations = servletContext.getFilterRegistrations();
        if (filterRegistrations.isEmpty()) {
            return;
        }

        FilterRegistrationWebEndpointMappingFactory factory = new FilterRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends FilterRegistration> entry : filterRegistrations.entrySet()) {
            String filterName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(filterName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    void resolveFromServlets(ServletContext servletContext, List<WebEndpointMapping> webEndpointMappings) {
        Map<String, ? extends ServletRegistration> servletRegistrations = servletContext.getServletRegistrations();
        if (servletRegistrations.isEmpty()) {
            return;
        }

        ServletRegistrationWebEndpointMappingFactory factory = new ServletRegistrationWebEndpointMappingFactory(servletContext);
        for (Map.Entry<String, ? extends ServletRegistration> entry : servletRegistrations.entrySet()) {
            String servletName = entry.getKey();
            Optional<WebEndpointMapping<String>> webEndpointMapping = factory.create(servletName);
            webEndpointMapping.ifPresent(webEndpointMappings::add);
        }
    }

    protected ServletContext getServletContext(ApplicationContext context) {
        if (!(context instanceof WebApplicationContext)) {
            return null;
        }
        WebApplicationContext webApplicationContext = (WebApplicationContext) context;
        return webApplicationContext.getServletContext();
    }
}
