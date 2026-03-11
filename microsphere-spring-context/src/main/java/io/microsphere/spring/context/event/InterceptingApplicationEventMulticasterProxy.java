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
import io.microsphere.annotation.Nullable;
import io.microsphere.lang.DelegatingWrapper;
import io.microsphere.spring.beans.factory.config.GenericBeanPostProcessorAdapter;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static io.microsphere.collection.MapUtils.newLinkedHashMap;
import static io.microsphere.concurrent.ExecutorUtils.shutdownOnExit;
import static io.microsphere.reflect.MethodUtils.findDeclaredMethod;
import static io.microsphere.reflect.MethodUtils.invokeMethod;
import static io.microsphere.spring.beans.BeanUtils.getSortedBeans;
import static io.microsphere.spring.beans.factory.BeanFactoryUtils.asListableBeanFactory;
import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticaster.resolveEventType;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * Intercepting {@link ApplicationEventMulticaster} Proxy
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventMulticaster
 * @since 1.0.0
 */
public class InterceptingApplicationEventMulticasterProxy extends GenericBeanPostProcessorAdapter<ApplicationListener>
        implements ApplicationEventMulticaster, BeanFactoryAware, DelegatingWrapper, DisposableBean {

    /**
     * The default reset bean name of {@link ApplicationEventMulticaster}.
     * <p>
     * The original bean name of {@link ApplicationEventMulticaster} is
     * {@link AbstractApplicationContext#APPLICATION_EVENT_MULTICASTER_BEAN_NAME "applicationEventMulticaster"}
     *
     * @see AbstractApplicationContext#APPLICATION_EVENT_MULTICASTER_BEAN_NAME
     */
    public static final String DEFAULT_RESET_BEAN_NAME = APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "_ORIGINAL";

    /**
     * The property name of the reset bean name of {@link ApplicationEventMulticaster}
     */
    @ConfigurationProperty(
            defaultValue = DEFAULT_RESET_BEAN_NAME,
            description = "The property name of the reset bean name of ApplicationEventMulticaster"
    )
    public static final String RESET_BEAN_NAME_PROPERTY_NAME = "microsphere.spring.application-event-multicaster.reset-bean-name";

    /**
     * The method name of {@link ApplicationEventMulticaster#removeApplicationListeners(Predicate)}
     * since Spring Framework 5.3.5
     */
    private static final Method removeApplicationListenersMethod = findDeclaredMethod(ApplicationEventMulticaster.class, "removeApplicationListeners", Predicate.class);

    /**
     * The method name of {@link ApplicationEventMulticaster#removeApplicationListenerBeans(Predicate)}
     * since Spring Framework 5.3.5
     */
    private static final Method removeApplicationListenerBeansMethod = findDeclaredMethod(ApplicationEventMulticaster.class, "removeApplicationListenerBeans", Predicate.class);

    private final String delegateBeanName;

    private ApplicationEventMulticaster delegate;

    private List<ApplicationEventInterceptor> applicationEventInterceptors;

    private List<ApplicationListenerInterceptor> applicationListenerInterceptors;

    private Map<ApplicationListener, InterceptingApplicationListener> applicationListenersMap;

    private Executor taskExecutor;

    public InterceptingApplicationEventMulticasterProxy(Environment environment) {
        this.delegateBeanName = getResetBeanName(environment);
    }

    public static String getResetBeanName(Environment environment) {
        return environment.getProperty(RESET_BEAN_NAME_PROPERTY_NAME, DEFAULT_RESET_BEAN_NAME);
    }

    @Override
    public synchronized void addApplicationListener(ApplicationListener<?> listener) {
        InterceptingApplicationListener interceptingApplicationListener = wrap(listener);
        this.delegate.addApplicationListener(interceptingApplicationListener);
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        this.delegate.addApplicationListenerBean(listenerBeanName);
    }

    @Override
    public synchronized void removeApplicationListener(ApplicationListener<?> listener) {
        final InterceptingApplicationListener wrapper;
        if (isCachedInterceptingApplicationListener(listener)) {
            wrapper = (InterceptingApplicationListener) listener;
        } else {
            wrapper = this.applicationListenersMap.remove(listener);
        }
        if (wrapper != null) {
            this.delegate.removeApplicationListener(wrapper);
        }
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        delegate.removeApplicationListenerBean(listenerBeanName);
    }

    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {
        if (removeApplicationListenersMethod == null) {
            return;
        }
        Predicate<ApplicationListener<?>> listenerPredicate = listener -> {
            InterceptingApplicationListener wrapper = (InterceptingApplicationListener) listener;
            return predicate.test(wrapper.getDelegate());
        };
        invokeMethod(this.delegate, removeApplicationListenersMethod, listenerPredicate);
    }

    public void removeApplicationListenerBeans(Predicate<String> predicate) {
        if (removeApplicationListenerBeansMethod == null) {
            return;
        }
        invokeMethod(this.delegate, removeApplicationListenerBeansMethod, predicate);
    }

    @Override
    public void removeAllListeners() {
        delegate.removeAllListeners();
    }

    @Override
    public void multicastEvent(ApplicationEvent event) {
        execute(() -> delegate.multicastEvent(event));
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        execute(() -> {
            ResolvableType type = resolveEventType(event, eventType);
            DefaultApplicationEventInterceptorChain chain = new DefaultApplicationEventInterceptorChain(this.applicationEventInterceptors, this::onEvent);
            chain.intercept(event, type);
        });
    }

    @Override
    protected ApplicationListener doPostProcessAfterInitialization(ApplicationListener bean, String beanName) throws BeansException {
        return wrap(bean);
    }

    protected InterceptingApplicationListener wrap(ApplicationListener listener) {
        if (listener instanceof InterceptingApplicationListener) {
            InterceptingApplicationListener interceptingApplicationListener = (InterceptingApplicationListener) listener;
            if (!isCachedInterceptingApplicationListener(listener)) {
                ApplicationListener<?> delegate = interceptingApplicationListener.getDelegate();
                this.applicationListenersMap.put(delegate, interceptingApplicationListener);
            }
            return interceptingApplicationListener;
        } else {
            return this.applicationListenersMap.computeIfAbsent(listener, l -> new InterceptingApplicationListener(l, applicationListenerInterceptors));
        }
    }

    protected boolean isCachedInterceptingApplicationListener(ApplicationListener listener) {
        return this.applicationListenersMap.containsValue(listener);
    }

    private void onEvent(ApplicationEvent event, ResolvableType resolvableType) {
        delegate.multicastEvent(event, resolvableType);
    }

    private void execute(Runnable runnable) {
        getTaskExecutor().execute(runnable);
    }

    public void setTaskExecutor(@Nullable Executor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    protected Executor getTaskExecutor() {
        if (this.taskExecutor == null) {
            setTaskExecutor(Runnable::run);
        }
        return this.taskExecutor;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ListableBeanFactory listableBeanFactory = asListableBeanFactory(beanFactory);
        this.delegate = beanFactory.getBean(this.delegateBeanName, ApplicationEventMulticaster.class);
        this.applicationEventInterceptors = getSortedBeans(listableBeanFactory, ApplicationEventInterceptor.class);
        this.applicationListenerInterceptors = getSortedBeans(listableBeanFactory, ApplicationListenerInterceptor.class);
        this.applicationListenersMap = newLinkedHashMap();
    }

    @Override
    public Object getDelegate() {
        return this.delegate;
    }

    @Override
    public void destroy() throws Exception {
        this.applicationListenersMap.clear();
        shutdownOnExit(this.taskExecutor);
    }
}