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

import org.springframework.test.context.ContextConfiguration;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * {@link ConstructionInjectionPointDependencyResolver} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ConstructionInjectionPointDependencyResolver
 * @since 1.0.0
 */
@ContextConfiguration(classes = ConstructionInjectionPointDependencyResolverTest.class)
public class ConstructionInjectionPointDependencyResolverTest extends AbstractInjectionPointDependencyResolverTest<ConstructionInjectionPointDependencyResolver> {

    @Override
    protected void testResolveFromField(Set<String> dependentBeanNames) {
        assertTrue(dependentBeanNames.isEmpty());
    }

    @Override
    protected void testResolveFromMethod(Set<String> dependentBeanNames) {
        assertTrue(dependentBeanNames.isEmpty());
    }

    @Override
    protected void testResolveFromConstructor(Set<String> dependentBeanNames) {
        assertEquals(1, dependentBeanNames.size());
        assertTrue(dependentBeanNames.contains("constructionInjectionPointDependencyResolverTest"));
    }
}