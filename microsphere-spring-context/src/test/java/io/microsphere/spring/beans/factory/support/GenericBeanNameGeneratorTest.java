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

package io.microsphere.spring.beans.factory.support;


import io.microsphere.spring.beans.BeanUtils;
import io.microsphere.spring.test.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;

import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static io.microsphere.spring.beans.factory.support.GenericBeanNameGenerator.INSTANCE;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link GenericBeanNameGenerator} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see GenericBeanNameGenerator
 * @since 1.0.0
 */
class GenericBeanNameGeneratorTest {

    @Test
    void testGenerateBeanName() {
        asserBeanName(User.class);
        asserBeanName(GenericBeanNameGenerator.class);
        asserBeanName(GenericBeanNameGeneratorTest.class);
    }

    void asserBeanName(Class<?> beanType) {
        BeanDefinition beanDefinition = genericBeanDefinition(beanType);
        String beanName = INSTANCE.generateBeanName(beanDefinition, null);
        assertEquals(BeanUtils.generateBeanName(beanType), beanName);
    }
}