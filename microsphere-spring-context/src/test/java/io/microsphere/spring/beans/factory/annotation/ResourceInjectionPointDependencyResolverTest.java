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
package io.microsphere.spring.beans.factory.annotation;


import io.microsphere.spring.beans.factory.AbstractInjectionPointDependencyResolverTest;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.findMethod;

/**
 * {@link ResourceInjectionPointDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResourceInjectionPointDependencyResolver
 * @since 1.0.0
 */
@ContextConfiguration(classes = {
        ResourceInjectionPointDependencyResolverTest.class,
        ResourceInjectionPointDependencyResolverTest.Config.class,
})
class ResourceInjectionPointDependencyResolverTest extends AbstractInjectionPointDependencyResolverTest<ResourceInjectionPointDependencyResolver> {

    @Override
    protected Field getField() {
        return findField(Config.class, "resourceInjectionPointDependencyResolverTest");
    }

    @Override
    protected Method getMethod() {
        return findMethod(Config.class, "setResourceInjectionPointDependencyResolverTest", ResourceInjectionPointDependencyResolverTest.class);
    }

    @Override
    protected void testResolveFromField(Set<String> dependentBeanNames) {
        assertDependentBeanNames(dependentBeanNames);
    }

    @Override
    protected void testResolveFromMethod(Set<String> dependentBeanNames) {
        assertDependentBeanNames(dependentBeanNames);
    }

    @Override
    protected void testResolveFromConstructor(Set<String> dependentBeanNames) {
        assertTrue(dependentBeanNames.isEmpty());
    }

    private void assertDependentBeanNames(Set<String> dependentBeanNames) {
        assertEquals(1, dependentBeanNames.size());
        assertTrue(dependentBeanNames.contains("resourceInjectionPointDependencyResolverTest"));
    }

    static class Config {

        @Resource
        private ResourceInjectionPointDependencyResolverTest resourceInjectionPointDependencyResolverTest;

        @Resource
        public void setResourceInjectionPointDependencyResolverTest(ResourceInjectionPointDependencyResolverTest test) {
        }
    }
}