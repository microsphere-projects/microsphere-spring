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
package io.microsphere.spring.constants;

import org.junit.jupiter.api.Test;

import static io.microsphere.spring.constants.PropertyConstants.AUTO_REGISTERED_PROPERTY_NAME_SUFFIX;
import static io.microsphere.spring.constants.PropertyConstants.BEANS_PROPERTY_NAME_PREFIX;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PropertyConstants} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see PropertyConstants
 * @since 1.0.0
 */
class PropertyConstantsTest {

    @Test
    void test() {
        assertEquals("microsphere.spring.", MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX);
        assertEquals("microsphere.spring.beans.", BEANS_PROPERTY_NAME_PREFIX);
        assertEquals("auto-registered", AUTO_REGISTERED_PROPERTY_NAME_SUFFIX);
        assertEquals("true", DEFAULT_AUTO_REGISTERED_PROPERTY_VALUE);
        assertEquals(true, DEFAULT_AUTO_REGISTERED_VALUE);
    }
}
