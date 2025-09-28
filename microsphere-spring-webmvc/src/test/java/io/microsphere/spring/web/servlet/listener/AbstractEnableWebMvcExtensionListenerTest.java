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

package io.microsphere.spring.web.servlet.listener;


import io.microsphere.spring.test.tomcat.embedded.EmbeddedTomcatConfiguration;
import io.microsphere.spring.web.util.RequestContextStrategy;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.RequestContextFilter;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import static io.microsphere.util.ArrayUtils.asArray;
import static io.microsphere.util.ArrayUtils.ofArray;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.springframework.web.context.support.WebApplicationContextUtils.getRequiredWebApplicationContext;

/**
 * {@link EnableWebMvcExtensionListener} Test for {@link @EnableWebMvcExtension} with
 * {@link RequestContextStrategy#INHERITABLE_THREAD_LOCAL} strategy.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtensionListener
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@EmbeddedTomcatConfiguration(classes = Object.class)
abstract class AbstractEnableWebMvcExtensionListenerTest {

    @Autowired
    WebApplicationContext wac;

    ServletContext servletContext;

    @Before
    public void setUp() {
        this.servletContext = wac.getServletContext();
    }

    @Test
    public void test() {
        assertServletContext(this.wac, this.servletContext);

        assertDispatcherServlet(this.servletContext);

        assertRequestContextFilter(this.servletContext);
    }

    static void assertServletContext(WebApplicationContext wac, ServletContext servletContext) {
        assertNotNull(wac);
        assertNotNull(servletContext);
        assertSame(wac, getRequiredWebApplicationContext(servletContext));
    }

    static void assertDispatcherServlet(ServletContext servletContext) {
        ServletRegistration servletRegistration = servletContext.getServletRegistration("dispatcherServlet");
        assertEquals(DispatcherServlet.class.getName(), servletRegistration.getClassName());
        assertNotNull(servletRegistration.getInitParameter("threadContextInheritable"));
        assertArrayEquals(ofArray("/*"), asArray(servletRegistration.getMappings(), String.class));
    }

    static void assertRequestContextFilter(ServletContext servletContext) {
        FilterRegistration filterRegistration = servletContext.getFilterRegistration("requestContextFilter");
        assertEquals(RequestContextFilter.class.getName(), filterRegistration.getClassName());
        assertArrayEquals(ofArray(), asArray(filterRegistration.getServletNameMappings(), String.class));
        assertArrayEquals(ofArray("/*"), asArray(filterRegistration.getUrlPatternMappings(), String.class));
    }
}