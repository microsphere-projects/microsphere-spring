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

import io.microsphere.annotation.Nonnull;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.filter.ResolvableDependencyTypeFilter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isClass;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.filter.ResolvableDependencyTypeFilter.get;
import static io.microsphere.spring.core.MethodParameterUtils.forParameter;
import static java.util.Collections.addAll;

/**
 * Abstract base class for implementing {@link InjectionPointDependencyResolver}.
 * <p>
 * This class provides a foundation for resolving dependencies at injection points within Spring-managed beans.
 * It handles common tasks such as bean factory awareness, dependency type resolution, and logging using the
 * {@link Logger} interface.
 * </p>
 *
 * <h3>Key Responsibilities</h3>
 * <ul>
 *     <li>Tracking and resolving dependencies based on injection points (fields, methods, constructors).</li>
 *     <li>Filtering resolvable dependency types via the configured {@link ResolvableDependencyTypeFilter}.</li>
 *     <li>Providing consistent logging capabilities through the injected logger.</li>
 *     <li>Supporting custom resolution logic by allowing subclasses to implement specific strategies.</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 *
 * <h4>Basic Implementation</h4>
 * <pre>{@code
 * public class MyDependencyResolver extends AbstractInjectionPointDependencyResolver {
 *     // Custom implementation details here
 * }
 * }</pre>
 *
 * <h4>Resolving Dependencies from a Field</h4>
 * <pre>{@code
 * public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *     String dependentBeanName = resolveDependentBeanNameByName(field, beanFactory);
 *     if (dependentBeanName == null) {
 *         resolveDependentBeanNamesByType(field::getGenericType, beanFactory, dependentBeanNames);
 *     } else {
 *         dependentBeanNames.add(dependentBeanName);
 *     }
 * }
 * }</pre>
 *
 * <h4>Resolving Dependencies from a Method Parameter</h4>
 * <pre>{@code
 * public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
 *     String dependentBeanName = resolveDependentBeanNameByName(parameter, beanFactory);
 *     if (dependentBeanName == null) {
 *         resolveDependentBeanNamesByType(parameter::getParameterizedType, beanFactory, dependentBeanNames);
 *     } else {
 *         dependentBeanNames.add(dependentBeanName);
 *     }
 * }
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractInjectionPointDependencyResolver implements InjectionPointDependencyResolver, BeanFactoryAware {

    protected final Logger logger = getLogger(getClass());

    @Nonnull
    protected ResolvableDependencyTypeFilter resolvableDependencyTypeFilter;

    @Nonnull
    protected AutowireCandidateResolver autowireCandidateResolver;

    /**
     * Resolve the dependent bean names from the given {@link Field} injection point.
     * <p>
     * First attempts to resolve by name using the {@link AutowireCandidateResolver}.
     * If no name is suggested, falls back to resolving by the field's generic type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AbstractInjectionPointDependencyResolver resolver = ...;
     *   resolver.setBeanFactory(beanFactory);
     *   Field field = ReflectionUtils.findField(MyConfig.class, "myDependency");
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(field, beanFactory, dependentBeanNames);
     *   // dependentBeanNames now contains the bean names matching the field
     * }</pre>
     *
     * @param field              the field injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        String dependentBeanName = resolveDependentBeanNameByName(field, beanFactory);
        if (dependentBeanName == null) {
            resolveDependentBeanNamesByType(field::getGenericType, beanFactory, dependentBeanNames);
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

    /**
     * Resolve the dependent bean names from the given {@link Method} injection point.
     * <p>
     * Iterates over each parameter of the method and delegates resolution to
     * {@link #resolve(Parameter, ConfigurableListableBeanFactory, Set)}.
     * Methods with no parameters are ignored.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AbstractInjectionPointDependencyResolver resolver = ...;
     *   resolver.setBeanFactory(beanFactory);
     *   Method method = MethodUtils.findMethod(MyConfig.class, "user", MyDependency[].class);
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(method, beanFactory, dependentBeanNames);
     *   // dependentBeanNames now contains bean names matching the method parameters
     * }</pre>
     *
     * @param method             the method injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Method method, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        int parametersCount = method.getParameterCount();
        if (parametersCount < 1) {
            logger.trace("The no-argument method[{}] will be ignored", method);
            return;
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parametersCount; i++) {
            Parameter parameter = parameters[i];
            resolve(parameter, beanFactory, dependentBeanNames);
        }
    }

    /**
     * Resolve the dependent bean names from the given {@link Constructor} injection point.
     * <p>
     * Iterates over each parameter of the constructor and delegates resolution to
     * {@link #resolve(Parameter, ConfigurableListableBeanFactory, Set)}.
     * Constructors with no parameters are ignored.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AbstractInjectionPointDependencyResolver resolver = ...;
     *   resolver.setBeanFactory(beanFactory);
     *   Constructor<?> constructor = ConstructorUtils.findConstructor(MyConfig.class, Map.class);
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(constructor, beanFactory, dependentBeanNames);
     *   // dependentBeanNames now contains bean names matching the constructor parameters
     * }</pre>
     *
     * @param constructor        the constructor injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Constructor constructor, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        int parametersCount = constructor.getParameterCount();
        if (parametersCount < 1) {
            logger.trace("The no-argument constructor[{}] will be ignored", constructor);
            return;
        }
        Parameter[] parameters = constructor.getParameters();
        for (int i = 0; i < parametersCount; i++) {
            Parameter parameter = parameters[i];
            resolve(parameter, beanFactory, dependentBeanNames);
        }
    }

    /**
     * Resolve the dependent bean names from the given {@link Parameter} injection point.
     * <p>
     * First attempts to resolve by name using the {@link AutowireCandidateResolver}.
     * If no name is suggested, falls back to resolving by the parameter's parameterized type.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AbstractInjectionPointDependencyResolver resolver = ...;
     *   resolver.setBeanFactory(beanFactory);
     *   Method method = MethodUtils.findMethod(MyConfig.class, "user", MyDependency[].class);
     *   Parameter parameter = method.getParameters()[0];
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolver.resolve(parameter, beanFactory, dependentBeanNames);
     *   // dependentBeanNames now contains bean names matching the parameter type
     * }</pre>
     *
     * @param parameter          the parameter injection point to resolve
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        String dependentBeanName = resolveDependentBeanNameByName(parameter, beanFactory);
        if (dependentBeanName == null) {
            resolveDependentBeanNamesByType(parameter::getParameterizedType, beanFactory, dependentBeanNames);
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

    /**
     * Initialize this resolver by extracting the {@link ResolvableDependencyTypeFilter}
     * and {@link AutowireCandidateResolver} from the given {@link BeanFactory}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   AbstractInjectionPointDependencyResolver resolver = new MyDependencyResolver();
     *   resolver.setBeanFactory(beanFactory);
     *   // resolver is now ready to resolve injection point dependencies
     * }</pre>
     *
     * @param beanFactory the owning {@link BeanFactory}
     * @throws BeansException if initialization fails
     */
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.resolvableDependencyTypeFilter = get(beanFactory);
        this.autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
    }

    /**
     * Resolve the dependent bean name by name for the given {@link Field} using
     * the {@link AutowireCandidateResolver}'s suggested value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Field field = ReflectionUtils.findField(MyConfig.class, "myService");
     *   String beanName = resolveDependentBeanNameByName(field, beanFactory);
     *   // beanName is the suggested name, or null if none was suggested
     * }</pre>
     *
     * @param field       the field to resolve the dependent bean name from
     * @param beanFactory the {@link ConfigurableListableBeanFactory} (unused but available for subclass overrides)
     * @return the suggested bean name, or {@code null} if no name was suggested
     */
    protected String resolveDependentBeanNameByName(Field field, ConfigurableListableBeanFactory beanFactory) {
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor);
    }

    /**
     * Resolve the dependent bean name by name for the given {@link Parameter} using
     * the {@link AutowireCandidateResolver}'s suggested value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Parameter parameter = method.getParameters()[0];
     *   String beanName = resolveDependentBeanNameByName(parameter, beanFactory);
     *   // beanName is the suggested name, or null if none was suggested
     * }</pre>
     *
     * @param parameter   the parameter to resolve the dependent bean name from
     * @param beanFactory the {@link ConfigurableListableBeanFactory} (unused but available for subclass overrides)
     * @return the suggested bean name, or {@code null} if no name was suggested
     */
    protected String resolveDependentBeanNameByName(Parameter parameter, ConfigurableListableBeanFactory beanFactory) {
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor);
    }

    /**
     * Resolve the dependent bean name by name from the given {@link DependencyDescriptor}
     * using the {@link AutowireCandidateResolver}'s suggested value.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DependencyDescriptor descriptor = new DependencyDescriptor(field, true, false);
     *   String beanName = resolveDependentBeanNameByName(descriptor);
     *   // beanName is non-null only if the resolver suggests a String value
     * }</pre>
     *
     * @param dependencyDescriptor the {@link DependencyDescriptor} to resolve the bean name from
     * @return the suggested bean name as a {@link String}, or {@code null} if the suggested
     *         value is not a {@code String} or is {@code null}
     */
    protected String resolveDependentBeanNameByName(DependencyDescriptor dependencyDescriptor) {
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    /**
     * Resolve dependent bean names by type from the given type supplier.
     * <p>
     * The resolved type is checked against the {@link ResolvableDependencyTypeFilter};
     * if it is a resolvable dependency type (e.g., {@code BeanFactory}, {@code ApplicationContext}),
     * it is skipped. Otherwise, all bean names matching the type are added to the result set.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   Set<String> dependentBeanNames = new LinkedHashSet<>();
     *   resolveDependentBeanNamesByType(field::getGenericType, beanFactory, dependentBeanNames);
     *   // dependentBeanNames now contains all bean names matching the field's type
     * }</pre>
     *
     * @param typeSupplier       a supplier providing the {@link Type} to resolve bean names for
     * @param beanFactory        the {@link ConfigurableListableBeanFactory} to look up beans
     * @param dependentBeanNames the set to collect resolved dependent bean names into
     */
    protected void resolveDependentBeanNamesByType(Supplier<Type> typeSupplier, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> dependentType = resolveDependentType(typeSupplier);
        if (resolvableDependencyTypeFilter.accept(dependentType)) {
            // The dependent type is a resolvable dependency type
            return;
        }
        String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
        addAll(dependentBeanNames, beanNames);
    }

    private AutowireCandidateResolver getAutowireCandidateResolver(BeanFactory beanFactory) {
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        return dbf.getAutowireCandidateResolver();
    }

    private Class<?> resolveDependentType(Supplier<Type> typeSupplier) {
        Type type = typeSupplier.get();
        return resolveDependentType(type);
    }

    /**
     * Resolve the dependent {@link Class} from the given {@link Type}.
     * <p>
     * Handles plain classes (including array component types), and parameterized types
     * such as {@code ObjectProvider<SomeBean>}, {@code List<SomeBean>},
     * {@code Map<String, SomeBean>}, etc. For parameterized types, the last type argument
     * is recursively resolved.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   // For a field declared as Optional<List<MyBean>>
     *   Type type = field.getGenericType();
     *   Class<?> dependentType = resolveDependentType(type);
     *   // dependentType == MyBean.class
     * }</pre>
     *
     * @param type the {@link Type} to resolve
     * @return the resolved {@link Class}, or {@code null} if the type cannot be resolved
     */
    protected Class<?> resolveDependentType(Type type) {
        if (isClass(type)) {
            Class klass = (Class) type;
            if (klass.isArray()) {
                return resolveDependentType(klass.getComponentType());
            }
            return klass;
        } else if (isParameterizedType(type)) {
            // ObjectProvider<SomeBean> == arguments == [SomeBean.class]
            // ObjectFactory<SomeBean> == arguments == [SomeBean.class]
            // Optional<SomeBean> == arguments == [SomeBean.class]
            // javax.inject.Provider<SomeBean> == arguments == [SomeBean.class]
            // Map<String,SomeBean> == arguments == [SomeBean.class]
            // List<SomeBean> == arguments= [SomeBean.class]
            // Set<SomeBean> == arguments= [SomeBean.class]
            List<Type> arguments = resolveActualTypeArguments(type, asClass(type));
            // Last argument
            Type argumentType = arguments.get(arguments.size() - 1);
            return resolveDependentType(argumentType);
        }
        return null;
    }
}
