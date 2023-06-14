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

import io.microsphere.filter.Filter;
import io.microsphere.reflect.MemberUtils;
import io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionDependencyResolver;
import io.microsphere.spring.beans.factory.annotation.AnnotatedInjectionDependencyResolvers;
import io.microsphere.spring.beans.factory.filter.ResolvableDependencyTypeFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
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
import org.springframework.context.EnvironmentAware;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StopWatch;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.collection.MapUtils.newHashMap;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.reflect.TypeUtils.asClass;
import static io.microsphere.reflect.TypeUtils.isParameterizedType;
import static io.microsphere.reflect.TypeUtils.resolveActualTypeArgumentClasses;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.util.SpringFactoriesLoaderUtils.loadFactories;
import static io.microsphere.util.ArrayUtils.EMPTY_PARAMETER_ARRAY;
import static io.microsphere.util.ClassUtils.resolveClass;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.core.MethodParameter.forParameter;
import static org.springframework.util.ObjectUtils.isEmpty;

/**
 * Parallel {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ParallelBeanFactoryListener extends BeanFactoryListenerAdapter implements
        InitializingBean, BeanNameAware, EnvironmentAware, BeanFactoryAware {

    public static final String PARALLEL_TASKS_PROPERTY_NAME = "microsphere.spring.beans.process.parallel.tasks";

    private static final Logger logger = LoggerFactory.getLogger(ParallelBeanFactoryListener.class);

    private String beanName;

    private Environment environment;

    private int parallelTasks;

    private DefaultListableBeanFactory beanFactory;

    private AutowireCandidateResolver autowireCandidateResolver;

    private AnnotatedInjectionDependencyResolvers dependencyResolvers;

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (bf != beanFactory) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return;
        }

        Filter<Class<?>> resolvableDependencyTypeFilter = new ResolvableDependencyTypeFilter(beanFactory);

        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        Map<String, RootBeanDefinition> eligibleBeanDefinitionsMap = getEligibleBeanDefinitionsMap(beanFactory);
        int beansCount = eligibleBeanDefinitionsMap.size();
        Map<String, Set<String>> dependentBeanNamesMap = new HashMap<>(beansCount);
        for (Map.Entry<String, RootBeanDefinition> entry : eligibleBeanDefinitionsMap.entrySet()) {
            String beanName = entry.getKey();
            RootBeanDefinition beanDefinition = entry.getValue();
            Set<String> dependentBeanNames = resolveDependentBeanNames(beanName, beanDefinition,
                    resolvableDependencyTypeFilter, eligibleBeanDefinitionsMap, beanFactory);
            dependentBeanNamesMap.put(beanName, dependentBeanNames);
        }

        flattenDependentBeanNamesMap(dependentBeanNamesMap);

        processBeanInParallel(eligibleBeanDefinitionsMap, dependentBeanNamesMap, beanFactory);
    }

    private void processBeanInParallel(Map<String, RootBeanDefinition> beanDefinitionsMap,
                                       Map<String, Set<String>> dependentBeanNamesMap,
                                       DefaultListableBeanFactory beanFactory) {
        StopWatch stopWatch = new StopWatch("Bean-Process-In-Parallel");
        stopWatch.start();
        ExecutorService executorService = newFixedThreadPool(parallelTasks);
        CompletionService completionService = new ExecutorCompletionService(executorService);
        int tasks = 0;
        AtomicInteger completedTasks = new AtomicInteger(0);
        for (Map.Entry<String, Set<String>> dependentEntry : dependentBeanNamesMap.entrySet()) {
            String beanName = dependentEntry.getKey();
            Set<String> dependentBeanNames = dependentEntry.getValue();
            if (dependentBeanNames.isEmpty()) { // No Dependent Bean
                completionService.submit(() -> {
                    Object bean = null;
                    try {
                        bean = beanFactory.getBean(beanName);
                        logger.debug("The bean[name : '{}'] was created : {}", beanName, bean);
                    } finally {
                        completedTasks.incrementAndGet();
                    }
                    return bean;
                });
                logger.debug("The task to create bean[name : '{}'] is submitted", beanName);
                tasks++;
            }
        }

        while (tasks > completedTasks.getAndIncrement()) {
            execute(() -> executorService.awaitTermination(100, TimeUnit.MICROSECONDS));
        }

        executorService.shutdown();
        stopWatch.stop();
        logger.info(stopWatch.toString());
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
                                                  Filter<Class<?>> resolvableDependencyTypeFilter,
                                                  Map<String, RootBeanDefinition> beanDefinitionsMap,
                                                  DefaultListableBeanFactory beanFactory) {

        Set<String> dependentBeanNames = new LinkedHashSet<>();
        // Resolve the dependent bean names from BeanDefinition
        resolveBeanDefinitionDependentBeanNames(beanDefinition, dependentBeanNames);
        // Resolve the dependent bean names from parameters
        resolveParameterDependentBeanNames(beanName, beanDefinition, resolvableDependencyTypeFilter, beanFactory, dependentBeanNames);
        // Resolve the dependent bean names from injection points
        resolveInjectionDependentBeanNames(beanName, beanDefinition, resolvableDependencyTypeFilter, beanFactory, dependentBeanNames);
        // remove self
        dependentBeanNames.remove(beanName);
        // remove the names of beans that had been initialized stored into DefaultListableBeanFactory.singletonObjects
        removeReadyBeanNames(dependentBeanNames, beanFactory);

        return dependentBeanNames;
    }

    private void resolveInjectionDependentBeanNames(String beanName, RootBeanDefinition beanDefinition,
                                                    Filter<Class<?>> resolvableDependencyTypeFilter,
                                                    DefaultListableBeanFactory beanFactory,
                                                    Set<String> dependentBeanNames) {

        ResolvableType resolvableType = beanDefinition.getResolvableType();
        Class beanClass = resolvableType.resolve();

        ReflectionUtils.doWithFields(beanClass, field -> {
            dependencyResolvers.resolve(field,beanFactory,dependentBeanNames);
        }, field -> !MemberUtils.isStatic(field));


        ReflectionUtils.doWithMethods(beanClass, method -> {
            int length = method.getParameterCount();
            if (length > 0) {
                Parameter[] parameters = method.getParameters();
                for (int i = 0; i < length; i++) {
                    Parameter parameter = parameters[i];
                    dependencyResolvers.resolve(parameter,beanFactory,dependentBeanNames);
                }
            }

        }, method -> !MemberUtils.isStatic(method));

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

    private void resolveParameterDependentBeanNames(String beanName,
                                                    RootBeanDefinition beanDefinition,
                                                    Filter<Class<?>> resolvableDependencyTypeFilter,
                                                    DefaultListableBeanFactory beanFactory, Set<String> dependentBeanNames) {
        Parameter[] parameters = getParameters(beanName, beanDefinition, beanFactory);

        int parametersLength = parameters.length;
        if (parametersLength < 1) {
            return;
        }

        for (int i = 0; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            Class<?> dependentType = resolveDependentType(parameter);
            if (resolvableDependencyTypeFilter.accept(dependentType)) {
                continue;
            }
            List<String> beanNames = resolveDependentBeanNames(parameter, dependentType, beanFactory);
            dependentBeanNames.addAll(beanNames);
        }

    }

    private List<String> resolveDependentBeanNames(Parameter parameter, Class<?> dependentType, DefaultListableBeanFactory beanFactory) {
        String dependentBeanName = resolveSuggestedDependentBeanName(parameter);
        if (dependentBeanName == null) {
            String[] beanNames = beanFactory.getBeanNamesForType(dependentType, false, false);
            return asList(beanNames);
        } else {
            return singletonList(dependentBeanName);
        }
    }

    private String resolveSuggestedDependentBeanName(Field field) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(field, true, false);
        return resolveSuggestedDependentBeanName(dependencyDescriptor, autowireCandidateResolver);
    }

    private String resolveSuggestedDependentBeanName(Parameter parameter) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        DependencyDescriptor dependencyDescriptor = new DependencyDescriptor(forParameter(parameter), true, false);
        return resolveSuggestedDependentBeanName(dependencyDescriptor, autowireCandidateResolver);
    }

    private String resolveSuggestedDependentBeanName(DependencyDescriptor dependencyDescriptor,
                                                     AutowireCandidateResolver autowireCandidateResolver) {
        if (autowireCandidateResolver == null) {
            return null;
        }
        Object suggestedValue = autowireCandidateResolver.getSuggestedValue(dependencyDescriptor);
        return suggestedValue instanceof String ? (String) suggestedValue : null;
    }

    private Class<?> resolveDependentType(Parameter parameter) {
        Type parameterType = parameter.getParameterizedType();
        return resolveDependentType(parameterType);
    }

    private Class<?> resolveDependentType(Type type) {
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
            if (this.beanName.equals(beanName)) {
                logger.debug("The Current Bean[name : '{}'] should be ignored", beanName);
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

    private boolean isBeanReady(String beanName, DefaultListableBeanFactory beanFactory) {
        boolean ready = beanFactory.containsSingleton(beanName);
        if (ready && logger.isDebugEnabled()) {
            logger.debug("The Bean[name : '{}'] is ready in the BeanFactory[id : '{}']", beanName, beanFactory.getSerializationId());
        }
        return ready;
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

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.parallelTasks = environment.getProperty(PARALLEL_TASKS_PROPERTY_NAME, int.class, getDefaultParallelTasks());
    }

    private int getDefaultParallelTasks() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Math.max(1, availableProcessors);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asDefaultListableBeanFactory(beanFactory);
        if (this.beanFactory != null) {
            this.autowireCandidateResolver = this.beanFactory.getAutowireCandidateResolver();
        }
        this.dependencyResolvers = new AnnotatedInjectionDependencyResolvers(loadFactories(AnnotatedInjectionDependencyResolver.class, beanFactory));
    }
}
