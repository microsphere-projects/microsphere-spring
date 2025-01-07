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
package io.microsphere.spring.util;


import io.microsphere.util.Version;
import org.junit.Test;
import org.springframework.util.StringUtils;

import static io.microsphere.spring.util.SpringVersionUtils.SPRING_AOP_VERSION;
import static io.microsphere.spring.util.SpringVersionUtils.SPRING_BEANS_VERSION;
import static io.microsphere.spring.util.SpringVersionUtils.SPRING_CONTEXT_SUPPORT_VERSION;
import static io.microsphere.spring.util.SpringVersionUtils.SPRING_CONTEXT_VERSION;
import static io.microsphere.spring.util.SpringVersionUtils.SPRING_CORE_VERSION;
import static io.microsphere.spring.util.SpringVersionUtils.getSpringVersion;
import static org.junit.Assert.assertEquals;

/**
 * {@link SpringVersionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class SpringVersionUtilsTest {

    @Test(expected = NullPointerException.class)
    public void testGetSpringVersionOnNPE() {
        getSpringVersion((Class) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetSpringVersionOnIAE() {
        getSpringVersion(String.class);
    }

    @Test
    public void testGetSpringVersion() {
        Version version = getSpringVersion(StringUtils.class);
        assertEquals(version,SPRING_AOP_VERSION);
        assertEquals(SPRING_CORE_VERSION,SPRING_AOP_VERSION);
        assertEquals(SPRING_AOP_VERSION, SPRING_BEANS_VERSION);
        assertEquals(SPRING_BEANS_VERSION, SPRING_CONTEXT_VERSION);
        assertEquals(SPRING_CONTEXT_VERSION, SPRING_CONTEXT_SUPPORT_VERSION);
    }
}
