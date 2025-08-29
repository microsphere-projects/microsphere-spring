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

import jakarta.servlet.Registration;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;

import java.util.Collection;

import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link RegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RegistrationWebEndpointMappingFactory
 * @since 1.0.0
 */
public class RegistrationWebEndpointMappingFactoryTest {

    static class RegistrationWebEndpointMappingFactoryImpl extends RegistrationWebEndpointMappingFactory<Registration> {

        public RegistrationWebEndpointMappingFactoryImpl(ServletContext servletContext) {
            super(servletContext);
        }

        @Override
        protected Collection<String> getMethods(Registration registration) {
            return ALL_HTTP_METHODS;
        }

        @Override
        protected Registration getRegistration(String name, ServletContext servletContext) {
            return null;
        }

        @Override
        protected Collection<String> getPatterns(Registration registration) {
            return emptyList();
        }
    }

    private ServletContext servletContext;

    private RegistrationWebEndpointMappingFactory factory;

    @BeforeEach
    void setUp() {
        this.servletContext = new MockServletContext();
        this.factory = new RegistrationWebEndpointMappingFactoryImpl(servletContext);
    }

    @Test
    void testSupports() {
        assertFalse(factory.supports(null));
    }

    @Test
    void testGetRegistration() {
        assertNull(this.factory.getRegistration(null, this.servletContext));
    }

    @Test
    void testGetMethods() {
        assertSame(ALL_HTTP_METHODS, this.factory.getMethods(null));
    }

    @Test
    void testGetPatterns() {
        assertSame(emptyList(), this.factory.getPatterns(null));
    }

}