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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.collection.ListUtils.newLinkedList;
import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.util.concurrent.Executors.newFixedThreadPool;

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
        CompletionService completionService = new ExecutorCompletionService(executorService);
        int tasks = 0;
        AtomicInteger completedTasks = new AtomicInteger(0);
        for (Map.Entry<String, Set<String>> dependentEntry : dependentBeanNamesMap.entrySet()) {
            String beanName = dependentEntry.getKey();
            Set<String> dependentBeanNames = dependentEntry.getValue();
            // TODO Add parallel strategies:
            // 1. add allow list
            // 2. add disallow list
            // 3. safest
            // 4. Spring stack beans in the main thread
            if (dependentBeanNames.isEmpty()) { // No Dependent Bean == safest
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
            execute(() -> executorService.awaitTermination(1, TimeUnit.MILLISECONDS));
        }

        stopWatch.stop();
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
