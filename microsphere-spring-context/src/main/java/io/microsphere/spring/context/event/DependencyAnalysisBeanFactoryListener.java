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

import io.microsphere.annotation.Nullable;
import io.microsphere.filter.Filter;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.filter.ResolvableDependencyTypeFilter;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.DependencyDescriptor;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.AutowireCandidateResolver;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Supplier;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.Lists.ofList;
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClasses;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.getInstanceSupplier;
import static io.microsphere.spring.core.MethodParameterUtils.forParameter;
import static io.microsphere.util.ArrayUtils.EMPTY_PARAMETER_ARRAY;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.springframework.util.ClassUtils.resolveClassName;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Dependency Analysis {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanInitializer
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanFactoryListeners
 * @see BeanFactoryListener
 * @see BeanFactoryListenerAdapter
 * @see ConfigurableListableBeanFactory
 * @see DefaultListableBeanFactory
 * @since 1.0.0
 */
public class DependencyAnalysisBeanFactoryListener implements BeanFactoryListenerAdapter {

    private static final Logger logger = getLogger(DependencyAnalysisBeanFactoryListener.class);

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {

        DefaultListableBeanFactory beanFactory = asDefaultListableBeanFactory(bf);

        Filter<Class<?>> resolvableDependencyTypeFilter = new ResolvableDependencyTypeFilter(beanFactory);

        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        List<BeanDefinitionHolder> beanDefinitionHolders = getNonLazyInitSingletonMergedBeanDefinitionHolders(bf);
        int beansCount = beanDefinitionHolders.size();
        Map<String, Set<String>> dependentBeanNamesMap = new HashMap<>(beansCount);
        for (int i = 0; i < beansCount; i++) {
            BeanDefinitionHolder beanDefinitionHolder = beanDefinitionHolders.get(i);
            Set<String> dependentBeanNames = resolveDependentBeanNames(beanDefinitionHolder,
                    resolvableDependencyTypeFilter, beanDefinitionHolders, beanFactory);
            dependentBeanNamesMap.put(beanDefinitionHolder.getBeanName(), dependentBeanNames);
        }
        flattenDependentBeanNamesMap(dependentBeanNamesMap);
    }

    private void flattenDependentBeanNamesMap(Map<String, Set<String>> dependentBeanNamesMap) {
        Map<String, Set<String>> dependenciesMap = new LinkedHashMap<>(dependentBeanNamesMap.size());
        for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
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
        for (Entry<String, Set<String>> entry : dependenciesMap.entrySet()) {
            String dependentBeanName = entry.getKey();
            dependentBeanNamesMap.remove(dependentBeanName);
            logDependenciesTrace(dependentBeanName, entry);
        }

        logDependentTrace(dependentBeanNamesMap);
    }


    private void logDependenciesTrace(String dependentBeanName, Entry<String, Set<String>> dependencies) {
        if (logger.isTraceEnabled()) {
            logger.trace("The bean dependency : '{}' -> beans : {}", dependentBeanName, dependencies.getValue());
        }
    }


    private void logDependentTrace(Map<String, Set<String>> dependentBeanNamesMap) {
        if (logger.isTraceEnabled()) {
            for (Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
                logger.trace("The bean : '{}' <- bean dependencies : {}", entry.getKey(), entry.getValue());
            }
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
                                                  Filter<Class<?>> resolvableDependencyTypeFilter,
                                                  List<BeanDefinitionHolder> beanDefinitionHolders,
                                                  DefaultListableBeanFactory beanFactory) {
        String beanName = beanDefinitionHolder.getBeanName();
        RootBeanDefinition beanDefinition = (RootBeanDefinition) beanDefinitionHolder.getBeanDefinition();


        Set<String> dependentBeanNames = new LinkedHashSet<>();
        List<String> beanDefinitionDependentBeanNames = resolveBeanDefinitionDependentBeanNames(beanDefinition);
        List<String> parameterDependentBeanNames = resolveParameterDependentBeanNames(beanName, beanDefinition, resolvableDependencyTypeFilter, beanDefinitionHolders, beanFactory);
        List<String> injectedBeanNames = resolveInjectionDependentBeanNames(beanName, beanDefinition, resolvableDependencyTypeFilter, beanDefinitionHolder, beanFactory);

        dependentBeanNames.addAll(beanDefinitionDependentBeanNames);
        dependentBeanNames.addAll(parameterDependentBeanNames);
        dependentBeanNames.addAll(injectedBeanNames);

        // remove self
        dependentBeanNames.remove(beanName);
        // remove the names of beans that had been initialized stored into DefaultListableBeanFactory.singletonObjects
        removeInitializedBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private List<String> resolveInjectionDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                            Filter<Class<?>> resolvableDependencyTypeFilter,
                                                            BeanDefinitionHolder beanDefinitionHolder,
                                                            DefaultListableBeanFactory beanFactory) {
        List<String> injectedBeanNames = newLinkedList();
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
        // the bean names from RootBeanDefinitions' depends-on
        List<String> dependsOnBeanNames = getDependsOnBeanNames(beanDefinition);

        // the bean names from RootBeanDefinitions that were declared on XML elements,
        // e.g <ref> or <bean>
        List<String> refBeanNames = getRefBeanNames(beanDefinition);

        int size = dependsOnBeanNames.size() + refBeanNames.size();

        if (size < 1) {
            return emptyList();
        }

        List<String> dependentBeanNames = newArrayList(size);
        dependentBeanNames.addAll(dependsOnBeanNames);
        dependentBeanNames.addAll(refBeanNames);
        return dependentBeanNames;
    }

    private List<String> getDependsOnBeanNames(RootBeanDefinition beanDefinition) {
        String[] dependsOn = beanDefinition.getDependsOn();
        return isEmpty(dependsOn) ? emptyList() : asList(dependsOn);
    }

    private List<String> getRefBeanNames(RootBeanDefinition beanDefinition) {
        MutablePropertyValues mutablePropertyValues = beanDefinition.getPropertyValues();
        PropertyValue[] propertyValues = mutablePropertyValues.getPropertyValues();
        int propertyValuesLength = propertyValues.length;
        if (propertyValuesLength < 1) {
            return emptyList();
        }

        List<String> dependentBeanNames = newLinkedList();

        for (int i = 0; i < propertyValuesLength; i++) {
            PropertyValue propertyValue = propertyValues[i];
            Object value = propertyValue.getValue();
            if (value instanceof BeanReference) {
                BeanReference beanReference = (BeanReference) value;
                String beanName = beanReference.getBeanName();
                dependentBeanNames.add(beanName);
            }
        }
        return dependentBeanNames;
    }

    private List<String> resolveParameterDependentBeanNames(String beanName,
                                                            RootBeanDefinition beanDefinition,
                                                            Filter<Class<?>> resolvableDependencyTypeFilter,
                                                            List<BeanDefinitionHolder> beanDefinitionHolders,
                                                            DefaultListableBeanFactory beanFactory) {
        Parameter[] parameters = getParameters(beanName, beanDefinition, beanFactory);

        int parametersLength = parameters.length;
        if (parametersLength < 1) {
            return emptyList();
        }

        List<String> dependentBeanNames = newArrayList(parametersLength);

        for (int i = 0; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            Class<?> dependentType = resolveDependentType(parameter);
            if (resolvableDependencyTypeFilter.accept(dependentType)) {
                continue;
            }
            List<String> beanNames = resolveDependentBeanNames(parameter, dependentType, beanFactory);
            dependentBeanNames.addAll(beanNames);
        }

        return dependentBeanNames;
    }

    private List<String> resolveDependentBeanNames(Parameter parameter, Class<?> dependentType, DefaultListableBeanFactory beanFactory) {
        String dependentBeanName = resolveSuggestedDependentBeanName(parameter, beanFactory);
        if (dependentBeanName == null) {
            String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
            return asList(beanNames);
        } else {
            return ofList(dependentBeanName);
        }
    }

    private String resolveSuggestedDependentBeanName(Parameter parameter, DefaultListableBeanFactory beanFactory) {
        AutowireCandidateResolver autowireCandidateResolver = beanFactory.getAutowireCandidateResolver();
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    private Class<?> resolveDependentType(Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        Class parameterRawType = parameter.getType();
        Class dependentType = parameterRawType;
        if (isParameterizedType(parameterType)) {
            List<Class> arguments = resolveActualTypeArgumentClasses(parameterType, parameterRawType);
            int argumentsSize = arguments.size();
            if (argumentsSize > 0) {
                // Last argument
                dependentType = arguments.get(argumentsSize - 1);
            }
        }
        return dependentType;
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

    private List<BeanDefinitionHolder> getNonLazyInitSingletonMergedBeanDefinitionHolders(ConfigurableListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        int beansCount = beanNames.length;
        List<BeanDefinitionHolder> beanDefinitionHolders = newArrayList(beansCount);
        for (int i = 0; i < beansCount; i++) {
            String beanName = beanNames[i];
            if (beanFactory.containsSingleton(beanName)) {
                logger.trace("The Bean[name : '{}'] is ready", beanName);
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
                resolveClassName(beanDefinition.getBeanClassName(), classLoader);
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
            Supplier<?> instanceSupplier = getInstanceSupplier(rootBeanDefinition);
            return instanceSupplier == null || instanceSupplier.get() == null;
        }
        return false;
    }
}
