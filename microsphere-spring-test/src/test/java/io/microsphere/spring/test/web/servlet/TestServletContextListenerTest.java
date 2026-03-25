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


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.springframework.mock.web.MockServletContext;

import javax.servlet.ServletContextEvent;
import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;

/**
 * {@link TestServletContextListener} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see TestServletContextListener
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class TestServletContextListenerTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    private TestServletContextListener listener;

    @Before
    public void setUp() {
        this.listener = new TestServletContextListener();
    }

    @Test
    public void testContextInitialized() {
        this.listener.contextInitialized(new ServletContextEvent(new MockServletContext()));
    }

    @Test
    public void testContextDestroyed() {
        this.listener.contextDestroyed(new ServletContextEvent(new MockServletContext()));
    }
}