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

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import javax.annotation.Resource;
import java.beans.Introspector;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.Set;

import static org.springframework.util.StringUtils.hasText;

/**
 * {@link AnnotatedInjectionPointDependencyResolver} for {@link Resource}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ResourceInjectionPointDependencyResolver extends AnnotatedInjectionPointDependencyResolver<Resource> {

    /**
     * Get the {@link Resource} annotation from the given {@link Parameter}.
     * <p>
     * Unlike typical annotation lookup, this method looks for {@code @Resource} on the
     * declaring executable (method) rather than the parameter itself, since {@code @Resource}
     * is typically placed on setter methods.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResourceInjectionPointDependencyResolver resolver = new ResourceInjectionPointDependencyResolver();
     *   // Given: @Resource public void setMyService(MyService service) { }
     *   Method method = ReflectionUtils.findMethod(Config.class, "setMyService", MyService.class);
     *   Parameter parameter = method.getParameters()[0];
     *   Resource resource = resolver.getAnnotation(parameter);
     *   // resource is non-null since @Resource is on the method
     * }</pre>
     *
     * @param parameter the parameter whose declaring executable is inspected for {@link Resource}
     * @return the {@link Resource} annotation, or {@code null} if not found on the declaring executable
     */
    @Override
    public Resource getAnnotation(Parameter parameter) {
        // Find @Resource annotation in the method
        Executable executable = parameter.getDeclaringExecutable();
        Resource resource = super.getAnnotation(executable);
        return resource;
    }

    /**
     * Resolve the dependent bean names from the given {@link Field} if it is annotated
     * with {@link Resource}. Fields without the annotation are silently skipped.
     * <p>
     * If the {@code @Resource} specifies an explicit {@code type()}, beans are looked up by that type.
     * Otherwise, the bean name is resolved from the annotation's {@code name()} attribute,
     * falling back to the field name.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResourceInjectionPointDependencyResolver resolver = new ResourceInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Resource private MyService resourceInjectionPointDependencyResolverTest;
     *   Field field = ReflectionUtils.findField(Config.class, "resourceInjectionPointDependencyResolverTest");
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(field, beanFactory, dependentBeanNames);
     *   // dependentBeanNames contains "resourceInjectionPointDependencyResolverTest"
     * }</pre>
     *
     * @param field              the field injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Resource resource = getAnnotation(field);
        if (resource == null) {
            // @Resource annotation can't be found in the field
            return;
        }
        Class<?> beanType = resource.type();
        if (Object.class.equals(beanType)) { // Default value
            String beanName = resolveBeanName(field, resource, beanFactory);
            dependentBeanNames.add(beanName);
        } else {
            String[] beanNames = beanFactory.getBeanNamesForType(beanType, false, false);
            for (String name : beanNames) {
                dependentBeanNames.add(name);
            }
        }
    }


    private String resolveBeanName(Field field, Resource resource, ConfigurableListableBeanFactory beanFactory) {
        String name = resource.name();
        if (hasText(name)) {
            return name;
        } else {
            return field.getName();
        }
    }

    private String resolveBeanName(Parameter parameter, Resource resource, ConfigurableListableBeanFactory beanFactory) {
        String name = resource.name();
        if (!hasText(name)) {
            Executable executable = parameter.getDeclaringExecutable();
            String methodName = executable.getName();
            if (methodName.startsWith("set")) {
                name = Introspector.decapitalize(methodName.substring(3));
            }
        }
        return name;
    }

    /**
     * Resolve the dependent bean names from the given {@link Parameter} if its declaring
     * method is annotated with {@link Resource}. Parameters on methods without the annotation
     * are silently skipped.
     * <p>
     * If the {@code @Resource} specifies an explicit {@code type()}, beans are looked up by that type.
     * Otherwise, the bean name is resolved from the annotation's {@code name()} attribute,
     * falling back to decapitalizing the setter method name (e.g., {@code setMyService} yields
     * {@code "myService"}).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   ResourceInjectionPointDependencyResolver resolver = new ResourceInjectionPointDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // Given: @Resource public void setResourceInjectionPointDependencyResolverTest(MyTest test) { }
     *   Method method = ReflectionUtils.findMethod(Config.class,
     *       "setResourceInjectionPointDependencyResolverTest", MyTest.class);
     *   Parameter parameter = method.getParameters()[0];
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(parameter, beanFactory, dependentBeanNames);
     *   // dependentBeanNames contains "resourceInjectionPointDependencyResolverTest"
     * }</pre>
     *
     * @param parameter          the parameter injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Resource resource = getAnnotation(parameter);
        if (resource == null) {
            // @Resource annotation can't be found in the method parameter
            return;
        }
        Class<?> beanType = resource.type();
        if (Object.class.equals(beanType)) { // Default value
            String beanName = resolveBeanName(parameter, resource, beanFactory);
            dependentBeanNames.add(beanName);
        } else {
            String[] beanNames = beanFactory.getBeanNamesForType(beanType, false, false);
            for (String name : beanNames) {
                dependentBeanNames.add(name);
            }
        }
    }
}
