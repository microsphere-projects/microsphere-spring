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

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import java.util.EnumSet;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.util.ArrayUtils.ofArray;
import static java.util.EnumSet.of;
import static javax.servlet.DispatcherType.FORWARD;
import static javax.servlet.DispatcherType.REQUEST;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * {@link FilterRegistrationWebEndpointMappingFactory} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FilterRegistrationWebEndpointMappingFactory
 * @since 1.0.0
 */
public class FilterRegistrationWebEndpointMappingFactoryTest {

    private static final String filterName = "testFilter";

    private static final String filterClassName = "org.springframework.web.filter.CharacterEncodingFilter";

    private static final EnumSet<DispatcherType> dispatcherTypes = of(REQUEST, FORWARD);

    private static final String[] urlPatterns = ofArray("/test/*", "/*");

    private ServletContext servletContext;

    private FilterRegistrationWebEndpointMappingFactory factory;

    @Before
    public void setUp() {
        this.servletContext = new TestServletContext();
        this.factory = new FilterRegistrationWebEndpointMappingFactory(this.servletContext);

        FilterRegistration.Dynamic registration = this.servletContext.addFilter(filterName, filterClassName);
        registration.addMappingForUrlPatterns(dispatcherTypes, true, urlPatterns);
    }

    @Test
    public void testGetRegistration() {
        FilterRegistration registration = factory.getRegistration(filterName, this.servletContext);
        assertEquals(filterClassName, registration.getClassName());
        assertArrayEquals(urlPatterns, registration.getUrlPatternMappings().toArray());
    }

    @Test
    public void testGetPatterns() {
        FilterRegistration registration = this.factory.getRegistration(filterName, this.servletContext);
        assertEquals(urlPatterns, this.factory.getPatterns(registration).toArray(new String[0]));
        assertEquals(newLinkedList(registration.getUrlPatternMappings()), newLinkedList(this.factory.getPatterns(registration)));
    }

}