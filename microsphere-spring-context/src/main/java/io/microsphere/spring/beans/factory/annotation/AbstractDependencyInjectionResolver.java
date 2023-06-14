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

import io.microsphere.spring.beans.factory.DependencyInjectionResolver;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static org.springframework.core.MethodParameter.forParameter;

/**
 * Abstract {@link DependencyInjectionResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractDependencyInjectionResolver implements DependencyInjectionResolver {

    @Override
    public void resolve(Field field, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        String dependentBeanName = resolveSuggestedDependentBeanName(field, beanFactory);
        if (dependentBeanName == null) {
            Class<?> dependentType = resolveDependentType(field);
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
        String dependentBeanName = resolveSuggestedDependentBeanName(parameter, beanFactory);
        if (dependentBeanName == null) {
            Class<?> dependentType = resolveDependentType(parameter);
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

    protected String resolveSuggestedDependentBeanName(Field field, ConfigurableListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveSuggestedDependentBeanName(dependencyDescriptor, autowireCandidateResolver);
    }

    protected String resolveSuggestedDependentBeanName(Parameter parameter, ConfigurableListableBeanFactory beanFactory) {
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

    protected Class<?> resolveDependentType(Field field) {
        Type type = field.getGenericType();
        return resolveDependentType(type);
    }

    protected Class<?> resolveDependentType(Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        return resolveDependentType(parameterType);
    }

    protected Class<?> resolveDependentType(Type type) {
        Class klass = asClass(type);
        Class dependentType = klass;
        if (isParameterizedType(type)) {
            // ObjectProvider<SomeBean> == arguments == [SomeBean.class]
            // ObjectFactory<SomeBean> == arguments == [SomeBean.class]
            // Optional<SomeBean> == arguments == [SomeBean.class]
            // javax.inject.Provider<SomeBean> == arguments == [SomeBean.class]
            // Map<String,SomeBean> == arguments == [SomeBean.class]
            // List<SomeBean> == arguments= [SomeBean.class]
            // Set<SomeBean> == arguments= [SomeBean.class]
            List<Type> arguments = resolveActualTypeArguments(type, klass);
            int argumentsSize = arguments.size();
            if (argumentsSize > 0) {
                // Last argument
                Type argumentType = arguments.get(argumentsSize - 1);
                Class<?> argumentClass = asClass(argumentType);
                if (argumentClass == null) {
                    dependentType = resolveDependentType(argumentType);
                } else {
                    if (argumentClass.isArray()) {
                        return argumentClass.getComponentType();
                    } else {
                        return argumentClass;
                    }
                }
            }
        }
        return dependentType;
    }
}
