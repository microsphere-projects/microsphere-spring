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
package io.microsphere.spring.beans.factory;


import io.microsphere.spring.util.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static io.microsphere.reflect.ConstructorUtils.findConstructor;
import static io.microsphere.reflect.MethodUtils.findMethod;
import static org.junit.Assert.assertTrue;
import static org.springframework.core.ResolvableType.forClass;
import static org.springframework.util.ReflectionUtils.findField;

/**
 * Abstract {@link AbstractInjectionPointDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AbstractInjectionPointDependencyResolver
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = AbstractInjectionPointDependencyResolverTest.Config.class)
public abstract class AbstractInjectionPointDependencyResolverTest<R extends AbstractInjectionPointDependencyResolver> {

    protected R resolver;

    @Autowired
    protected ConfigurableListableBeanFactory beanFactory;

    protected Set<String> dependentBeanNames;

    @Before
    public void before() throws InstantiationException, IllegalAccessException {
        this.resolver = (R) forClass(this.getClass())
                .as(AbstractInjectionPointDependencyResolverTest.class)
                .getGeneric(0)
                .toClass().newInstance();
        this.resolver.setBeanFactory(beanFactory);
        this.dependentBeanNames = new LinkedHashSet<>();
    }

    protected Field getField() {
        return findField(Config.class, "test");
    }

    protected Method getMethod() {
        return findMethod(Config.class, "user", AbstractInjectionPointDependencyResolverTest[].class);
    }

    protected Constructor getConstructor() {
        return findConstructor(Config.class, Map.class);
    }

    protected abstract void testResolveFromField(Set<String> dependentBeanNames);

    protected abstract void testResolveFromMethod(Set<String> dependentBeanNames);

    protected abstract void testResolveFromConstructor(Set<String> dependentBeanNames);

    @Test
    public void testResolveFromField() {
        resolver.resolve(this.getField(), this.beanFactory, this.dependentBeanNames);
        testResolveFromField(this.dependentBeanNames);
    }

    @Test
    public void testResolveFromMethod() {
        resolver.resolve(this.getMethod(), this.beanFactory, this.dependentBeanNames);
        testResolveFromMethod(this.dependentBeanNames);
    }

    @Test
    public void testResolveFromMethodWithoutParameter() {
        resolver.resolve(findMethod(getClass(), "testResolveFromMethodWithoutParameter"), this.beanFactory, this.dependentBeanNames);
        assertTrue(this.dependentBeanNames.isEmpty());
    }

    @Test
    public void testResolveFromConstructor() {
        resolver.resolve(this.getConstructor(), this.beanFactory, this.dependentBeanNames);
        testResolveFromConstructor(this.dependentBeanNames);
    }

    @Test
    public void testResolveFromConstructorWithoutParameter() {
        resolver.resolve(findConstructor(getClass()), this.beanFactory, this.dependentBeanNames);
        assertTrue(this.dependentBeanNames.isEmpty());
    }

    static class Config {

        @Autowired
        private Optional<List<AbstractInjectionPointDependencyResolverTest>> test;

        @Autowired
        public Config(Map<String, AbstractInjectionPointDependencyResolverTest> test) {
        }

        @Bean
        public User user(@Autowired AbstractInjectionPointDependencyResolverTest[] test) {
            return new User();
        }

    }
}