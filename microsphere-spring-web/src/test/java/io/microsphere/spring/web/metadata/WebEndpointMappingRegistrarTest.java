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


import io.microsphere.spring.test.web.servlet.TestServletContext;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

import java.util.Collection;

import static io.microsphere.spring.test.util.ServletTestUtils.addTestFilter;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.spring.web.metadata.ServletWebEndpointMappingResolverTest.assertWebEndpointMappings;
import static org.junit.Assert.assertTrue;

/**
 * {@link WebEndpointMappingRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see WebEndpointMappingRegistrar
 * @since 1.0.0
 */
public class WebEndpointMappingRegistrarTest {

    private AnnotationConfigWebApplicationContext context;

    @Before
    public void setUp() throws Exception {
        TestServletContext servletContext = new TestServletContext();

        addTestFilter(servletContext);
        addTestServlet(servletContext);

        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(servletContext);
        context.register(SimpleWebEndpointMappingRegistry.class);
        context.register(ServletWebEndpointMappingResolver.class);
        context.register(WebEndpointMappingRegistrar.class);

        context.refresh();

        this.context = context;
    }

    @Test
    public void test() {
        WebEndpointMappingRegistry registry = this.context.getBean(WebEndpointMappingRegistry.class);
        Collection<WebEndpointMapping> webEndpointMappings = registry.getWebEndpointMappings();
        assertWebEndpointMappings(webEndpointMappings);
    }

    @Test
    public void testWithoutWebEndpointMapping() {
        testInSpringContainer(context -> {
            WebEndpointMappingRegistry registry = context.getBean(WebEndpointMappingRegistry.class);
            Collection<WebEndpointMapping> webEndpointMappings = registry.getWebEndpointMappings();
            assertTrue(webEndpointMappings.isEmpty());
        }, SimpleWebEndpointMappingRegistry.class, NoOpWebEndpointMappingResolver.class, WebEndpointMappingRegistrar.class);
    }
}