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

import io.microsphere.spring.web.metadata.WebEndpointMappingFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;
import java.util.Collection;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;

/**
 * {@link WebEndpointMappingFactory} from {@link ServletRegistration}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletContext#getServletRegistrations()
 * @see ServletRegistration
 * @since 1.0.0
 */
public class ServletRegistrationWebEndpointMappingFactory extends RegistrationWebEndpointMappingFactory<ServletRegistration> {

    public ServletRegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        super(servletContext);
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
