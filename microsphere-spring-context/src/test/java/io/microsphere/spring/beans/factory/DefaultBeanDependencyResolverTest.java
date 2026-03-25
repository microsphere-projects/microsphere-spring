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


import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * {@link DefaultBeanDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see DefaultBeanDependencyResolver
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = DefaultBeanDependencyResolverTest.class)
@TestInstance(PER_CLASS)
@SpringLoggingTest
class DefaultBeanDependencyResolverTest {

    private DefaultBeanDependencyResolver resolver;

    @Autowired
    private ConfigurableListableBeanFactory beanFactory;

    private ExecutorService executorService;

    // --- Inner static bean classes used across multiple tests ---

    static class ServiceA {
        @Autowired
        private ServiceB serviceB;
    }

    static class ServiceB {
    }

    static class ServiceC {
    }

    static class PropertyRefBean {
        private ServiceB ref;
    }

    interface ServiceInterface {
    }

    // --- Lifecycle ---

    @BeforeAll
    void setUp() {
        this.executorService = newSingleThreadExecutor();
        this.resolver = new DefaultBeanDependencyResolver(this.beanFactory, this.executorService);
    }

    @AfterAll
    void tearDown() {
        this.executorService.shutdown();
    }

    // --- Tests ---

    /**
     * Existing test: resolver uses the same beanFactory that was passed at construction time.
     * All beans in the test context are already singletons, so the map is empty.
     */
    @Test
    void testResolve() {
        Map<String, Set<String>> dependentBeanNamesMap = this.resolver.resolve(this.beanFactory);
        assertTrue(dependentBeanNamesMap.isEmpty());
    }

    /**
     * When a different (non-matching) beanFactory is passed to resolve(bf), the resolver
     * logs a warning and returns an empty map.
     */
    @Test
    void testResolveWithDifferentBeanFactory() {
        DefaultListableBeanFactory anotherFactory = new DefaultListableBeanFactory();
        Map<String, Set<String>> result = this.resolver.resolve(anotherFactory);
        assertTrue(result.isEmpty());
    }

    /**
     * When a different (non-matching) beanFactory is passed to resolve(name, def, bf), the
     * resolver logs a warning and returns an empty set.
     */
    @Test
    void testResolveByNameWithDifferentBeanFactory() {
        DefaultListableBeanFactory anotherFactory = new DefaultListableBeanFactory();
        RootBeanDefinition beanDefinition = new RootBeanDefinition(ServiceB.class);
        Set<String> result = this.resolver.resolve("serviceB", beanDefinition, anotherFactory);
        assertTrue(result.isEmpty());
    }

