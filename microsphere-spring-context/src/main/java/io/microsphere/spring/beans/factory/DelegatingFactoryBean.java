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

    /**
     * Creates a new {@link DelegatingFactoryBean} that wraps the given delegate object
     * as a singleton bean (the default).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DisposableBean myBean = new MyDisposableBean();
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(myBean);
     *   // factoryBean.isSingleton() returns true
     *   // factoryBean.getObject() returns myBean
     * }</pre>
     *
     * @param delegate the object to delegate to; must not be {@code null}
     */
    public DelegatingFactoryBean(Object delegate) {
        this(delegate, true);
    }

    /**
     * Creates a new {@link DelegatingFactoryBean} that wraps the given delegate object
     * with the specified singleton flag.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   User user = new User();
     *   // Non-singleton: a new instance may be requested each time
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(user, false);
     *   // factoryBean.isSingleton() returns false
     *   // factoryBean.getObjectType() returns User.class
     * }</pre>
     *
     * @param delegate  the object to delegate to; must not be {@code null}
     * @param singleton {@code true} if the factory bean should report itself as a singleton,
     *                  {@code false} otherwise
     */
    public DelegatingFactoryBean(Object delegate, boolean singleton) {
        this.delegate = delegate;
        this.objectType = getTargetClass(delegate);
        this.singleton = singleton;
    }

    /**
     * Returns the delegate object managed by this factory bean.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   User user = new User();
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(user, false);
     *   Object obj = factoryBean.getObject();
     *   // obj is the same instance as user
     * }</pre>
     *
     * @return the delegate object, never {@code null}
     * @throws Exception if object creation fails
     */
    @Override
    public Object getObject() throws Exception {
        return delegate;
    }

    /**
     * Returns the type of the delegate object. The type is resolved using
     * {@link org.springframework.aop.support.AopUtils#getTargetClass(Object)},
     * which correctly resolves the target class even for AOP proxies.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   User user = new User();
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(user, false);
     *   Class<?> type = factoryBean.getObjectType();
     *   // type is User.class
     * }</pre>
     *
     * @return the class of the delegate object
     */
    @Override
    public Class<?> getObjectType() {
        return objectType;
    }

    /**
     * Invokes the {@link InitializingBean#afterPropertiesSet()} callback on the delegate
     * if it implements {@link InitializingBean}. This allows the delegate to perform
     * initialization work after all properties have been set by the containing
     * {@link org.springframework.beans.factory.BeanFactory}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(myInitializingBean);
     *   // Triggers delegate's afterPropertiesSet() if it implements InitializingBean
     *   factoryBean.afterPropertiesSet();
     * }</pre>
     *
     * @throws Exception if the delegate's initialization fails
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        invokeInitializingBean(delegate);
    }

    /**
     * Sets the {@link ApplicationContext} and propagates it to the delegate if the delegate
     * implements any Spring aware interfaces (e.g., {@link ApplicationContextAware},
     * {@link org.springframework.context.EnvironmentAware}, etc.).
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(myBean);
     *   // If myBean implements ApplicationContextAware, it will receive the context
     *   factoryBean.setApplicationContext(applicationContext);
     * }</pre>
     *
     * @param context the {@link ApplicationContext} to propagate to the delegate
     * @throws BeansException if the aware callback invocation fails
     */
    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        invokeAwareInterfaces(delegate, context);
    }

    /**
     * Sets the bean name and propagates it to the delegate if the delegate implements
     * {@link BeanNameAware}.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(myBean);
     *   // If myBean implements BeanNameAware, it will receive the name "factoryBean"
     *   factoryBean.setBeanName("factoryBean");
     * }</pre>
     *
     * @param name the name of the bean in the Spring container
     */
    @Override
    public void setBeanName(String name) {
        invokeBeanNameAware(delegate, name);
    }

    /**
     * Returns whether this factory bean produces a singleton instance. The value is determined
     * by the {@code singleton} parameter passed to the constructor.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DelegatingFactoryBean singletonBean = new DelegatingFactoryBean(delegate);
     *   singletonBean.isSingleton(); // returns true (default)
     *
     *   DelegatingFactoryBean prototypeBean = new DelegatingFactoryBean(delegate, false);
     *   prototypeBean.isSingleton(); // returns false
     * }</pre>
     *
     * @return {@code true} if this factory bean is a singleton, {@code false} otherwise
     */
    @Override
    public boolean isSingleton() {
        return singleton;
    }

    /**
     * Destroys the delegate by invoking its {@link DisposableBean#destroy()} method,
     * but only if the delegate implements {@link DisposableBean}. If the delegate does not
     * implement {@link DisposableBean}, this method is a no-op.
     *
     * <h3>Example Usage</h3>
     * <pre>{@code
     *   DisposableBean disposable = new MyDisposableBean();
     *   DelegatingFactoryBean factoryBean = new DelegatingFactoryBean(disposable);
     *   // Invokes disposable.destroy()
     *   factoryBean.destroy();
     *
     *   User user = new User();
     *   DelegatingFactoryBean factoryBean2 = new DelegatingFactoryBean(user, false);
     *   // No-op since User does not implement DisposableBean
     *   factoryBean2.destroy();
     * }</pre>
     *
     * @throws Exception if the delegate's destroy method fails
     */
    @Override
    public void destroy() throws Exception {
        if (delegate instanceof DisposableBean disposableBean) {
            disposableBean.destroy();
        }
    }
}
