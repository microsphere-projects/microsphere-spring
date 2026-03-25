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

package io.microsphere.spring.test.tomcat.embedded;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletRegistration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

/**
 * {@link EmbeddedTomcatConfiguration} Test with default attributes
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedTomcatConfiguration
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@EmbeddedTomcatConfiguration
public class EmbeddedTomcatConfigurationTest {

    @Autowired
    private ConfigurableWebApplicationContext wac;

    @Autowired
    private ApplicationContext context;

    @Test
    public void test() {
        assertNotNull(this.wac);

        assertNotNull(this.context);

        assertNotSame(this.wac, this.context);

        ServletContext servletContext = this.wac.getServletContext();
        assertNotNull(servletContext);

        ServletRegistration servletRegistration = servletContext.getServletRegistration("dispatcherServlet");
        assertNotNull(servletRegistration);
        assertEquals(DispatcherServlet.class.getName(), servletRegistration.getClassName());
        assertEquals("", servletRegistration.getInitParameter("contextConfigLocation"));
        assertEquals("/*", servletRegistration.getMappings().iterator().next());
    }
}
