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

package io.microsphere.spring.test.web.servlet;


import jakarta.servlet.ServletContextEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockServletContext;

/**
 * {@link TestServletContextListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletContextListener
 * @since 1.0.0
 */
class TestServletContextListenerTest {

    private TestServletContextListener listener;

    @BeforeEach
    void setUp() throws Exception {
        this.listener = new TestServletContextListener();
    }

    @Test
    void testContextInitialized() {
        this.listener.contextInitialized(new ServletContextEvent(new MockServletContext()));
    }

    @Test
    void testContextDestroyed() {
        this.listener.contextDestroyed(new ServletContextEvent(new MockServletContext()));
    }
}