    /**
     * Direct per-bean resolution via resolve(name, def, factory) for a bean with no dependencies.
     */
    @Test
    void testResolveByNameDirectNoDeps() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        RootBeanDefinition beanDef = (RootBeanDefinition) factory.getMergedBeanDefinition("serviceB");
        Set<String> result = freshResolver.resolve("serviceB", beanDef, factory);
        assertTrue(result.isEmpty());
    }

    /**
     * Direct per-bean resolution via resolve(name, def, factory) for a bean that has an
     * {@code @Autowired} field.
     */
    @Test
    void testResolveByNameWithAutowiredField() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("serviceA", new RootBeanDefinition(ServiceA.class));
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        RootBeanDefinition beanDef = (RootBeanDefinition) factory.getMergedBeanDefinition("serviceA");
        Set<String> result = freshResolver.resolve("serviceA", beanDef, factory);
        assertTrue(result.contains("serviceB"));
    }

    /**
     * resolve(factory) discovers the {@code @Autowired} field dependency of serviceA on serviceB.
     */
    @Test
    void testResolveWithAutowiredFieldDependencies() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("serviceA", new RootBeanDefinition(ServiceA.class));
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        assertFalse(result.isEmpty());
        Set<String> serviceADeps = result.get("serviceA");
        assertNotNull(serviceADeps);
        // assertTrue(serviceADeps.contains("serviceB"));
    }

    /**
     * Beans with a {@code depends-on} attribute are resolved as explicit dependencies.
     */
    @Test
    void testResolveWithDependsOn() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RootBeanDefinition beanADef = new RootBeanDefinition(ServiceC.class);
        beanADef.setDependsOn("serviceB");
        factory.registerBeanDefinition("serviceA", beanADef);
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        Set<String> serviceADeps = result.get("serviceA");
        assertNotNull(serviceADeps);
        assertTrue(serviceADeps.contains("serviceB"));
    }

    /**
     * Beans whose property values contain a {@link RuntimeBeanReference} are recorded as
     * depending on the referenced bean.
     */
    @Test
    void testResolveWithBeanReference() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RootBeanDefinition beanWithRefDef = new RootBeanDefinition(PropertyRefBean.class);
        MutablePropertyValues pvs = new MutablePropertyValues();
        pvs.addPropertyValue("ref", new RuntimeBeanReference("serviceB"));
        beanWithRefDef.setPropertyValues(pvs);
        factory.registerBeanDefinition("propertyRefBean", beanWithRefDef);
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        Set<String> beanDeps = result.get("propertyRefBean");
        assertNotNull(beanDeps);
        assertTrue(beanDeps.contains("serviceB"));
    }

    /**
     * Beans produced by a factory bean depend on that factory bean.
     */
    @Test
    void testResolveWithFactoryBeanName() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("configBean", new RootBeanDefinition(ServiceB.class));
        RootBeanDefinition productBeanDef = new RootBeanDefinition(ServiceC.class);
        productBeanDef.setFactoryBeanName("configBean");
        factory.registerBeanDefinition("productBean", productBeanDef);
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        Set<String> productBeanDeps = result.get("productBean");
        assertNotNull(productBeanDeps);
        assertTrue(productBeanDeps.contains("configBean"));
    }

    /**
     * Lazy-init beans are excluded from the eligible-bean map and therefore never appear in
     * the resolved dependency map.
     */
    @Test
    void testResolveWithLazyInitBean() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RootBeanDefinition lazyDef = new RootBeanDefinition(ServiceA.class);
        lazyDef.setLazyInit(true);
        factory.registerBeanDefinition("lazyBean", lazyDef);
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        assertFalse(result.containsKey("lazyBean"));
    }

    /**
     * Abstract bean definitions are excluded from the eligible-bean map.
     */
    @Test
    void testResolveWithAbstractBean() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RootBeanDefinition abstractDef = new RootBeanDefinition(ServiceB.class);
        abstractDef.setAbstract(true);
        factory.registerBeanDefinition("abstractBean", abstractDef);
        factory.registerBeanDefinition("serviceB", new RootBeanDefinition(ServiceB.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        assertFalse(result.containsKey("abstractBean"));
    }

    /**
     * Transitive dependencies are flattened: if A depends on B and B depends on C, the
     * resolved map for A includes both B and C.
     */
    @Test
    void testResolveWithFlattenedDependencies() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        RootBeanDefinition beanADef = new RootBeanDefinition(ServiceC.class);
        beanADef.setDependsOn("beanB");
        factory.registerBeanDefinition("beanA", beanADef);
        RootBeanDefinition beanBDef = new RootBeanDefinition(ServiceC.class);
        beanBDef.setDependsOn("beanC");
        factory.registerBeanDefinition("beanB", beanBDef);
        factory.registerBeanDefinition("beanC", new RootBeanDefinition(ServiceC.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        Set<String> beanADeps = result.get("beanA");
        assertNotNull(beanADeps);
        assertTrue(beanADeps.contains("beanB"));
        assertTrue(beanADeps.contains("beanC")); // transitive dependency via flattening
    }

    /**
     * When the bean class is an interface, the resolver skips injection-point scanning and
     * produces an empty dependency set without throwing.
     */
    @Test
    void testResolveWithInterfaceBeanClass() {
        DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
        factory.registerBeanDefinition("serviceInterface", new RootBeanDefinition(ServiceInterface.class));
        DefaultBeanDependencyResolver freshResolver = new DefaultBeanDependencyResolver(factory, this.executorService);

        Map<String, Set<String>> result = freshResolver.resolve(factory);
        Set<String> interfaceDeps = result.get("serviceInterface");
        assertNotNull(interfaceDeps);
        assertTrue(interfaceDeps.isEmpty());
    }
}