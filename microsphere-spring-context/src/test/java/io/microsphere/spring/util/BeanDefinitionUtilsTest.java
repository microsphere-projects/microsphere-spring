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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import static io.microsphere.spring.util.BeanDefinitionUtils.genericBeanDefinition;
import static org.junit.Assert.assertEquals;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_APPLICATION;
import static org.springframework.beans.factory.config.BeanDefinition.ROLE_INFRASTRUCTURE;

/**
 * {@link BeanDefinitionUtils} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see BeanDefinitionUtils
 * @since 1.0.0
 */
public class BeanDefinitionUtilsTest {

    /**
     * Test methods:
     * <ul>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class)}</li>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class, int)}</li>
     *     <li>{@link BeanDefinitionUtils#genericBeanDefinition(Class, int, Object...)}</li>
     * </ul>
     */
    @Test
    public void testGenericBeanDefinition() {
        AbstractBeanDefinition beanDefinition = genericBeanDefinition(User.class);
        assertBeanDefinition(beanDefinition, ROLE_APPLICATION);

        beanDefinition = genericBeanDefinition(User.class, ROLE_INFRASTRUCTURE);
        assertBeanDefinition(beanDefinition, ROLE_INFRASTRUCTURE);

        beanDefinition = genericBeanDefinition(User.class, "Mercy", 38);
        assertBeanDefinition(beanDefinition, ROLE_APPLICATION, "Mercy", 38);

        beanDefinition = genericBeanDefinition(User.class, ROLE_INFRASTRUCTURE, "Mercy", 38);
        assertBeanDefinition(beanDefinition, ROLE_INFRASTRUCTURE, "Mercy", 38);

    }

    private void assertBeanDefinition(AbstractBeanDefinition beanDefinition, int role, Object... constructorArguments) {
        ConstructorArgumentValues argumentValues = beanDefinition.getConstructorArgumentValues();
        assertEquals(role, beanDefinition.getRole());
        int length = constructorArguments.length;
        assertEquals(length, argumentValues.getArgumentCount());
        for (int i = 0; i < length; i++) {
            ConstructorArgumentValues.ValueHolder argumentValue = argumentValues.getArgumentValue(i, Object.class);
            assertEquals(constructorArguments[i], argumentValue.getValue());
        }
    }
}
