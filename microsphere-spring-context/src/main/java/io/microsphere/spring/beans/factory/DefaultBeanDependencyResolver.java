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

import io.microsphere.collection.CollectionUtils;
import io.microsphere.collection.SetUtils;
import io.microsphere.lang.function.ThrowableAction;
import io.microsphere.spring.beans.factory.filter.ResolvableDependencyTypeFilter;
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
import org.springframework.util.StopWatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.collection.MapUtils.ofEntry;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.reflect.MemberUtils.isStatic;
import static io.microsphere.spring.util.BeanDefinitionUtils.resolveBeanType;
import static io.microsphere.util.ClassLoaderUtils.loadClass;
import static java.lang.InheritableThreadLocal.withInitial;
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

    private static final ThreadLocal<Set<Member>> resolvedBeanMembersHolder = withInitial(SetUtils::newLinkedHashSet);

    private final DefaultListableBeanFactory beanFactory;

    private final ClassLoader classLoader;

    private final ResolvableDependencyTypeFilter resolvableDependencyTypeFilter;

    private final InjectionPointDependencyResolvers resolvers;

    private final List<SmartInstantiationAwareBeanPostProcessor> smartInstantiationAwareBeanPostProcessors;

    private final ExecutorService executorService;

    public DefaultBeanDependencyResolver(BeanFactory bf, ExecutorService executorService) {
        isInstanceOf(DefaultListableBeanFactory.class, bf, "The BeanFactory is not an instance of DefaultListableBeanFactory");
        this.beanFactory = (DefaultListableBeanFactory) bf;
        this.classLoader = this.beanFactory.getBeanClassLoader();
        this.resolvableDependencyTypeFilter = new ResolvableDependencyTypeFilter(beanFactory);
        this.resolvers = new InjectionPointDependencyResolvers(beanFactory);
        this.smartInstantiationAwareBeanPostProcessors = getSmartInstantiationAwareBeanPostProcessors(beanFactory);
        this.executorService = executorService;
    }

    @Override
    public Map<String, Set<String>> resolve(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (beanFactory != bf) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return emptyMap();
        }

        StopWatch stopWatch = new StopWatch("BeanDependencyResolver");

        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = getEligibleBeanDefinitionsMap(beanFactory, stopWatch);

        // Pre-Process Bean Classes for BeanDefinitions
        preProcessLoadBeanClasses(eligibleBeanDefinitionsMap, stopWatch);

        // No Bean(name) conflict here, thus it could be HashMap since Java 8
        Map<String, Set<String>> dependentBeanNamesMap = resolveDependentBeanNamesMap(eligibleBeanDefinitionsMap, stopWatch);

        flattenDependentBeanNamesMap(dependentBeanNamesMap, stopWatch);

        clearResolvedBeanMembers();

        logger.info(stopWatch.toString());

        return dependentBeanNamesMap;
    }

    private Map<String, Set<String>> resolveDependentBeanNamesMap(Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap, StopWatch stopWatch) {
        stopWatch.start("resolveDependentBeanNamesMap");

        int beansCount = eligibleBeanDefinitionsMap.size();
        final Map<String, Set<String>> dependentBeanNamesMap = newHashMap(beansCount);

        CompletionService<Map.Entry<String, Set<String>>> completionService = new ExecutorCompletionService<>(this.executorService);

        for (Map.Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            completionService.submit(() -> {
                String beanName = entry.getKey();
                RootBeanDefinition beanDefinition = entry.getValue();
                Set<String> dependentBeanNames = resolve(beanName, beanDefinition, beanFactory);
                return ofEntry(beanName, dependentBeanNames);
            });
        }

        for (int i = 0; i < beansCount; i++) {
            ThrowableAction.execute(() -> {
                Future<Map.Entry<String, Set<String>>> future = completionService.take();
                Map.Entry<String, Set<String>> entry = future.get();
                String beanName = entry.getKey();
                Set<String> dependentBeanNames = entry.getValue();
                dependentBeanNamesMap.put(beanName, dependentBeanNames);
            });
        }

        stopWatch.stop();
        return dependentBeanNamesMap;
    }

    private void preProcessLoadBeanClasses(Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap, StopWatch stopWatch) {
        stopWatch.start("preProcessLoadBeanClasses");

        ClassLoader classLoader = this.classLoader;
        for (Map.Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            String beanName = entry.getKey();
            RootBeanDefinition beanDefinition = entry.getValue();
            preProcessLoadBeanClass(beanName, beanDefinition, eligibleBeanDefinitionsMap, classLoader);
        }
        awaitTasksCompleted();

        stopWatch.stop();
    }

    private void awaitTasksCompleted() {
        while (execute(() -> executorService.awaitTermination(10, TimeUnit.MILLISECONDS))) {
        }
    }

    private void preProcessLoadBeanClass(String beanName, RootBeanDefinition beanDefinition, Map<String, RootBeanDefinition> beanDefinitionsMap,
                                         ClassLoader classLoader) {
        String beanClassName = beanDefinition.getBeanClassName();
        if (beanClassName == null) {
            if (beanDefinition.getResolvedFactoryMethod() == null) {
                String factoryBeanName = beanDefinition.getFactoryBeanName();
                if (factoryBeanName != null) {
                    RootBeanDefinition factoryBeanDefinition = getMergedBeanDefinition(factoryBeanName, beanDefinitionsMap);
                    preProcessLoadBeanClass(factoryBeanName, factoryBeanDefinition, beanDefinitionsMap, classLoader);
                }
            }
        } else {
            executorService.execute(() -> {
                Class beanClass = loadClass(beanClassName, classLoader, true);
                beanDefinition.setBeanClass(beanClass);
                if (logger.isDebugEnabled()) {
                    logger.debug("The bean[name : '{}'] class[name : '{}'] was loaded", beanName, beanClassName);
                }
            });
        }
    }

    private RootBeanDefinition getMergedBeanDefinition(String beanName, Map<String, RootBeanDefinition> beanDefinitionsMap) {
        RootBeanDefinition beanDefinition = beanDefinitionsMap.get(beanName);
        if (beanDefinition == null) {
            beanDefinition = (RootBeanDefinition) this.beanFactory.getMergedBeanDefinition(beanName);
        }
        return beanDefinition;
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

    private void flattenDependentBeanNamesMap(Map<String, Set<String>> dependentBeanNamesMap, StopWatch stopWatch) {
        stopWatch.start("flattenDependentBeanNamesMap");

        for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            Set<String> dependentBeanNames = entry.getValue();
            if (dependentBeanNames.isEmpty()) { // No Dependent bean name
                continue;
            }
            String beanName = entry.getKey();
            Set<String> flattenDependentBeanNames = new LinkedHashSet<>(dependentBeanNames.size() * 2);
            // flat
            flatDependentBeanNames(beanName, dependentBeanNamesMap, flattenDependentBeanNames);
            // Replace flattenDependentBeanNames to dependentBeanNames
            entry.setValue(flattenDependentBeanNames);
        }

        Set<String> nonRootBeanNames = new LinkedHashSet<>();
        for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            String beanName = entry.getKey();
            Set<String> dependentBeanNames = entry.getValue();
            for (String dependentBeanName : dependentBeanNames) {
                Set<String> nestedDependentBeanNames = dependentBeanNamesMap.get(dependentBeanName);
                if (CollectionUtils.isNotEmpty(nestedDependentBeanNames) && !dependentBeanNames.containsAll(nestedDependentBeanNames)) {
                    nonRootBeanNames.add(beanName);
                    break;
                }
            }
        }

        for (String nonRootBeanName : nonRootBeanNames) {
            if (dependentBeanNamesMap.remove(nonRootBeanName) != null) {
                logger.debug("Non Root Bean name was removed : {}", nonRootBeanName);
            }
        }

        logDependentBeanNames(dependentBeanNamesMap);

        stopWatch.stop();
    }

    private void logDependentBeanNames(Map<String, Set<String>> dependentBeanNamesMap) {
        if (logger.isDebugEnabled()) {
            for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
                logger.debug("The bean : '{}' <- bean dependencies : {}", entry.getKey(), entry.getValue());
            }
        }
    }

    private void flatDependentBeanNames(String beanName, Map<String, Set<String>> dependentBeanNamesMap, Set<String> flattenDependentBeanNames) {
        Set<String> dependentBeanNames = retrieveDependentBeanNames(beanName, dependentBeanNamesMap);
        if (dependentBeanNames.isEmpty()) {
            return;
        }

        // remove self-reference
        dependentBeanNames.remove(beanName);

        for (String dependentBeanName : dependentBeanNames) {
            if (flattenDependentBeanNames.add(dependentBeanName)) {
                flatDependentBeanNames(dependentBeanName, dependentBeanNamesMap, flattenDependentBeanNames);
            }
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

    private Set<String> resolveDependentBeanNames(String beanName, RootBeanDefinition beanDefinition, DefaultListableBeanFactory beanFactory) {

        Set<String> dependentBeanNames = new LinkedHashSet<>();
        // Resolve the dependent bean names from BeanDefinition
        resolveBeanDefinitionDependentBeanNames(beanDefinition, dependentBeanNames);
        // Resolve the dependent bean names from constructors' parameters
        resolveConstructionParametersDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        // Resolve the dependent bean names from injection points
        resolveInjectionPointsDependentBeanNames(beanName, beanDefinition, beanFactory, dependentBeanNames);
        // remove self
        dependentBeanNames.remove(beanName);
        // remove the names of beans that had been initialized stored into DefaultListableBeanFactory.singletonObjects
        removeReadyBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private void resolveInjectionPointsDependentBeanNames(String beanName, RootBeanDefinition beanDefinition, DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {

        ClassLoader classLoader = beanFactory.getBeanClassLoader();

        Class beanClass = resolveBeanClass(beanDefinition, classLoader);
        if (beanClass == null) {
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

    private void resolveMethodParametersDependentBeanNames(String beanName, Class beanClass, DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {

        Class<?> targetClass = beanClass;
        do {
            doWithLocalMethods(targetClass, method -> {
                if (isStatic(method)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Injection Point[bean : '{}' , class : {}] is not supported on static method : {}", beanName, method.getDeclaringClass().getName(), method);
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
                        if (isBeanMemberResolved(method)) {
                            logger.debug("The beans'[name : '{}'] method has been resolved : {}", beanName, method);
                        } else {
                            resolvers.resolve(method, beanFactory, dependentBeanNames);
                            addResolvedBeanMember(method);
                        }
                    }
                }
            });

            targetClass = targetClass.getSuperclass();

        } while (targetClass != null && targetClass != Object.class);

    }

    private void resolveFieldDependentBeanNames(String beanName, Class beanClass, DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Class<?> targetClass = beanClass;
        do {
            doWithLocalFields(targetClass, field -> {
                if (isStatic(field)) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("The Injection Point[bean : '{}' , class : {}] is not supported on static field : {}", beanName, field.getDeclaringClass().getName(), field);
                    }
                    return;
                }
                if (isBeanMemberResolved(field)) {
                    logger.debug("The beans'[name : '{}'] field has been resolved : {}", beanName, field);
                } else {
                    resolvers.resolve(field, beanFactory, dependentBeanNames);
                    addResolvedBeanMember(field);
                }
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

    private void resolveConstructionParametersDependentBeanNames(String beanName, RootBeanDefinition beanDefinition, DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Method factoryMethod = beanDefinition.getResolvedFactoryMethod();

        if (factoryMethod == null) { // The bean-class Definition
            Class<?> beanClass = resolveBeanClass(beanDefinition, beanFactory.getBeanClassLoader());

            Constructor[] constructors = resolveConstructors(beanName, beanClass);
            int constructorsLength = constructors.length;
            if (constructorsLength != 1) {
                logger.warn("Why the Bean[name : '{}' , class : {} ] has {} constructors?", beanName, beanClass, constructorsLength);
            } else {
                Constructor constructor = constructors[0];
                if (isBeanMemberResolved(constructor)) {
                    logger.debug("The beans'[name : '{}'] constructor has been resolved : {}", beanName, constructor);
                } else {
                    resolvers.resolve(constructor, beanFactory, dependentBeanNames);
                    addResolvedBeanMember(constructor);
                }
            }
        } else { // the @Bean or customized Method Definition
            if (isBeanMemberResolved(factoryMethod)) {
                logger.debug("The beans'[name : '{}'] factory-method has been resolved : {}", beanName, factoryMethod);
            } else {
                resolvers.resolve(factoryMethod, beanFactory, dependentBeanNames);
                addResolvedBeanMember(factoryMethod);
            }
        }

    }

    private Constructor[] resolveConstructors(String beanName, Class<?> beanClass) {
        Constructor[] constructors = null;
        if (!beanClass.isInterface()) {
            List<SmartInstantiationAwareBeanPostProcessor> processors = this.smartInstantiationAwareBeanPostProcessors;
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

    private Class<?> resolveBeanClass(RootBeanDefinition beanDefinition, @Nullable ClassLoader classLoader) {
        return resolveBeanType(beanDefinition, classLoader);
    }

    private Map<String, RootBeanDefinition> getEligibleBeanDefinitionsMap(DefaultListableBeanFactory beanFactory, StopWatch stopWatch) {
        stopWatch.start("getEligibleBeanDefinitionsMap");

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

        stopWatch.stop();
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
        if (beanDefinition != null && !beanDefinition.isAbstract() && beanDefinition.isSingleton() && !beanDefinition.isLazyInit() && beanDefinition instanceof RootBeanDefinition) {
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

    private static Set<Member> getResolvedBeanMembers() {
        return resolvedBeanMembersHolder.get();
    }

    private static void addResolvedBeanMember(Member resolvedBeanMember) {
        Set<Member> resolvedBeanMembers = getResolvedBeanMembers();
        resolvedBeanMembers.add(resolvedBeanMember);
    }

    private static boolean isBeanMemberResolved(Member member) {
        Set<Member> resolvedBeanMembers = getResolvedBeanMembers();
        return resolvedBeanMembers.contains(member);
    }

    private static void clearResolvedBeanMembers() {
        resolvedBeanMembersHolder.remove();
    }
}
