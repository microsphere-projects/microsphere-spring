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

package io.microsphere.spring.test.jdbc.embedded;


import org.junit.Test;
import org.springframework.beans.factory.BeanCreationException;

import static io.microsphere.spring.test.util.SpringTestUtils.testInSpringContainer;
import static org.junit.Assert.assertThrows;

/**
 * {@link EmbeddedDataBaseBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EmbeddedDataBaseBeanDefinitionRegistrar
 * @since 1.0.0
 */
public class EmbeddedDataBaseBeanDefinitionRegistrarTest {

    @Test
    public void test() {
        assertThrows(BeanCreationException.class, () -> {
            testInSpringContainer(context -> {
            }, Config.class);
        });
    }

    @EnableEmbeddedDatabase(dataSource = "primary")
    @EnableEmbeddedDatabase(dataSource = "primary")
    static class Config {

    }
}