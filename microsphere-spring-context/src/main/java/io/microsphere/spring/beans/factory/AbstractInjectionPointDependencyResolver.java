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

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        String dependentBeanName = resolveDependentBeanNameByName(field, beanFactory);
        if (dependentBeanName == null) {
            resolveDependentBeanNamesByType(field::getGenericType, beanFactory, dependentBeanNames);
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

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

    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        String dependentBeanName = resolveDependentBeanNameByName(parameter, beanFactory);
        if (dependentBeanName == null) {
            resolveDependentBeanNamesByType(parameter::getParameterizedType, beanFactory, dependentBeanNames);
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.resolvableDependencyTypeFilter = get(beanFactory);
        this.autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
    }

    protected String resolveDependentBeanNameByName(Field field, ConfigurableListableBeanFactory beanFactory) {
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor);
    }

    protected String resolveDependentBeanNameByName(Parameter parameter, ConfigurableListableBeanFactory beanFactory) {
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor);
    }

    protected String resolveDependentBeanNameByName(DependencyDescriptor dependencyDescriptor) {
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

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
