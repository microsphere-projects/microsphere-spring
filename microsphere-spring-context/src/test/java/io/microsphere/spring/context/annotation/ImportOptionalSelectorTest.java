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

package io.microsphere.spring.context.annotation;


import io.microsphere.logging.test.junit4.LoggingLevelsRule;
import io.microsphere.spring.test.domain.User;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static io.microsphere.logging.test.junit4.LoggingLevelsRule.levels;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * {@link ImportOptionalSelector} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImportOptionalSelector
 * @see ImportOptional
 * @since 1.0.0
 */
@RunWith(JUnit4.class)
public class ImportOptionalSelectorTest {

    @ClassRule
    public static final LoggingLevelsRule LOGGING_LEVELS_RULE = levels("TRACE", "INFO", "ERROR");


    @Test
    public void testPresent() {
        testInSpringContainer(context -> {
            assertNotNull(context.getBean(User.class));
        }, PresentConfig.class);
    }

    @Test
    public void testAbsent() {
        testInSpringContainer(context -> {
            assertFalse(isBeanPresent(context, "notFound"));
            assertFalse(isBeanPresent(context, "NotFound"));
            assertFalse(isBeanPresent(context, "io.microsphere.NotFound"));
        }, AbsentConfig.class);
    }

    @ImportOptional(value = {
            "io.microsphere.spring.test.domain.User"
    })
    static class PresentConfig {
    }

    @ImportOptional(value = {
            "io.microsphere.NotFound"
    })
    static class AbsentConfig {
    }
}