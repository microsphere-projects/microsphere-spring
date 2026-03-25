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

package io.microsphere.spring.test.util;


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static io.microsphere.util.ExceptionUtils.create;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/**
 * {@link SpringTestUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see SpringTestUtils
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class SpringTestUtilsTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    @Test
    public void testTestInSpringContainer() {
        testInSpringContainer(context -> {
            assertNotNull(context.getBean(SpringTestUtilsTest.class));
        }, SpringTestUtilsTest.class);
    }

    @Test
    public void testTestInSpringContainerWithEnvironment() {
        testInSpringContainer((context, environment) -> {
            assertNotNull(context.getBean(SpringTestUtilsTest.class));
            assertNotNull(environment);
        }, SpringTestUtilsTest.class);
    }

    @Test
    public void testTestInSpringContainerOnFailed() {
        assertThrows(RuntimeException.class, () -> testInSpringContainer(context -> {
            throw create(Throwable.class);
        }, SpringTestUtilsTest.class));
    }
}