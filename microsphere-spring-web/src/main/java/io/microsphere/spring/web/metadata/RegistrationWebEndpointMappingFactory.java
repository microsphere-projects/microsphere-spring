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

import io.microsphere.spring.web.metadata.AbstractWebEndpointMappingFactory;
import io.microsphere.spring.web.metadata.WebEndpointMapping;
import org.springframework.lang.NonNull;

import javax.servlet.Registration;
import javax.servlet.ServletContext;

import java.util.Collection;

import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.SERVLET;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.of;

/**
 * The abstract class of {@link AbstractWebEndpointMappingFactory} for Servlet {@link Registration}
 *
 * @param <R> The type of {@link R}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractWebEndpointMappingFactory
 * @since 1.0.0
 */
public abstract class RegistrationWebEndpointMappingFactory<R extends Registration> extends AbstractWebEndpointMappingFactory<String> {

    protected final ServletContext servletContext;

    public RegistrationWebEndpointMappingFactory(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public final boolean supports(String endpoint) {
        return getRegistration(endpoint, this.servletContext) != null;
    }

    @Override
    protected final WebEndpointMapping<String> doCreate(String endpoint) throws Throwable {
        R registration = getRegistration(endpoint, this.servletContext);
        Collection<String> patterns = getPatterns(registration);
        WebEndpointMapping.Builder<String> builder = of(SERVLET, endpoint, patterns);
        builder.source(this.servletContext);
        contribute(endpoint, servletContext, builder);
        return builder.build();
    }

    /**
     * @param name           the name of {@link R Registration}
     * @param servletContext {@link ServletContext}
     * @return The {@link R Registration}
     */
    @NonNull
    protected abstract R getRegistration(String name, ServletContext servletContext);

    /**
     * Get the patterns of {@link R Registration}
     *
     * @param registration {@link R Registration}
     * @return non-null
     */
    @NonNull
    protected abstract Collection<String> getPatterns(R registration);

    /**
     * Contribute the {@link WebEndpointMapping.Builder} to create an instance of {@link WebEndpointMapping}
     *
     * @param endpoint       the name of {@link R Registration}
     * @param servletContext {@link ServletContext}
     * @param builder        {@link WebEndpointMapping.Builder}
     * @throws Throwable an error if contribution failed
     */
    protected void contribute(String endpoint, ServletContext servletContext, WebEndpointMapping.Builder<String> builder) throws Throwable {
        // The sub-class implements the current method
    }
}
