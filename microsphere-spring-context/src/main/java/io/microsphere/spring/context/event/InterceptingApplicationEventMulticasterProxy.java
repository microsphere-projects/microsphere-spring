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

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationEventMulticaster;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static io.microsphere.spring.context.event.InterceptingApplicationEventMulticaster.resolveEventType;
import static io.microsphere.spring.util.BeanUtils.getSortedBeans;
import static org.springframework.context.support.AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME;

/**
 * Intercepting {@link ApplicationEventMulticaster} Proxy
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventMulticaster
 * @since 1.0.0
 */
public class InterceptingApplicationEventMulticasterProxy extends StaticMethodMatcherPointcutAdvisor
        implements InitializingBean, MethodInterceptor, ApplicationEventMulticaster, BeanFactoryAware {

    /**
     * The property name of the reset bean name of {@link ApplicationEventMulticaster}
     */
    public static final String RESET_BEAN_NAME_PROPERTY_NAME = "microsphere.application-event-multicaster.reset-bean-name";

    /**
     * The default reset bean name of {@link ApplicationEventMulticaster}.
     * <p>
     * The original bean name of {@link ApplicationEventMulticaster} is
     * {@link AbstractApplicationContext#APPLICATION_EVENT_MULTICASTER_BEAN_NAME "applicationEventMulticaster"}
     *
     * @see AbstractApplicationContext#APPLICATION_EVENT_MULTICASTER_BEAN_NAME
     */
    public static final String DEFAULT_RESET_BEAN_NAME = APPLICATION_EVENT_MULTICASTER_BEAN_NAME + "_ORIGINAL";

    private final String delegateBeanName;

    private ApplicationEventMulticaster delegate;

    private List<ApplicationEventInterceptor> applicationEventInterceptors;

    private List<ApplicationListenerInterceptor> applicationListenerInterceptors;

    private Executor executor;

    public InterceptingApplicationEventMulticasterProxy(Environment environment) {
        this.delegateBeanName = getResetBeanName(environment);
    }

    public static String getResetBeanName(Environment environment) {
        return environment.getProperty(RESET_BEAN_NAME_PROPERTY_NAME, DEFAULT_RESET_BEAN_NAME);
    }

    @Override
    public void addApplicationListener(ApplicationListener<?> listener) {
        InterceptingApplicationListener wrapper = new InterceptingApplicationListener(listener, applicationListenerInterceptors);
        delegate.addApplicationListener(wrapper);
    }

    @Override
    public void addApplicationListenerBean(String listenerBeanName) {
        delegate.addApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListener(ApplicationListener<?> listener) {
        delegate.removeApplicationListener(listener);
    }

    @Override
    public void removeApplicationListenerBean(String listenerBeanName) {
        delegate.removeApplicationListenerBean(listenerBeanName);
    }

    @Override
    public void removeApplicationListeners(Predicate<ApplicationListener<?>> predicate) {
        delegate.removeApplicationListeners(predicate);
    }

    @Override
    public void removeApplicationListenerBeans(Predicate<String> predicate) {
        delegate.removeApplicationListenerBeans(predicate);
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

    private void onEvent(ApplicationEvent event, ResolvableType resolvableType) {
        delegate.multicastEvent(event, resolvableType);
    }

    private void execute(Runnable runnable) {
        getExecutor().execute(runnable);
    }

    public void setTaskExecutor(@Nullable Executor executor) {
        this.executor = executor;
    }

    public Executor getExecutor() {
        if (executor == null) {
            executor = Runnable::run;
        }
        return executor;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ListableBeanFactory listableBeanFactory = (ListableBeanFactory) beanFactory;
        this.delegate = beanFactory.getBean(this.delegateBeanName, ApplicationEventMulticaster.class);
        this.applicationEventInterceptors = getSortedBeans(listableBeanFactory, ApplicationEventInterceptor.class);
        this.applicationListenerInterceptors = getSortedBeans(listableBeanFactory, ApplicationListenerInterceptor.class);
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        if (!ApplicationListener.class.isAssignableFrom(targetClass)) {
            return false;
        }

        String methodName = method.getName();
        if (!"onApplicationEvent".equals(methodName)) {
            return false;
        }

        if (method.getParameterCount() != 1) {
            return false;
        }

        Class<?> parameterType = method.getParameterTypes()[0];

        if (!ApplicationEvent.class.isAssignableFrom(parameterType)) {
            return false;
        }

        return true;
    }


    @Nullable
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        ApplicationListener applicationListener = (ApplicationListener) invocation.getThis();
        InterceptingApplicationListener wrapper = new InterceptingApplicationListener(applicationListener, applicationListenerInterceptors);
        Object[] args = invocation.getArguments();
        Method method = invocation.getMethod();
        return method.invoke(wrapper, args);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        setAdvice(this);
    }
}
