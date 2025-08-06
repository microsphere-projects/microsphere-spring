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

import io.microsphere.annotation.ConfigurationProperty;
import io.microsphere.logging.Logger;
import io.microsphere.spring.beans.factory.BeanDependencyResolver;
import io.microsphere.spring.beans.factory.DefaultBeanDependencyResolver;
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
import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asDefaultListableBeanFactory;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static java.util.Collections.emptySet;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.springframework.util.CollectionUtils.containsAny;

/**
 * A {@link BeanFactoryListener} implementation that pre-instantiates singleton beans in parallel
 * to improve application startup performance. This class leverages multi-threading to initialize
 * beans concurrently while respecting bean dependencies.
 *
 * <h3>Configuration Properties</h3>
 *
 * <dl>
 *     <dt>{@value #THREADS_PROPERTY_NAME}</dt>
 *     <dd>
 *         The number of threads to use for parallel pre-instantiation. Default is based on the number of
 *         available processors, with a minimum of 1 thread.
 *     </dd>
 *
 *     <dt>{@value #THREAD_NAME_PREFIX_PROPERTY_NAME}</dt>
 *     <dd>
 *         The prefix for the thread names used during parallel pre-instantiation. Default is:
 *         {@value #DEFAULT_THREAD_NAME_PREFIX}
 *     </dd>
 * </dl>
 *
 * <h3>Example Usage</h3>
 *
 * <pre>{@code
 * # application.properties
 * microsphere.spring.pre-instantiation.singletons.threads=4
 * microsphere.spring.pre-instantiation.singletons.thread.name-prefix=MyCustomThread-
 * }</pre>
 *
 * <h3>Example Usage</h3>
 * <p>
 * This listener is typically registered in a Spring configuration class as follows:
 *
 * <pre>{@code
 * @Configuration
 * public class AppConfig {
 *     @Bean
 *     public BeanFactoryListener parallelPreInstantiationListener() {
 *         return new ParallelPreInstantiationSingletonsBeanFactoryListener();
 *     }
 * }
 * }</pre>
 *
 * <p>Once registered, the listener will automatically trigger parallel pre-instantiation
 * when the bean factory configuration is frozen.</p>
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
public class ParallelPreInstantiationSingletonsBeanFactoryListener implements BeanFactoryListenerAdapter,
        EnvironmentAware, BeanFactoryAware {

    /**
     * The prefix of the property for {@link ParallelPreInstantiationSingletonsBeanFactoryListener} : "microsphere.spring.pre-instantiation.singletons.";
     */
    private static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "pre-instantiation.singletons.";

    /**
     * The property name of the number of threads to pre-instantiate singletons in parallel
     */
    @ConfigurationProperty(
            type = int.class,
            description = "the number of threads to pre-instantiate singletons in parallel"
    )
    public static final String THREADS_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "threads";

    /**
     * The default prefix of thread name to pre-instantiate singletons in parallel
     */
    public static final String DEFAULT_THREAD_NAME_PREFIX = "Parallel-Pre-Instantiation-Singletons-Thread-";

    /**
     * The property name of the prefix of the thread name to pre-instantiate singletons in parallel
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_THREAD_NAME_PREFIX,
            description = "the prefix of the thread name to pre-instantiate singletons in parallel"
    )
    public static final String THREAD_NAME_PREFIX_PROPERTY_NAME = PROPERTY_NAME_PREFIX + "thread.name-prefix";

    private static final Logger logger = getLogger(ParallelPreInstantiationSingletonsBeanFactoryListener.class);

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
                executorService.shutdown();
            }
        }

        logger.info(stopWatch.toString());
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
        stopWatch.start("resolveDependentBeanNamesMap");

        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
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
                    logger.trace("The bean[name : '{}'] was created : {}", beanName, bean);
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
