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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * {@link AnnotatedInjectionPointDependencyResolver} for {@link Autowired}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class AutowiredInjectionPointDependencyResolver extends AnnotatedInjectionPointDependencyResolver<Autowired> {

    /**
     * Get the {@link Autowired} annotation from the given {@link Parameter}.
     * <p>
     * First looks for {@code @Autowired} directly on the parameter. If not found,
     * falls back to looking for {@code @Autowired} on the declaring method or constructor.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
     *   // Given: @Bean public User user(@Autowired MyDependency[] deps) { ... }
     *   Method method = MethodUtils.findMethod(Config.class, "user", MyDependency[].class);
     *   Parameter parameter = method.getParameters()[0];
     *   Autowired autowired = resolver.getAnnotation(parameter);
     *   // autowired is non-null since @Autowired is present on the parameter
     * }</pre>
     *
     * @param parameter the parameter to inspect for the {@link Autowired} annotation
     * @return the {@link Autowired} annotation, or {@code null} if not found on the parameter
     *         or its declaring executable
     */
    @Override
    public Autowired getAnnotation(Parameter parameter) {
        // Find @Autowired annotation in the parameter
        Autowired autowired = super.getAnnotation(parameter);
        if (autowired == null) {
            // try to find @Autowired annotation in the method or constructor
            Executable executable = parameter.getDeclaringExecutable();
            autowired = super.getAnnotation(executable);
        }
        return autowired;
    }

    /**
     * Resolve the dependent bean names from the given {@link Field} if it is annotated
     * with {@link Autowired}. Fields without the annotation are silently skipped.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Autowired private Optional<List<MyDependency>> test;
     *   Field field = ReflectionUtils.findField(Config.class, "test");
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(field, beanFactory, dependentBeanNames);
     *   // dependentBeanNames contains bean names matching the field type
     * }</pre>
     *
     * @param field              the field injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Autowired autowired = getAnnotation(field);
        if (autowired == null) {
            // @Autowired annotation can't be found in the field
            return;
        }
        super.resolve(field, beanFactory, dependentBeanNames);
    }

    /**
     * Resolve the dependent bean names from the given {@link Parameter} if it or its
     * declaring method/constructor is annotated with {@link Autowired}. Parameters without
     * the annotation are silently skipped.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AutowiredInjectionPointDependencyResolver resolver = new AutowiredInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Autowired public Config(Map<String, MyDependency> test) { }
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
        Autowired autowired = getAnnotation(parameter);
        if (autowired == null) {
            // @Autowired annotation can't be found in the method parameter
            return;
        }
        super.resolve(parameter, beanFactory, dependentBeanNames);
    }
}
