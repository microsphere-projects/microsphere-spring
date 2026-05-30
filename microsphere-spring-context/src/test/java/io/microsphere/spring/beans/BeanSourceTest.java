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

package io.microsphere.spring.beans;

import org.junit.jupiter.api.Test;

import static io.microsphere.spring.beans.BeanSource.BEAN_FACTORY;
import static io.microsphere.spring.beans.BeanSource.JAVA_SERVICE_PROVIDER;
import static io.microsphere.spring.beans.BeanSource.SPRING_FACTORIES;
import static io.microsphere.spring.beans.BeanSource.values;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link BeanSource} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see BeanSource
 * @since 1.0.0
 */
class BeanSourceTest {

    @Test
    void test() {
        BeanSource[] values = values();
        assertEquals(3, values.length);
        assertEquals(BEAN_FACTORY, values[0]);
        assertEquals(SPRING_FACTORIES, values[1]);
        assertEquals(JAVA_SERVICE_PROVIDER, values[2]);
    }
}