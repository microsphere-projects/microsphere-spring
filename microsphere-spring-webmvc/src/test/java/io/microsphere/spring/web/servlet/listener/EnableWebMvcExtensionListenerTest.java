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


import io.microsphere.spring.test.web.servlet.TestFilter;
import io.microsphere.spring.test.web.servlet.TestServlet;
import io.microsphere.spring.test.web.servlet.TestServletContext;
import io.microsphere.spring.webmvc.annotation.EnableWebMvcExtension;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.FrameworkServlet;

import static io.microsphere.collection.Sets.ofSet;
import static io.microsphere.spring.web.servlet.listener.AbstractEnableWebMvcExtensionListenerTest.assertRequestContextFilter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * {@link EnableWebMvcExtensionListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebMvcExtensionListener
 * @since 1.0.0
 */
@EnableWebMvcExtension
public class EnableWebMvcExtensionListenerTest {

    private EnableWebMvcExtensionListener listener;

    private TestServletContext servletContext;

    @Before
    public void setUp() {
        this.listener = new EnableWebMvcExtensionListener();
        this.servletContext = new TestServletContext();
    }

    @Test
    public void testOnStartup() {
        this.listener.onStartup(ofSet(this.getClass()), this.servletContext);
        assertTrue(this.servletContext.getServletRegistrations().isEmpty());
        assertTrue(this.servletContext.getFilterRegistrations().isEmpty());
    }

    @Test
    public void testOnStartupWithTestServlet() {
        assertNotNull(this.servletContext.addServlet("test", TestServlet.class));
        this.listener.onStartup(ofSet(EnableWebMvcExtensionListenerThreadLocalStrategyTest.class), this.servletContext);
        assertEquals(1, this.servletContext.getServletRegistrations().size());
        assertRequestContextFilter(this.servletContext);
    }

    @Test
    public void testOnStartupWithNullClasses() {
        this.listener.onStartup(null, this.servletContext);
        assertTrue(this.servletContext.getServletRegistrations().isEmpty());
        assertTrue(this.servletContext.getFilterRegistrations().isEmpty());
    }

    @Test
    public void testIsFrameworkServlet() {
        assertTrue(this.listener.isFrameworkServlet(FrameworkServlet.class.getName(), this.servletContext));
        assertFalse(this.listener.isFrameworkServlet(TestServlet.class.getName(), this.servletContext));
    }

    @Test
    public void testHasRequestContextFilterRegistration() {
        assertFalse(this.listener.hasRequestContextFilterRegistration(this.servletContext));

        assertNotNull(this.servletContext.addFilter("test", TestFilter.class));
        assertFalse(this.listener.hasRequestContextFilterRegistration(this.servletContext));
    }
}