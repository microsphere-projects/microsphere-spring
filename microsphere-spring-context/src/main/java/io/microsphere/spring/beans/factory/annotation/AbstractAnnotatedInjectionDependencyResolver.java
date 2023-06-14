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
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClass;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClasses;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static org.springframework.core.MethodParameter.forParameter;

/**
 * Abstract {@link AnnotatedInjectionDependencyResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractAnnotatedInjectionDependencyResolver<A extends Annotation> implements AnnotatedInjectionDependencyResolver<A> {

    private final Class<A> annotationType;

    public AbstractAnnotatedInjectionDependencyResolver() {
        this.annotationType = resolveActualTypeArgumentClass(getClass(), AnnotatedInjectionDependencyResolver.class, 0);
    }

    public AbstractAnnotatedInjectionDependencyResolver(Class<A> annotationType) {
        this.annotationType = annotationType;
    }

    @Override
    public final Class<A> getAnnotationType() {
        return annotationType;
    }

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        A annotation = getAnnotation(field);
        if (annotation == null) {
            return;
        }
        String dependentBeanName = resolveSuggestedDependentBeanName(field, annotation, beanFactory);
        if (dependentBeanName == null) {
            Class<?> dependentType = resolveDependentType(field, annotation);
            String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
            for (String beanName : beanNames) {
                dependentBeanNames.add(beanName);
            }
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

    @Override
    public void resolve(Parameter parameter, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        A annotation = getAnnotation(parameter);
        if (annotation == null) {
            return;
        }
        String dependentBeanName = resolveSuggestedDependentBeanName(parameter, annotation, beanFactory);
        if (dependentBeanName == null) {
            Class<?> dependentType = resolveDependentType(parameter, annotation);
            String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
            for (String beanName : beanNames) {
                dependentBeanNames.add(beanName);
            }
        } else {
            dependentBeanNames.add(dependentBeanName);
        }
    }

    protected AutowireCandidateResolver getAutowireCandidateResolver(ConfigurableListableBeanFactory beanFactory) {
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        return dbf == null ? null : dbf.getAutowireCandidateResolver();
    }

    protected String resolveSuggestedDependentBeanName(Field field, A annotation, ConfigurableListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveSuggestedDependentBeanName(dependencyDescriptor, autowireCandidateResolver);
    }

    protected String resolveSuggestedDependentBeanName(Parameter parameter, A annotation, ConfigurableListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        return resolveSuggestedDependentBeanName(dependencyDescriptor, autowireCandidateResolver);
    }

    protected String resolveSuggestedDependentBeanName(DependencyDescriptor dependencyDescriptor,
                                                       AutowireCandidateResolver autowireCandidateResolver) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    protected Class<?> resolveDependentType(Field field, A annotation) {
        Type type = field.getGenericType();
        return resolveDependentType(type);
    }

    protected Class<?> resolveDependentType(Parameter parameter, A annotation) {
        Type parameterType = parameter.getParameterizedType();
        return resolveDependentType(parameterType);
    }

    protected Class<?> resolveDependentType(Type type) {
        Class klass = asClass(type);
        Class dependentType = klass;
        if (isParameterizedType(type)) {
            List<Class> arguments = resolveActualTypeArgumentClasses(type, klass);
            int argumentsSize = arguments.size();
            if (argumentsSize > 0) {
                // Last argument
                dependentType = arguments.get(argumentsSize - 1);
            }
        }
        return dependentType;
    }

}