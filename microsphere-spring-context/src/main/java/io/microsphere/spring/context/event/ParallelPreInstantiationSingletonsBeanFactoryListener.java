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

import io.microsphere.lang.function.ThrowableSupplier;
import io.microsphere.spring.beans.factory.BeanDependencyResolver;
import io.microsphere.spring.beans.factory.DefaultBeanDependencyResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.util.StopWatch;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import static io.microsphere.collection.ListUtils.newArrayList;
import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.lang.function.ThrowableSupplier.execute;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * The {@link BeanFactoryListener} class {@link DefaultListableBeanFactory#preInstantiateSingletons() pre-instantiates singletons}
 * in parallel.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ParallelPreInstantiationSingletonsBeanFactoryListener extends BeanFactoryListenerAdapter implements
        EnvironmentAware, BeanFactoryAware {

    public static final String THREADS_PROPERTY_NAME = "microsphere.spring.pre-instantiation.singletons.threads";
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = "microsphere.spring.pre-instantiation.singletons.thread.name-prefix";
    public static final String DEFAULT_THREAD_NAME_PREFIX = "Parallel-Pre-Instantiation-Singletons-Thread-";

    private static final Logger logger = LoggerFactory.getLogger(ParallelPreInstantiationSingletonsBeanFactoryListener.class);

    private Environment environment;

    private DefaultListableBeanFactory beanFactory;

    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (bf != beanFactory) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return;
        }

        StopWatch stopWatch = new StopWatch("ParallelPreInstantiationSingletons");
        ExecutorService executorService = newExecutorService();
        if (executorService != null) {
            try {
                Map<String, Set<String>> dependentBeanNamesMap = resolveDependentBeanNamesMap(beanFactory, executorService, stopWatch);
                preInstantiateSingletonsInParallel(dependentBeanNamesMap, beanFactory, executorService, stopWatch);
            } finally {
                logger.info(stopWatch.toString());
                executorService.shutdown();
            }
        }
    }

    private ExecutorService newExecutorService() {
        int threads = environment.getProperty(THREADS_PROPERTY_NAME, int.class, getDefaultThreads());
        if (threads < 1) {
            return null;
        }
        String threadNamePrefix = environment.getProperty(THREAD_NAME_PREFIX_PROPERTY_NAME, DEFAULT_THREAD_NAME_PREFIX);
        ExecutorService executorService = newFixedThreadPool(threads, new CustomizableThreadFactory(threadNamePrefix));
        return executorService;
    }

    private Map<String, Set<String>> resolveDependentBeanNamesMap(DefaultListableBeanFactory beanFactory, ExecutorService executorService, StopWatch stopWatch) {
        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        stopWatch.start("resolveDependentBeanNamesMap");
        BeanDependencyResolver beanDependencyResolver = new DefaultBeanDependencyResolver(beanFactory, executorService);
        Map<String, Set<String>> dependentBeanNamesMap = beanDependencyResolver.resolve(beanFactory);
        stopWatch.stop();
        return dependentBeanNamesMap;
    }

    private void preInstantiateSingletonsInParallel(Map<String, Set<String>> dependentBeanNamesMap,
                                                    DefaultListableBeanFactory beanFactory, ExecutorService executorService, StopWatch stopWatch) {
        stopWatch.start("preInstantiateSingletonsInParallel");

        List<Set<String>> beanNamesInDependencyPaths = resolveBeanNamesInDependencyPaths(dependentBeanNamesMap);

        for (int i = 0; i < beanNamesInDependencyPaths.size(); i++) {
            Set<String> beanNamesInDependencyPath = beanNamesInDependencyPaths.get(i);
            executorService.submit(() -> {
                for (String beanName : beanNamesInDependencyPath) {
                    Object bean = beanFactory.getBean(beanName);
                    logger.debug("The bean[name : '{}'] was created : {}", beanName, bean);
                }
                return null;
            });
        }

        while (execute(() -> executorService.awaitTermination(10, TimeUnit.MILLISECONDS))) {
        }

        stopWatch.stop();
    }

    private List<Set<String>> resolveBeanNamesInDependencyPaths(Map<String, Set<String>> dependentBeanNamesMap) {
        List<Set<String>> beanNamesList = buildBeanNamesList(dependentBeanNamesMap);
        List<Set<String>> dependencyPaths = newLinkedList();
        int size = beanNamesList.size();
        for (int i = 0; i < size; i++) {
            Set<String> beanNames = beanNamesList.get(i);
            if (!beanNames.isEmpty()) {
                for (int j = i + 1; j < size; j++) {
                    Set<String> otherNames = beanNamesList.get(j);
                    if (containsAny(beanNames, otherNames)) {
                        beanNames.addAll(otherNames);
                        // set the empty set into the index 'j'
                        beanNamesList.set(j, emptySet());
                    }
                }
            }
        }

        for (int i = 0; i < size; i++) {
            Set<String> beanNames = beanNamesList.get(i);
            if (!beanNames.isEmpty()) {
                dependencyPaths.add(beanNames);
            }
        }

        return dependencyPaths;
    }

    private List<Set<String>> buildBeanNamesList(Map<String, Set<String>> dependentBeanNamesMap) {
        List<Set<String>> beanNamesList = newArrayList(dependentBeanNamesMap.size());
        for (Map.Entry<String, Set<String>> dependentEntry : dependentBeanNamesMap.entrySet()) {
            String beanName = dependentEntry.getKey();
            Set<String> dependentBeanNames = dependentEntry.getValue();
            // reuse the space of dependentBeanNames
            Set<String> beanNames = dependentBeanNames;
            beanNames.add(beanName);
            beanNamesList.add(beanNames);
        }
        return beanNamesList;
    }

    private void mergeBeanNames(Map.Entry<String, Set<String>> dependentEntry, List<Set<String>> allBeanNamesList,
                                Map<String, Set<String>> dependentBeanNamesMap) {

        for (Map.Entry<String, Set<String>> entry : dependentBeanNamesMap.entrySet()) {
            if (dependentEntry.equals(entry)) {
                continue;
            }

            String beanName = dependentEntry.getKey();
            Set<String> dependentBeanNames = dependentEntry.getValue();
            Set<String> allBeanNames = new LinkedHashSet<>(1 + dependentBeanNames.size());
            allBeanNames.add(beanName);
            allBeanNames.addAll(dependentBeanNames);
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private int getDefaultThreads() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        return Math.max(1, availableProcessors);
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = asDefaultListableBeanFactory(beanFactory);
    }
}
