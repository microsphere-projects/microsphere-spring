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

import io.microsphere.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.BeanReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.SmartInstantiationAwareBeanPostProcessor;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.util.ArrayUtils.EMPTY_PARAMETER_ARRAY;
import static io.microsphere.util.ClassUtils.resolveClass;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.springframework.core.BridgeMethodResolver.findBridgedMethod;
import static org.springframework.core.BridgeMethodResolver.isVisibilityBridgeMethodPair;
import static org.springframework.util.Assert.isInstanceOf;
import static org.springframework.util.ClassUtils.getMostSpecificMethod;
import static org.springframework.util.ObjectUtils.isEmpty;
import static org.springframework.util.ReflectionUtils.doWithLocalFields;
import static org.springframework.util.ReflectionUtils.doWithLocalMethods;

/**
 * Default {@link BeanDependencyResolver}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class DefaultBeanDependencyResolver implements BeanDependencyResolver {

    private static final Logger logger = LoggerFactory.getLogger(DefaultBeanDependencyResolver.class);

    private final DefaultListableBeanFactory beanFactory;

    private final InjectionPointDependencyResolvers resolvers;

    public DefaultBeanDependencyResolver(BeanFactory bf) {
        isInstanceOf(DefaultListableBeanFactory.class, bf, "The BeanFactory is not an instance of DefaultListableBeanFactory");
        this.beanFactory = (DefaultListableBeanFactory) bf;
        this.resolvers = new InjectionPointDependencyResolvers(beanFactory);
    }

    @Override
    public Map<String, Set<String>> resolve(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (beanFactory != bf) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return emptyMap();
        }
        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = getEligibleBeanDefinitionsMap(beanFactory);
        int beansCount = eligibleBeanDefinitionsMap.size();
        Map<String, Set<String>> dependentBeanNamesMap = new HashMap<>(beansCount);
        for (Map.Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            String beanName = entry.getKey();
            RootBeanDefinition beanDefinition = entry.getValue();
            Set<String> dependentBeanNames = resolve(beanName, beanDefinition, beanFactory);
            dependentBeanNamesMap.put(beanName, dependentBeanNames);
        }

        flattenDependentBeanNamesMap(dependentBeanNamesMap);

        return dependentBeanNamesMap;
    }

    @Override
    public Set<String> resolve(String beanName, RootBeanDefinition mergedBeanDefinition, ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (beanFactory != bf) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return emptySet();
        }
        return resolveDependentBeanNames(beanName, mergedBeanDefinition, beanFactory);
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
        for (Map.Entry<String, Set<String>> entry : dependenciesMap.entrySet()) {
            String dependentBeanName = entry.getKey();
            dependentBeanNamesMap.remove(dependentBeanName);
            logDependencies(dependentBeanName, entry);
        }

        logDependentBeanNames(dependentBeanNamesMap);
    }

    private void logDependencies(String dependentBeanName, Map.Entry<String, Set<String>> dependencies) {
        if (logger.isDebugEnabled()) {
            logger.debug("The bean dependency : '{}' -> beans : {}", dependentBeanName, dependencies.getValue());
        }
    }

    private void logDependentBeanNames(Map<String, Set<String>> dependentBeanNamesMap) {
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
                logger.debug("The bean : '{}' <- bean dependencies : {}", entry.getKey(), entry.getValue());
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

    private Set<String> resolveDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                  DefaultListableBeanFactory beanFactory) {

        Set<String> dependentBeanNames = new LinkedHashSet<>();
        // Resolve the dependent bean names from BeanDefinition
        resolveBeanDefinitionDependentBeanNames(beanDefinition, dependentBeanNames);
        // Resolve the dependent bean names from constructors' parameters
        resolveConstructorParametersDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        // Resolve the dependent bean names from injection points
        resolveInjectionPointsDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        // remove self
        dependentBeanNames.remove(beanName);
        // remove the names of beans that had been initialized stored into DefaultListableBeanFactory.singletonObjects
        removeReadyBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private void resolveInjectionPointsDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                          DefaultListableBeanFactory beanFactory,
                                                          Set<String> dependentBeanNames) {

        String beanClassName = beanDefinition.getBeanClassName();
        ClassLoader classLoader = beanFactory.getBeanClassLoader();
        final Class beanClass;
        if (StringUtils.hasText(beanClassName)) {
            beanClass = ClassUtils.resolveClass(beanClassName, classLoader);
        } else {
            ResolvableType resolvableType = beanDefinition.getResolvableType();
            beanClass = resolvableType.resolve();
        }

        boolean isInterfaceBean = beanClass.isInterface();

        if (isInterfaceBean) {
            logger.debug("The resolved type of BeanDefinition : {}", beanClass.getName());
            return;
        }

        resolveFieldDependentBeanNames(beanName, beanClass, beanFactory, dependentBeanNames);

        resolveMethodParametersDependentBeanNames(beanName, beanClass, beanFactory, dependentBeanNames);


    }

    private void resolveMethodParametersDependentBeanNames(String beanName, Class beanClass,
                                                           DefaultListableBeanFactory beanFactory,
                                                           Set<String> dependentBeanNames) {

        Class<?> targetClass = beanClass;
        do {
            doWithLocalMethods(targetClass, method -> {
                if (isStatic(method)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("The Injection Point[bean : '{}' , class : {}] is not supported on static method : {}",
                                beanName, method.getDeclaringClass().getName(), method);
                    }
                    return;
                }

                int length = method.getParameterCount();
                if (length > 0) {
                    Method bridgedMethod = findBridgedMethod(method);
                    if (!isVisibilityBridgeMethodPair(method, bridgedMethod)) {
                        return;
                    }

                    if (method.equals(getMostSpecificMethod(method, beanClass))) {
                        resolvers.resolve(method, beanFactory, dependentBeanNames);
                    }
                }
            });

            targetClass = targetClass.getSuperclass();

        } while (targetClass != null && targetClass != Object.class);

    }

    private void resolveFieldDependentBeanNames(String beanName, Class beanClass, DefaultListableBeanFactory beanFactory,
                                                Set<String> dependentBeanNames) {
        Class<?> targetClass = beanClass;
        do {
            doWithLocalFields(targetClass, field -> {
                if (isStatic(field)) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("The Injection Point[bean : '{}' , class : {}] is not supported on static field : {}",
                                beanName, field.getDeclaringClass().getName(), field);
                    }
                    return;
                }
                resolvers.resolve(field, beanFactory, dependentBeanNames);
            });

            targetClass = targetClass.getSuperclass();

        } while (targetClass != null && targetClass != Object.class);
    }


    private void removeReadyBeanNames(Set<String> dependentBeanNames, DefaultListableBeanFactory beanFactory) {
        if (dependentBeanNames.isEmpty()) {
            return;
        }
        Iterator<String> iterator = dependentBeanNames.iterator();
        while (iterator.hasNext()) {
            String dependentBeanName = iterator.next();
            if (isBeanReady(dependentBeanName, beanFactory)) {
                logger.debug("The dependent bean name['{}'] is removed since it's ready!", dependentBeanName);
                iterator.remove();
            }
        }
    }

    private void resolveBeanDefinitionDependentBeanNames(RootBeanDefinition beanDefinition, Set<String> dependentBeanNames) {
        // the bean names from RootBeanDefinitions' depends-on
        List<String> dependsOnBeanNames = getDependsOnBeanNames(beanDefinition);

        // the bean names from RootBeanDefinitions that were declared on XML elements,
        // e.g <ref> or <bean>
        List<String> refBeanNames = getRefBeanNames(beanDefinition);
        // The factory-bean XML element or @Bean method
        String factoryBeanName = beanDefinition.getFactoryBeanName();

        boolean hasFactoryBean = factoryBeanName != null;

        int size = dependsOnBeanNames.size() + refBeanNames.size() + (hasFactoryBean ? 1 : 0);

        if (size < 1) {
            return;
        }

        dependentBeanNames.addAll(dependsOnBeanNames);
        dependentBeanNames.addAll(refBeanNames);
        if (hasFactoryBean) {
            dependentBeanNames.add(factoryBeanName);
        }
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

    private void resolveConstructorParametersDependentBeanNames(String beanName,
                                                                RootBeanDefinition beanDefinition,
                                                                DefaultListableBeanFactory beanFactory,
                                                                Set<String> dependentBeanNames) {
        Parameter[] parameters = getParameters(beanName, beanDefinition, beanFactory);

        int parametersLength = parameters.length;
        if (parametersLength < 1) {
            return;
        }

        for (int i = 0; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            resolvers.resolve(parameter, beanFactory, dependentBeanNames);
        }

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

    private Map<String, RootBeanDefinition> getEligibleBeanDefinitionsMap(DefaultListableBeanFactory beanFactory) {
        String[] beanNames = beanFactory.getBeanDefinitionNames();
        int beansCount = beanNames.length;
        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = newHashMap(beansCount);
        for (int i = 0; i < beansCount; i++) {
            String beanName = beanNames[i];
            if (isBeanReady(beanName, beanFactory)) {
                continue;
            }
            if (beanFactory.isCurrentlyInCreation(beanName)) {
                logger.debug("The Bean[name : '{}'] is creating currently", beanName);
                continue;
            }

            BeanDefinition beanDefinition = beanFactory.getMergedBeanDefinition(beanName);
            RootBeanDefinition eligibleBeanDefinition = getEligibleBeanDefinition(beanDefinition);
            if (eligibleBeanDefinition != null) {
                BeanDefinitionHolder beanDefinitionHolder = eligibleBeanDefinition.getDecoratedDefinition();
                if (beanDefinitionHolder == null) {
                    String[] aliases = beanFactory.getAliases(beanName);
                    beanDefinitionHolder = new BeanDefinitionHolder(beanDefinition, beanName, aliases);
                    eligibleBeanDefinition.setDecoratedDefinition(beanDefinitionHolder);
                }
                eligibleBeanDefinitionsMap.put(beanName, eligibleBeanDefinition);
            }
        }
        return eligibleBeanDefinitionsMap;
    }

    /**
     * Get the Eligible {@link BeanDefinition} that must be
     * <ul>
     *     <li>non-null</li>
     *     <li>{@link BeanDefinition#isAbstract() non-abstract}</li>
     *     <li>{@link BeanDefinition#isSingleton() singleton}</li>
     *     <li>{@link BeanDefinition#isLazyInit() non-lazy-init}</li>
     *     <li>{@link RootBeanDefinition}</li>
     *     <li>{@link AbstractBeanDefinition#getInstanceSupplier() No instance supplier}</li>
     * </ul>
     *
     * @param beanDefinition
     * @return <code>true</code> if the given {@link BeanDefinition} must be
     */
    private RootBeanDefinition getEligibleBeanDefinition(BeanDefinition beanDefinition) {
        if (beanDefinition != null
                && !beanDefinition.isAbstract()
                && beanDefinition.isSingleton()
                && !beanDefinition.isLazyInit()
                && beanDefinition instanceof RootBeanDefinition) {
            RootBeanDefinition rootBeanDefinition = (RootBeanDefinition) beanDefinition;
            return rootBeanDefinition.getInstanceSupplier() == null ? rootBeanDefinition : null;
        }
        return null;
    }

    private boolean isBeanReady(String beanName, DefaultListableBeanFactory beanFactory) {
        boolean ready = beanFactory.containsSingleton(beanName);
        if (ready && logger.isDebugEnabled()) {
            logger.debug("The Bean[name : '{}'] is ready in the BeanFactory[id : '{}']", beanName, beanFactory.getSerializationId());
        }
        return ready;
    }
}
