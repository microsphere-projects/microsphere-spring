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

import io.microsphere.logging.Logger;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.util.ArrayUtils.arrayToString;

/**
 * A {@link BeanListener} implementation that provides comprehensive logging for various bean lifecycle events.
 * <p>
 * This class logs events at different stages of a bean's lifecycle, including bean definition readiness,
 * instantiation, property value setup, initialization, and destruction. All log messages are generated using the
 * {@link io.microsphere.logging.Logger} interface, and can be controlled based on the active logging level.
 * </p>
 *
 * <h3>Logged Events</h3>
 * <ul>
 *     <li>{@link #onBeanDefinitionReady(String, RootBeanDefinition)} - When a bean definition is ready.</li>
 *     <li>{@link #onBeforeBeanInstantiate(String, RootBeanDefinition)} - Before a bean is instantiated.</li>
 *     <li>{@link #onBeforeBeanInstantiate(String, RootBeanDefinition, Constructor, Object[])} - Before a bean is
 *     instantiated using a specific constructor.</li>
 *     <li>{@link #onBeforeBeanInstantiate(String, RootBeanDefinition, Object, Method, Object[])} - Before a bean is
 *     instantiated using a factory method.</li>
 *     <li>{@link #onAfterBeanInstantiated(String, RootBeanDefinition, Object)} - After a bean has been instantiated.</li>
 *     <li>{@link #onBeanPropertyValuesReady(String, Object, PropertyValues)} - When property values are ready for a bean.</li>
 *     <li>{@link #onBeforeBeanInitialize(String, Object)} - Before a bean is initialized.</li>
 *     <li>{@link #onAfterBeanInitialized(String, Object)} - After a bean has been initialized.</li>
 *     <li>{@link #onBeanReady(String, Object)} - When a bean is fully ready for use.</li>
 *     <li>{@link #onBeforeBeanDestroy(String, Object)} - Before a bean is destroyed.</li>
 *     <li>{@link #onAfterBeanDestroy(String, Object)} - After a bean has been destroyed.</li>
 * </ul>
 *
 * <h3>Logging Examples</h3>
 *
 * <h4>Basic Logging</h4>
 * When a bean is instantiated:
 * <pre>
 * onBeforeBeanInstantiate - bean name : exampleBean , definition : RootBeanDefinition...
 * </pre>
 *
 * <h4>Constructor-based Instantiation</h4>
 * When a bean is instantiated using a specific constructor:
 * <pre>
 * onBeforeBeanInstantiate - bean name : exampleBean , definition : RootBeanDefinition... , constructor : public ExampleBean(String arg) , args : [testArg]
 * </pre>
 *
 * <h4>Factory Method-based Instantiation</h4>
 * When a bean is created using a factory method:
 * <pre>
 * onBeforeBeanInstantiate - bean name : exampleBean , definition : RootBeanDefinition... , factoryBean : com.example.FactoryBean@12345 , factoryMethod : createBean , args : [testArg]
 * </pre>
 *
 * <h4>Property Injection</h4>
 * When property values are set on a bean:
 * <pre>
 * onBeanPropertyValuesReady - bean name : exampleBean , instance : com.example.ExampleBean@67890 , PropertyValues : {name=testValue}
 * </pre>
 *
 * <h4>Bean Initialization</h4>
 * Before and after bean initialization:
 * <pre>
 * onBeforeBeanInitialize - bean name : exampleBean , instance : com.example.ExampleBean@67890
 * onAfterBeanInitialized - bean name : exampleBean , instance : com.example.ExampleBean@67890
 * </pre>
 *
 * <h4>Bean Destruction</h4>
 * Before and after bean destruction:
 * <pre>
 * onBeforeBeanDestroy - bean name : exampleBean , instance : com.example.ExampleBean@67890
 * onAfterBeanDestroy - bean name : exampleBean , instance : com.example.ExampleBean@67890
 * </pre>
 *
 * <p>
 * These logs help in debugging and understanding the Spring bean lifecycle, especially useful in complex applications
 * where bean creation and management need to be closely monitored.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanInitializer
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanListeners
 * @see BeanListener
 * @see BeanListenerAdapter
 * @since 1.0.0
 */
public class LoggingBeanListener implements BeanListener {

    private static final Logger logger = getLogger(LoggingBeanListener.class);

    @Override
    public boolean supports(String beanName) {
        return true;
    }

    @Override
    public void onBeanDefinitionReady(String beanName, RootBeanDefinition mergedBeanDefinition) {
        logger.info("onBeanDefinitionReady - bean name : {} , definition : {}", beanName, mergedBeanDefinition);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition) {
        logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {}", beanName, mergedBeanDefinition);
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Constructor<?> constructor, Object[] args) {
        logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {} , constructor : {} , args : {}", beanName, mergedBeanDefinition, constructor, arrayToString(args));
    }

    @Override
    public void onBeforeBeanInstantiate(String beanName, RootBeanDefinition mergedBeanDefinition, Object factoryBean, Method factoryMethod, Object[] args) {
        logger.info("onBeforeBeanInstantiate - bean name : {} , definition : {} , factoryBean : {} , factoryMethod : {} , args : {}", beanName, mergedBeanDefinition, factoryBean, factoryMethod, arrayToString(args));
    }

    @Override
    public void onAfterBeanInstantiated(String beanName, RootBeanDefinition mergedBeanDefinition, Object bean) {
        logger.info("onAfterBeanInstantiated - bean name : {} , definition : {} , instance : {}", beanName, mergedBeanDefinition, bean);
    }

    @Override
    public void onBeanPropertyValuesReady(String beanName, Object bean, PropertyValues pvs) {
        logger.info("onBeanPropertyValuesReady - bean name : {} , instance : {} , PropertyValues : {}", beanName, bean, pvs);
    }

    @Override
    public void onBeforeBeanInitialize(String beanName, Object bean) {
        logger.info("onBeforeBeanInitialize - bean name : {} , instance : {}", beanName, bean);
    }

    @Override
    public void onAfterBeanInitialized(String beanName, Object bean) {
        logger.info("onAfterBeanInitialized - bean name : {} , instance : {}", beanName, bean);
    }

    @Override
    public void onBeanReady(String beanName, Object bean) {
        logger.info("onBeanReady - bean name : {} , instance : {}", beanName, bean);
    }

    @Override
    public void onBeforeBeanDestroy(String beanName, Object bean) {
        logger.info("onBeforeBeanDestroy - bean name : {} , instance : {}", beanName, bean);
    }

    @Override
    public void onAfterBeanDestroy(String beanName, Object bean) {
        logger.info("onAfterBeanDestroy - bean name : {} , instance : {}", beanName, bean);
    }
}
