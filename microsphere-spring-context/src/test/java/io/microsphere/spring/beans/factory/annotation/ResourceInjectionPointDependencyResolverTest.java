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
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.test.context.ContextConfiguration;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
        ResourceInjectionPointDependencyResolverTest.TypedConfig.class,
})
public class ResourceInjectionPointDependencyResolverTest extends AbstractInjectionPointDependencyResolverTest<ResourceInjectionPointDependencyResolver> {

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

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

    /** Field without @Resource → resolver returns early, no bean names added */
    @Test
    public void testResolveFieldWithoutResource() {
        Field field = findField(TypedConfig.class, "noResourceField");
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(field, beanFactory, names);
        assertTrue(names.isEmpty());
    }

    /** Field with @Resource(name="explicit") → uses the explicit bean name */
    @Test
    public void testResolveFieldWithExplicitName() {
        Field field = findField(TypedConfig.class, "namedField");
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(field, beanFactory, names);
        assertEquals(1, names.size());
        assertTrue(names.contains("resourceInjectionPointDependencyResolverTest"));
    }

    /** Field with @Resource(type=ResourceInjectionPointDependencyResolverTest.class) → resolves by type */
    @Test
    public void testResolveFieldWithExplicitType() {
        Field field = findField(TypedConfig.class, "typedField");
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(field, beanFactory, names);
        assertFalse(names.isEmpty());
    }

    /** Parameter on a non-setter method without explicit @Resource name → empty name added */
    @Test
    public void testResolveParameterOnNonSetterMethod() {
        Method method = findMethod(TypedConfig.class, "doSomething", ResourceInjectionPointDependencyResolverTest.class);
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(method.getParameters()[0], beanFactory, names);
        // Method name "doSomething" doesn't start with "set", name from resource.name() is "", so "" is added
        assertEquals(1, names.size());
    }

    /** Parameter on a setter method → bean name derived from method name */
    @Test
    public void testResolveParameterOnSetterMethod() {
        Method method = findMethod(Config.class, "setResourceInjectionPointDependencyResolverTest",
                ResourceInjectionPointDependencyResolverTest.class);
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(method.getParameters()[0], beanFactory, names);
        assertEquals(1, names.size());
        assertTrue(names.contains("resourceInjectionPointDependencyResolverTest"));
    }

    /** Parameter with @Resource(name="explicit") on method → explicit name used */
    @Test
    public void testResolveParameterWithExplicitName() {
        Method method = findMethod(TypedConfig.class, "setNamedParam", ResourceInjectionPointDependencyResolverTest.class);
        Set<String> names = new LinkedHashSet<>();
        resolver.resolve(method.getParameters()[0], beanFactory, names);
        assertEquals(1, names.size());
        assertTrue(names.contains("resourceInjectionPointDependencyResolverTest"));
    }

    static class Config {

        @Resource
        private ResourceInjectionPointDependencyResolverTest resourceInjectionPointDependencyResolverTest;

        @Resource
        public void setResourceInjectionPointDependencyResolverTest(ResourceInjectionPointDependencyResolverTest test) {
        }
    }

    static class TypedConfig {

        // No @Resource annotation → early return
        private ResourceInjectionPointDependencyResolverTest noResourceField;

        // @Resource with explicit name
        @Resource(name = "resourceInjectionPointDependencyResolverTest")
        private ResourceInjectionPointDependencyResolverTest namedField;

        // @Resource with explicit type → resolves by type
        @Resource(type = ResourceInjectionPointDependencyResolverTest.class)
        private Object typedField;

        // Non-setter method – @Resource on method (no name, no type)
        @Resource
        public void doSomething(ResourceInjectionPointDependencyResolverTest test) {
        }

        // Setter method with @Resource carrying explicit name for its parameter
        @Resource(name = "resourceInjectionPointDependencyResolverTest")
        public void setNamedParam(ResourceInjectionPointDependencyResolverTest test) {
        }
    }
}