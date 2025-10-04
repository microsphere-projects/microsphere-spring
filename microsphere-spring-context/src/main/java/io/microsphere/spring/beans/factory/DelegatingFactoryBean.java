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

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static io.microsphere.spring.beans.BeanUtils.invokeAwareInterfaces;
import static io.microsphere.spring.beans.BeanUtils.invokeBeanNameAware;
import static io.microsphere.spring.beans.BeanUtils.invokeInitializingBean;
import static org.springframework.aop.support.AopUtils.getTargetClass;


/**
 * A {@link FactoryBean} implementation that delegates to an existing object instance,
 * providing lifecycle management and integration with Spring's {@link ApplicationContext}.
 *
 * <p>
 * This class is useful when you want to expose an already instantiated object as a Spring bean,
 * while still benefiting from Spring's lifecycle callbacks (e.g., {@link InitializingBean},
 * {@link DisposableBean}, etc.).
 * </p>
 *
 * <h3>Key Features:</h3>
 * <ul>
 *   <li>Delegates bean creation via the {@link #getObject()} method.</li>
 *   <li>Supports initialization through {@link InitializingBean#afterPropertiesSet()}.</li>
 *   <li>Supports destruction callback if the delegate implements {@link DisposableBean}.</li>
 *   <li>Implements Spring's aware interfaces such as {@link ApplicationContextAware} and
 *       {@link BeanNameAware}, delegating calls to the target object if applicable.</li>
 * </ul>
 *
 * <h3>Usage Example:</h3>
 *
 * <pre>{@code
 * MyService myService = new MyServiceImpl();
 * DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(myService);
 *
 * // When used in a Spring configuration:
 * @Bean
 * public FactoryBean<MyService> myServiceFactoryBean() {
 *     return new DelegatingFactoryBean<>(myServiceInstance);
 * }
 * }</pre>
 *
 * <p>
 * In this example, Spring will treat the returned {@link FactoryBean} as a bean definition,
 * and the actual object returned by {@link #getObject()} will be registered in the context.
 * If the delegate implements lifecycle or aware interfaces, they will be invoked at the
 * appropriate time during bean creation and destruction.
 * </p>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see FactoryBean
 * @see InitializingBean
 * @see DisposableBean
 * @see ApplicationContextAware
 * @see BeanNameAware
 * @since 1.0.0
 */
public class DelegatingFactoryBean implements FactoryBean<Object>, InitializingBean, DisposableBean,
        ApplicationContextAware, BeanNameAware {

    private final Object delegate;

    private final Class<?> objectType;

    private final boolean singleton;

    public DelegatingFactoryBean(Object delegate) {
        this(delegate, true);
    }

    public DelegatingFactoryBean(Object delegate, boolean singleton) {
        this.delegate = delegate;
        this.objectType = getTargetClass(delegate);
        this.singleton = singleton;
    }

    @Override
    public Object getObject() throws Exception {
        return delegate;
    }

    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        invokeInitializingBean(delegate);
    }

    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        invokeAwareInterfaces(delegate, context);
    }

    @Override
    public void setBeanName(String name) {
        invokeBeanNameAware(delegate, name);
    }

    @Override
    public boolean isSingleton() {
        return singleton;
    }

    @Override
    public void destroy() throws Exception {
        if (delegate instanceof DisposableBean) {
            ((DisposableBean) delegate).destroy();
        }
    }
}
