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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArguments;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static org.springframework.core.MethodParameter.forParameter;

/**
 * Abstract {@link InjectionPointDependencyResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class AbstractInjectionPointDependencyResolver implements InjectionPointDependencyResolver {

    private final Logger logger = LoggerFactory.getLogger(getClass());

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
            logger.debug("The no-argument method[{}] will be ignored", method);
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
            logger.debug("The no-argument constructor[{}] will be ignored", constructor);
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

    protected String resolveDependentBeanNameByName(Field field, ConfigurableListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor, autowireCandidateResolver);
    }

    protected String resolveDependentBeanNameByName(Parameter parameter, ConfigurableListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = getAutowireCandidateResolver(beanFactory);
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        return resolveDependentBeanNameByName(dependencyDescriptor, autowireCandidateResolver);
    }

    protected String resolveDependentBeanNameByName(DependencyDescriptor dependencyDescriptor,
                                                    AutowireCandidateResolver autowireCandidateResolver) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    protected void resolveDependentBeanNamesByType(Supplier<Type> typeSupplier, ConfigurableListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> dependentType = resolveDependentType(typeSupplier);
        String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
        for (int i = 0; i < beanNames.length; i++) {
            String beanName = beanNames[i];
            dependentBeanNames.add(beanName);
        }
    }

    protected AutowireCandidateResolver getAutowireCandidateResolver(ConfigurableListableBeanFactory beanFactory) {
        DefaultListableBeanFactory dbf = asDefaultListableBeanFactory(beanFactory);
        return dbf == null ? null : dbf.getAutowireCandidateResolver();
    }

    private Class<?> resolveDependentType(Supplier<Type> typeSupplier) {
        Type type = typeSupplier.get();
        return resolveDependentType(type);
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
