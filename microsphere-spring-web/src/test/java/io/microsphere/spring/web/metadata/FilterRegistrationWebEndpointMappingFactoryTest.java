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


import io.microsphere.spring.test.web.servlet.TestFilter;
import io.microsphere.spring.test.web.servlet.TestFilterRegistration;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.LinkedList;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestFilter;
import static io.microsphere.spring.test.util.ServletTestUtils.addTestServlet;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_NAME;
import static io.microsphere.spring.test.web.servlet.TestFilter.DEFAULT_FILTER_URL_PATTERN;
import static io.microsphere.spring.test.web.servlet.TestFilter.FILTER_CLASS_NAME;
import static io.microsphere.spring.test.web.servlet.TestServlet.DEFAULT_SERVLET_URL_PATTERN;
import static io.microsphere.spring.web.util.HttpUtils.ALL_HTTP_METHODS;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link FilterRegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FilterRegistrationWebEndpointMappingFactory
 * @since 1.0.0
 */
class FilterRegistrationWebEndpointMappingFactoryTest {

    private ServletContext servletContext;

    private FilterRegistrationWebEndpointMappingFactory factory;

    @BeforeEach
    void setUp() {
        this.servletContext = new TestServletContext();
        this.factory = new FilterRegistrationWebEndpointMappingFactory(this.servletContext);

        addTestServlet(this.servletContext);
        addTestFilter(this.servletContext);
    }

    @Test
    void testGetMethods() {
        FilterRegistration registration = factory.getRegistration(DEFAULT_FILTER_NAME, this.servletContext);
        Collection<String> methods = factory.getMethods(registration);
        assertEquals(ALL_HTTP_METHODS, methods);
    }

    @Test
    void testGetMethodsWithoutUrlPatternMappings() {
        FilterRegistration registration = new TestFilterRegistration(DEFAULT_FILTER_NAME, FILTER_CLASS_NAME, new TestFilter());
        Collection<String> methods = factory.getMethods(registration);
        assertEquals(emptySet(), methods);
    }

    @Test
    void testGetRegistration() {
        FilterRegistration registration = factory.getRegistration(DEFAULT_FILTER_NAME, this.servletContext);
        assertEquals(FILTER_CLASS_NAME, registration.getClassName());
        assertArrayEquals(ofArray(DEFAULT_FILTER_URL_PATTERN), registration.getUrlPatternMappings().toArray(EMPTY_STRING_ARRAY));
    }

    @Test
    void testGetPatterns() {
        FilterRegistration registration = this.factory.getRegistration(DEFAULT_FILTER_NAME, this.servletContext);
        LinkedList<String> patterns = newLinkedList(registration.getUrlPatternMappings());
        patterns.add(DEFAULT_SERVLET_URL_PATTERN);
        assertEquals(patterns, newLinkedList(this.factory.getPatterns(registration)));
    }

}