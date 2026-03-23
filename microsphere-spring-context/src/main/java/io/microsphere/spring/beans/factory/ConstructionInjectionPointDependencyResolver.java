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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * {@link InjectionPointDependencyResolver} for {@link Constructor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ConstructionInjectionPointDependencyResolver extends AbstractInjectionPointDependencyResolver {

    /**
     * No-op implementation. Field injection points are not resolved by
     * this resolver since it only handles {@link Constructor} parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConstructionInjectionPointDependencyResolver resolver = new ConstructionInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   Field field = ReflectionUtils.findField(Config.class, "myField");
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(field, beanFactory, dependentBeanNames);
     *   // dependentBeanNames is empty — fields are ignored
     * }</pre>
     *
     * @param field              the field injection point (ignored)
     * @param beanFactory        the {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the set of dependent bean names (unchanged)
     */
    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        //DO NOTHING
    }

    /**
     * No-op implementation. Method injection points are not resolved by
     * this resolver since it only handles {@link Constructor} parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConstructionInjectionPointDependencyResolver resolver = new ConstructionInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   Method method = MethodUtils.findMethod(Config.class, "setMyService", MyService.class);
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(method, beanFactory, dependentBeanNames);
     *   // dependentBeanNames is empty — methods are ignored
     * }</pre>
     *
     * @param method             the method injection point (ignored)
     * @param beanFactory        the {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the set of dependent bean names (unchanged)
     */
    @Override
    public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        //DO NOTHING
    }

    /**
     * Resolve the dependent bean names from the given {@link Parameter} only if the parameter
     * belongs to a {@link Constructor}. Parameters from methods are silently skipped.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ConstructionInjectionPointDependencyResolver resolver = new ConstructionInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Autowired public Config(Map<String, MyDependency> deps) { }
     *   Constructor<?> constructor = ConstructorUtils.findConstructor(Config.class, Map.class);
     *   Parameter parameter = constructor.getParameters()[0];
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(parameter, beanFactory, dependentBeanNames);
     *   // dependentBeanNames contains bean names matching the parameter type
     * }</pre>
     *
     * @param parameter          the parameter injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Executable executable = parameter.getDeclaringExecutable();
        if (executable instanceof Constructor) {
            super.resolve(parameter, beanFactory, dependentBeanNames);
        }
    }
}
