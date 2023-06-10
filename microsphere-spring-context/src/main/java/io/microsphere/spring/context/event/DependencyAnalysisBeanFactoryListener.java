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
package io.microsphere.spring.context.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static io.microsphere.spring.util.FieldUtils.getFieldValue;
import static io.microsphere.util.ClassUtils.resolveClass;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.springframework.core.MethodParameter.forParameter;
import static org.springframework.core.ResolvableType.forMethodParameter;
import static org.springframework.core.ResolvableType.forType;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Dependency Analysis {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DependencyAnalysisBeanFactoryListener extends BeanFactoryListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(DependencyAnalysisBeanFactoryListener.class);

    private static final Parameter[] EMPTY_PARAMETER_ARRAY = new Parameter[0];

    @Nullable
    private static Class<?> javaxInjectProviderClass;

    static {
        try {
            javaxInjectProviderClass =
                    ClassUtils.forName("javax.inject.Provider", DependencyAnalysisBeanFactoryListener.class.getClassLoader());
        } catch (ClassNotFoundException ex) {
            // JSR-330 API not available - Provider interface simply not supported then.
            javaxInjectProviderClass = null;
        }
    }

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        if (!(bf instanceof DefaultListableBeanFactory)) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return;
        }
        DefaultListableBeanFactory beanFactory = (DefaultListableBeanFactory) bf;
        Set<Class<?>> resolvableDependencyTypes = getResolvableDependencyTypes(beanFactory);
        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        List<BeanDefinitionHolder> beanDefinitionHolders = getNonLazyInitSingletonMergedBeanDefinitionHolders(bf);
        int beansCount = beanDefinitionHolders.size();
        Map<String, Set<String>> dependentBeanNamesMap = new HashMap<>(beansCount);
        for (int i = 0; i < beansCount; i++) {
            BeanDefinitionHolder beanDefinitionHolder = beanDefinitionHolders.get(i);
            Set<String> dependentBeanNames = resolveDependentBeanNames(beanDefinitionHolder,
                    resolvableDependencyTypes, beanDefinitionHolders, beanFactory);
            dependentBeanNamesMap.put(beanDefinitionHolder.getBeanName(), dependentBeanNames);
        }
        flattenDependentBeanNamesMap(dependentBeanNamesMap);
        logger.info("dependentBeanNamesMap : {}", dependentBeanNamesMap);
    }

    private void flattenDependentBeanNamesMap(Map<String, Set<String>> dependentBeanNamesMap) {
        Map<String, Set<String>> dependenciesMap = new LinkedHashMap<>(dependentBeanNamesMap.size());
        for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            Set<String> dependentBeanNames = entry.getValue();
            if (dependentBeanNames.isEmpty()) { // No Dependent bean name
                continue;
            }
            String beanName = entry.getKey();
            Set<String> flattenDependentBeanNames = new LinkedHashSet<>(dependentBeanNames.size() * 2);
            // flat
            flatDependentBeanNames(beanName, dependentBeanNamesMap, dependenciesMap, flattenDependentBeanNames);
            // Replace flattenDependentBeanNames to dependentBeanNames
            entry.setValue(flattenDependentBeanNames);
        }

        // Remove the bean names that ware dependent by the requesting beans
        for (String nonRootBeanName : dependenciesMap.keySet()) {
            dependentBeanNamesMap.remove(nonRootBeanName);
        }

    }

    private void flatDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap,
                                        Map<String, Set<String>> dependenciesMap,
                                        Set<String> flattenDependentBeanNames) {
        Set<String> dependentBeanNames = retrieveDependentBeanNames(beanName, dependentBeanNamesMap);
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        for (String dependentBeanName : dependentBeanNames) {
            Set<String> dependencies = dependenciesMap.computeIfAbsent(dependentBeanName, k -> new LinkedHashSet<>());
            dependencies.add(beanName);
            flattenDependentBeanNames.add(dependentBeanName);
            flatDependentBeanNames(dependentBeanName, dependentBeanNamesMap, dependenciesMap, flattenDependentBeanNames);
        }
    }

    private Set<String> retrieveDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap) {
        Set<String> dependentBeanNames = dependentBeanNamesMap.get(beanName);
        if (dependentBeanNames == null) {
            dependentBeanNames = emptySet();
        } else {
            dependentBeanNames.remove(beanName);
        }
        return dependentBeanNames;
    }

    private Set<String> resolveDependentBeanNames(BeanDefinitionHolder beanDefinitionHolder,
                                                  Set<Class<?>> resolvableDependencyTypes,
                                                  List<BeanDefinitionHolder> beanDefinitionHolders,
                                                  DefaultListableBeanFactory beanFactory) {
        String beanName = beanDefinitionHolder.getBeanName();
        RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionHolder.getBeanDefinition();

        Set<String> dependentBeanNames = new LinkedHashSet<>();
        List<String> beanDefinitionDependentBeanNames = resolveBeanDefinitionDependentBeanNames(beanDefinition);
        List<String> parameterDependentBeanNames = resolveParameterDependentBeanNames(beanName, beanDefinition, resolvableDependencyTypes, beanDefinitionHolders, beanFactory);
        List<String> injectedBeanNames = resolveInjectedBeanNames(beanName, beanDefinition, resolvableDependencyTypes, beanDefinitionHolder, beanFactory);

        dependentBeanNames.addAll(beanDefinitionDependentBeanNames);
        dependentBeanNames.addAll(parameterDependentBeanNames);
        dependentBeanNames.addAll(injectedBeanNames);

        // remove self
        dependentBeanNames.remove(beanName);
        // remove the names of beans that had been initialized stored into DefaultListableBeanFactory.singletonObjects
        removeInitializedBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private List<String> resolveInjectedBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                  Set<Class<?>> resolvableDependencyTypes,
                                                  BeanDefinitionHolder beanDefinitionHolder,
                                                  DefaultListableBeanFactory beanFactory) {
        List<String> injectedBeanNames = new LinkedList<>();
        // TODO
        return injectedBeanNames;
    }

    private void removeInitializedBeanNames(Set<String> dependentBeanNames, DefaultListableBeanFactory beanFactory) {
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        Iterator<String> iterator = dependentBeanNames.iterator();
        while (iterator.hasNext()) {
            String dependentBeanName = iterator.next();
            if (beanFactory.containsSingleton(dependentBeanName)) {
                iterator.remove();
            }
        }
    }

    private List<String> resolveBeanDefinitionDependentBeanNames(RootBeanDefinition beanDefinition) {
        String[] dependsOn = beanDefinition.getDependsOn();
        return isEmpty(dependsOn) ? emptyList() : asList(dependsOn);
    }

    private List<String> resolveParameterDependentBeanNames(String beanName,
                                                            RootBeanDefinition beanDefinition,
                                                            Set<Class<?>> resolvableDependencyTypes,
                                                            List<BeanDefinitionHolder> beanDefinitionHolders,
                                                            DefaultListableBeanFactory beanFactory) {
        Parameter[] parameters = getParameters(beanName, beanDefinition, beanFactory);
        Map<Integer, Class<?>> parameterDependentTypesMap = getParameterDependentTypesMap(parameters, resolvableDependencyTypes);
        int size = parameterDependentTypesMap.size();
        if (size < 1) {
            return emptyList();
        }
        List<String> dependentBeanNames = new ArrayList<>(size);
        AutowireCandidateResolver autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        for (Map.Entry<Integer, Class<?>> entry : parameterDependentTypesMap.entrySet()) {
            Integer parameterIndex = entry.getKey();
            Parameter parameter = parameters[parameterIndex];
            MethodParameter methodParameter = forParameter(parameter);
            String dependentBeanName = resolveSuggestedDependentBeanName(methodParameter, autowireCandidateResolver);
            if (dependentBeanName == null) {
                // Class<?> parameterDependentType = entry.getValue();
                ResolvableType dependentType = forMethodParameter(methodParameter);
                // Resolve the bean names
                String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
                dependentBeanNames.addAll(asList(beanNames));
                // dependentBeanName = resolveDependentBeanName(beanName, beanDefinition, dependentType, beanDefinitionHolders, beanFactory);
            } else {
                dependentBeanNames.add(dependentBeanName);
            }
        }

        return dependentBeanNames;
    }

    private String resolveSuggestedDependentBeanName(MethodParameter methodParameter, AutowireCandidateResolver autowireCandidateResolver) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(methodParameter, true);
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    private String resolveDependentBeanName(String beanName,
                                            RootBeanDefinition beanDefinition,
                                            ResolvableType dependentType,
                                            List<BeanDefinitionHolder> beanDefinitionHolders,
                                            DefaultListableBeanFactory beanFactory) {
        String dependentBeanName = null;
        int size = beanDefinitionHolders.size();
        for (int i = 0; i < size; i++) {
            BeanDefinitionHolder beanDefinitionHolder = beanDefinitionHolders.get(i);
            String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
        }
        return dependentBeanName;
    }

    private Parameter[] getParameters(String beanName, RootBeanDefinition beanDefinition, DefaultListableBeanFactory beanFactory) {
        Method factoryMethod = beanDefinition.getResolvedFactoryMethod();

        Parameter[] parameters = null;

        if (factoryMethod == null) { // The bean-class Definition
            Class<?> beanClass = getBeanClass(beanDefinition, beanFactory.getBeanClassLoader());

            Constructor[] constructors = resolveConstructors(beanName, beanClass, beanFactory);
            int constructorsLength = constructors.length;
            if (constructorsLength != 1) {
                logger.warn("Why the Bean[name : '{}' , class : {} ] has {} constructors?", beanName, beanClass, constructorsLength);
                parameters = EMPTY_PARAMETER_ARRAY;
            } else {
                Constructor constructor = constructors[0];
                parameters = constructor.getParameters();
            }
        } else { // the @Bean or customized Method Definition
            parameters = factoryMethod.getParameters();
        }
        return parameters;
    }

    private Set<Class<?>> getResolvableDependencyTypes(DefaultListableBeanFactory beanFactory) {
        Map resolvableDependencies = getFieldValue(beanFactory, "resolvableDependencies", Map.class);
        return resolvableDependencies.keySet();
    }

    /**
     * @param parameters
     * @param resolvableDependencyTypes
     * @return the Map of dependency types whose key is the parameter index and value is the dependency type
     */
    private Map<Integer, Class<?>> getParameterDependentTypesMap(Parameter[] parameters, Set<Class<?>> resolvableDependencyTypes) {
        int parametersLength = parameters.length;
        if (parametersLength < 1) {
            return emptyMap();
        }
        Map<Integer, Class<?>> dependentTypesMap = new HashMap<>(parametersLength);
        for (int i = 0; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            Type parameterType = parameter.getParameterizedType();
            ResolvableType type = forType(parameterType);
            Class<?> rawType = type.resolve();
            Class dependentType = null;
            if (type.hasGenerics()) {
                if (Map.class.isAssignableFrom(rawType)) { // Bi-Type Container Wrapper
                    dependentType = type.getGeneric(1).resolve();
                } else if (Collection.class.isAssignableFrom(rawType)
                        || Objects.equals(javaxInjectProviderClass, rawType)
                        || ObjectFactory.class.equals(rawType)
                        || ObjectProvider.class.equals(rawType)
                ) {
                    dependentType = type.getGeneric(0).resolve();
                }
            } else {
                dependentType = rawType;
            }
            if (!isResolvableDependencyType(dependentType, resolvableDependencyTypes)) {
                dependentTypesMap.put(i, dependentType);
            }
        }
        return dependentTypesMap;
    }

    private boolean isResolvableDependencyType(Class<?> dependencyType, Set<Class<?>> resolvableDependencyTypes) {
        boolean result = false;
        for (Class<?> resolvableDependencyType : resolvableDependencyTypes) {
            if (ClassUtils.isAssignable(resolvableDependencyType, dependencyType)) {
                result = true;
                break;
            }
        }
        return result;
    }

    private List<BeanDefinitionHolder> getNonLazyInitSingletonMergedBeanDefinitionHolders(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        int beansCount = beanNames.length;
        List<BeanDefinitionHolder> beanDefinitionHolders = new ArrayList<>(beansCount);
        for (int i = 0; i < beansCount; i++) {
            String beanName = beanNames[i];
            if (beanFactory.containsSingleton(beanName)) {
                logger.debug("The Bean[name : '{}'] is ready", beanName);
                continue;
            }
            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            if (isEligibleBeanDefinition(beanDefinition)) {
                String[] aliases = beanFactory.getAliases(beanName);
                BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName, aliases);
                beanDefinitionHolders.add(beanDefinitionHolder);
            }
        }
        return beanDefinitionHolders;
    }

    private Constructor[] resolveConstructors(String beanName, Class<?> beanClass, ConfigurableListableBeanFactory beanFactory) {
        Constructor[] constructors = null;
        if (!beanClass.isInterface()) {
            List<SmartInstantiationAwareBeanPostProcessor> processors = getSmartInstantiationAwareBeanPostProcessors(beanFactory);
            for (SmartInstantiationAwareBeanPostProcessor processor : processors) {
                constructors = processor.determineCandidateConstructors(beanClass, beanName);
                if (constructors != null) {
                    break;
                }
            }
        }
        constructors = isEmpty(constructors) ? beanClass.getConstructors() : constructors;
        constructors = isEmpty(constructors) ? beanClass.getDeclaredConstructors() : constructors;
        return constructors;
    }

    private List<SmartInstantiationAwareBeanPostProcessor> getSmartInstantiationAwareBeanPostProcessors(ConfigurableListableBeanFactory beanFactory) {
        if (beanFactory instanceof DefaultListableBeanFactory) {
            DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) beanFactory;
            List<SmartInstantiationAwareBeanPostProcessor> processors = new LinkedList<>();
            List<BeanPostProcessor> beanPostProcessors = dbf.getBeanPostProcessors();
            for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
                if (beanPostProcessor instanceof SmartInstantiationAwareBeanPostProcessor) {
                    processors.add((SmartInstantiationAwareBeanPostProcessor) beanPostProcessor);
                }
            }
            return processors;
        } else {
            return emptyList();
        }

    }

    private Class<?> getBeanClass(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        return beanDefinition.hasBeanClass() ? beanDefinition.getBeanClass() :
                resolveClass(beanDefinition.getBeanClassName(), classLoader);
    }

    /**
     * @param beanDefinition
     * @return <code>true</code> if the given {@link BeanDefinition} must be
     * <ul>
     *     <li>non-null</li>
     *     <li>{@link BeanDefinition#isSingleton() singleton}</li>
     *     <li>{@link BeanDefinition#isLazyInit() non-lazy-init}</li>
     *     <li>{@link RootBeanDefinition}</li>
     *     <li>{@link AbstractBeanDefinition#getInstanceSupplier() No instance supplier}</li>
     * </ul>
     */
    private boolean isEligibleBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition != null
                && beanDefinition.isSingleton()
                && !beanDefinition.isLazyInit()
                && beanDefinition instanceof RootBeanDefinition) {
            RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
            return rootBeanDefinition.getInstanceSupplier() == null;
        }
        return false;
    }
}
