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
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * {@link InjectionPointDependencyResolver} for {@link Bean @Bean} {@link Method method}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class BeanMethodInjectionPointDependencyResolver extends AbstractInjectionPointDependencyResolver {

    /**
     * No-op implementation. Field injection points are not resolved by
     * this resolver since it only handles {@link Bean @Bean} method parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanMethodInjectionPointDependencyResolver resolver = new BeanMethodInjectionPointDependencyResolver();
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
     * No-op implementation. Constructor injection points are not resolved by
     * this resolver since it only handles {@link Bean @Bean} method parameters.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanMethodInjectionPointDependencyResolver resolver = new BeanMethodInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   Constructor<?> constructor = ConstructorUtils.findConstructor(Config.class, Map.class);
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(constructor, beanFactory, dependentBeanNames);
     *   // dependentBeanNames is empty — constructors are ignored
     * }</pre>
     *
     * @param constructor        the constructor injection point (ignored)
     * @param beanFactory        the {@link ConfigurableListableBeanFactory}
     * @param dependentBeanNames the set of dependent bean names (unchanged)
     */
    @Override
    public void resolve(Constructor constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        //DO NOTHING
    }

    /**
     * Resolve the dependent bean names from the given {@link Parameter} only if the parameter
     * belongs to a {@link Method} annotated with {@link Bean @Bean}. Parameters from
     * non-{@code @Bean} methods and constructors are silently skipped.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   BeanMethodInjectionPointDependencyResolver resolver = new BeanMethodInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Bean public User user(@Autowired MyDependency[] deps) { ... }
     *   Method method = MethodUtils.findMethod(Config.class, "user", MyDependency[].class);
     *   Parameter parameter = method.getParameters()[0];
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(parameter, beanFactory, dependentBeanNames);
     *   // dependentBeanNames contains bean names matching MyDependency
     * }</pre>
     *
     * @param parameter          the parameter injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Executable executable = parameter.getDeclaringExecutable();
        if (executable instanceof Method && executable.isAnnotationPresent(Bean.class)) {
            super.resolve(parameter, beanFactory, dependentBeanNames);
        }
    }
}
