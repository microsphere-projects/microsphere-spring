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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StopWatch;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static io.microsphere.lang.function.ThrowableAction.execute;
import static io.microsphere.spring.util.BeanFactoryUtils.asDefaultListableBeanFactory;
import static java.util.concurrent.Executors.newFixedThreadPool;

/**
 * Parallel {@link BeanFactoryListener}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public class ParallelBeanFactoryListener extends BeanFactoryListenerAdapter implements InitializingBean, EnvironmentAware, BeanFactoryAware {

    public static final String PARALLEL_TASKS_PROPERTY_NAME = "microsphere.spring.beans.process.parallel.tasks";

    private static final Logger logger = LoggerFactory.getLogger(ParallelBeanFactoryListener.class);


    private Environment environment;

    private int parallelTasks;

    private DefaultListableBeanFactory beanFactory;


    @Override
    public void onBeanFactoryConfigurationFrozen(ConfigurableListableBeanFactory bf) {
        DefaultListableBeanFactory beanFactory = this.beanFactory;
        if (bf != beanFactory) {
            logger.warn("Current BeanFactory[{}] is not a instance of DefaultListableBeanFactory", bf);
            return;
        }

        // Not Ready & Non-Lazy-Init Merged BeanDefinitions
        BeanDependencyResolver beanDependencyResolver = new DefaultBeanDependencyResolver(beanFactory);

        Map<String, Set<String>> dependentBeanNamesMap = beanDependencyResolver.resolve(beanFactory);

        processBeanInParallel(dependentBeanNamesMap, beanFactory);
    }

    private void processBeanInParallel(Map<String, Set<String>> dependentBeanNamesMap,
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
            execute(() -> executorService.awaitTermination(100, TimeUnit.MICROSECONDS));
        }

        executorService.shutdown();
        stopWatch.stop();
        logger.info(stopWatch.toString());
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
    }
}
