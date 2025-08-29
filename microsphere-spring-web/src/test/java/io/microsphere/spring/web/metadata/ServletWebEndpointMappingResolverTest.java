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
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.support.GenericWebApplicationContext;

import javax.servlet.FilterRegistration;
import java.util.Collection;
import java.util.Iterator;

import static io.microsphere.spring.test.util.ServletTestUtils.addTestFilter;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.FILTER;
import static io.microsphere.spring.web.metadata.WebEndpointMapping.Kind.SERVLET;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptyList;
import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.REQUEST;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * {@link ServletWebEndpointMappingResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ServletWebEndpointMappingResolver
 * @since 1.0.0
 */
public class ServletWebEndpointMappingResolverTest {

    private TestServletContext servletContext;

    private GenericWebApplicationContext context;

    private ServletWebEndpointMappingResolver webEndpointMappingResolver;

    @Before
    public void setUp() throws Exception {
        this.servletContext = new TestServletContext();
        this.context = new GenericWebApplicationContext(servletContext);
        this.webEndpointMappingResolver = new ServletWebEndpointMappingResolver();
    }

    @Test
    public void testResolve() {
        addTestServlet(servletContext);

        FilterRegistration.Dynamic filterRegistration = addTestFilter(servletContext);
        String notFoundServletName = "notFoundServlet";
        filterRegistration.addMappingForServletNames(of(REQUEST), true, notFoundServletName);

        Collection<WebEndpointMapping> webEndpointMappings = webEndpointMappingResolver.resolve(this.context);
        assertWebEndpointMappings(webEndpointMappings);
    }

    @Test
    public void testResolveOnEmpty() {
        Collection<WebEndpointMapping> webEndpointMappings = webEndpointMappingResolver.resolve(this.context);
        assertTrue(webEndpointMappings.isEmpty());
    }

    @Test
    public void testResolveOnServlet2() {
        MockServletContext servletContext = new MockServletContext();
        servletContext.setMajorVersion(2);
        GenericWebApplicationContext context = new GenericWebApplicationContext(servletContext);
        assertSame(emptyList(), this.webEndpointMappingResolver.resolve(context));
    }

    @Test
    public void testResolveOnNullServletContext() {
        GenericWebApplicationContext context = new GenericWebApplicationContext();
        assertSame(emptyList(), this.webEndpointMappingResolver.resolve(context));
    }

    @Test
    public void testResolveOnGenericApplicationContext() {
        GenericApplicationContext context = new GenericApplicationContext();
        assertSame(emptyList(), this.webEndpointMappingResolver.resolve(context));
    }

    static void assertWebEndpointMappings(Collection<WebEndpointMapping> webEndpointMappings) {
        assertEquals(2, webEndpointMappings.size());

        Iterator<WebEndpointMapping> iterator = webEndpointMappings.iterator();

        assertTrue(iterator.hasNext());
        WebEndpointMapping filterWebEndpointMapping = iterator.next();
        assertSame(FILTER, filterWebEndpointMapping.getKind());
        assertArrayEquals(ofArray(DEFAULT_FILTER_URL_PATTERN, DEFAULT_SERVLET_URL_PATTERN), filterWebEndpointMapping.getPatterns());

        assertTrue(iterator.hasNext());
        WebEndpointMapping servletWebEndpointMapping = iterator.next();
        assertSame(SERVLET, servletWebEndpointMapping.getKind());
        assertArrayEquals(ofArray(DEFAULT_SERVLET_URL_PATTERN), servletWebEndpointMapping.getPatterns());
    }
}