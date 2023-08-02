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

import io.microsphere.spring.web.metadata.WebEndpointMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import java.util.Optional;

import static io.microsphere.util.ArrayUtils.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link ServletRegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ServletRegistrationWebEndpointMappingFactoryTest {

    private ServletRegistrationWebEndpointMappingFactory factory;

    private String servletName;

    private String url;

    private ServletRegistration.Dynamic registration;

    @BeforeEach
    public void init() throws ServletException {
        servletName = "test-servlet";
        url = "/test";
        this.factory = ServletRegistrationWebEndpointMappingFactory.INSTANCE;

        this.registration = new TestServletRegistration(servletName, "TestServlet");
        this.registration.addMapping(url);
    }

    @Test
    public void testCreate() {

        Optional<WebEndpointMapping<?>> webEndpointMapping = factory.create(registration);
        webEndpointMapping.ifPresent(mapping -> {
            assertEquals(this.servletName, mapping.getEndpoint());
            assertArrayEquals(of(this.url), mapping.getPatterns());
        });
    }
}
