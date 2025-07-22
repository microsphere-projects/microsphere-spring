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

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.Registration;
import javax.servlet.ServletContext;
import java.util.Collection;

import static java.util.Collections.emptyList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

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

    @Before
    public void init() {
        this.servletContext = new MockServletContext();
        this.factory = new RegistrationWebEndpointMappingFactoryImpl(servletContext);
    }

    @Test
    public void testSupports() {
        assertFalse(factory.supports(null));
    }

    @Test
    public void testGetRegistration() {
        assertNull(this.factory.getRegistration(null, this.servletContext));
    }

    @Test
    public void testGetPatterns() {
        assertSame(emptyList(), this.factory.getPatterns(null));
    }

}