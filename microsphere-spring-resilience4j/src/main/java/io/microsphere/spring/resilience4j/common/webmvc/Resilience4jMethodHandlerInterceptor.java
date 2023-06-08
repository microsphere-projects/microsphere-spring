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
package io.microsphere.spring.resilience4j.common.webmvc;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.Registry;
import io.microsphere.spring.resilience4j.common.Resilience4jContext;
import io.microsphere.spring.resilience4j.common.Resilience4jModule;
import io.microsphere.spring.webmvc.interceptor.MethodHandlerInterceptor;
import io.microsphere.spring.webmvc.method.HandlerMethodsInitializedEvent;
import io.vavr.control.Try;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.util.Assert;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import static io.microsphere.reflect.MethodUtils.getSignature;
import static io.microsphere.spring.resilience4j.common.Resilience4jModule.valueOf;
import static org.springframework.core.ResolvableType.forType;

/**
 * The abstract template class for Resilience4j's {@link MethodHandlerInterceptor}
 *
 * @param <E> the type of Resilience4j's entity, e.g., {@link CircuitBreaker}
 * @param <C> the type of Resilience4j's configuration, e.g., {@link CircuitBreakerConfig}
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see MethodHandlerInterceptor
 * @see Resilience4jModule
 * @since 1.0.0
 */
public abstract class Resilience4jMethodHandlerInterceptor<E, C> extends MethodHandlerInterceptor implements ApplicationListener<HandlerMethodsInitializedEvent>, DisposableBean, Ordered {

    protected final static int ENTRY_CLASS_GENERIC_INDEX = 0;

    protected final static int CONFIGURATION_CLASS_GENERIC_INDEX = 1;

    protected final static Function<? super Throwable, Exception> EXCEPTION_PROVIDER = t -> t instanceof Exception ? (Exception) t : new Exception(t.getCause());

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final Registry<E, C> registry;

    /**
     * Local Cache using {@link HashMap} with better performance
     */
    protected final Map<String, E> entryCaches;

    private final Class<E> entryClass;

    private final Class<C> configurationClass;

    private final Resilience4jModule module;

    public Resilience4jMethodHandlerInterceptor(Registry<E, C> registry) {
        // always keep self being a delegate
        super(Boolean.TRUE);
        Assert.notNull(registry, "The 'registry' argument can't be null");
        this.registry = registry;
        this.entryCaches = new HashMap<>();
        ResolvableType currentType = forType(getClass());
        ResolvableType superType = currentType.as(Resilience4jMethodHandlerInterceptor.class);
        this.entryClass = (Class<E>) superType.getGeneric(ENTRY_CLASS_GENERIC_INDEX).resolve();
        this.configurationClass = (Class<C>) superType.getGeneric(CONFIGURATION_CLASS_GENERIC_INDEX).resolve();
        this.module = valueOf(this.entryClass);
    }

    @Override
    protected final boolean preHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {
        Resilience4jContext<E> context = getContext(request, handlerMethod);
        Try.run(() -> preHandle(context, request, response, handlerMethod)).getOrElseThrow(EXCEPTION_PROVIDER);
        return true;
    }


    @Override
    protected final void postHandle(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Exception {
        Resilience4jContext<E> context = getContext(request, handlerMethod);
        Try.run(() -> postHandle(context, request, response, handlerMethod, modelAndView)).getOrElseThrow(EXCEPTION_PROVIDER);
    }

    @Override
    protected final void afterCompletion(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Exception {
        Resilience4jContext<E> context = getContext(request, handlerMethod);
        Try.run(() -> afterCompletion(context, request, response, handlerMethod, ex)).getOrElseThrow(EXCEPTION_PROVIDER);
    }

    protected abstract void preHandle(Resilience4jContext<E> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Throwable;

    protected abstract void postHandle(Resilience4jContext<E> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, ModelAndView modelAndView) throws Throwable;

    protected abstract void afterCompletion(Resilience4jContext<E> context, HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod, Exception ex) throws Throwable;

    @Override
    public void onApplicationEvent(HandlerMethodsInitializedEvent event) {
        Set<HandlerMethod> handlerMethods = event.getHandlerMethods();
        initEntryCache(handlerMethods);
    }

    @Override
    public void destroy() throws Exception {
        this.entryCaches.clear();
    }

    /**
     * Get the Resilience4j Registry
     *
     * @return non-null
     */
    public final Registry<E, C> getRegistry() {
        return registry;
    }

    /**
     * Get the order of current interceptor bean
     *
     * @return {@link #getModule() current module}'s {@link Resilience4jModule#getDefaultAspectOrder() aspect order} as default
     * @see <a href="https://resilience4j.readme.io/docs/getting-started-3#aspect-order">Resilience4j Aspect order</a>
     */
    public int getOrder() {
        return this.module.getDefaultAspectOrder();
    }

    protected void initEntryCache(Set<HandlerMethod> handlerMethods) {
        int size = handlerMethods.size();
        Map<String, E> entryCaches = new HashMap<>(size);

        for (HandlerMethod handlerMethod : handlerMethods) {
            String entryName = getEntryName(handlerMethod);
            E entry = createEntry(entryName);
            entryCaches.put(entryName, entry);
            logger.debug("A new entry[name : '{}' , type : '{}'] was added into cache", entryName, entry.getClass().getName());
        }

        this.entryCaches.putAll(entryCaches);
    }

    protected final Resilience4jContext<E> getContext(HttpServletRequest request, HandlerMethod handlerMethod) {
        String attributeName = getModule().name();
        Resilience4jContext<E> context = (Resilience4jContext<E>) request.getAttribute(attributeName);
        if (context == null) {
            String name = getEntryName(handlerMethod);
            E entry = getEntry(name);
            context = new Resilience4jContext<>(name, entry, module);
            request.setAttribute(attributeName, context);
        }
        return context;
    }

    protected final E getEntry(String name) {
        E entry = entryCaches.computeIfAbsent(name, this::createEntry);
        return entry;
    }

    protected abstract E createEntry(String name);

    /**
     * Get the name of fault-tolerance entry
     *
     * @param handlerMethod Spring MVC {@link HandlerMethod Handler Method}
     * @return non-null
     */
    protected String getEntryName(HandlerMethod handlerMethod) {
        String moduleName = getModule().name();
        Method method = handlerMethod.getMethod();
        String signature = getSignature(method);
        return "spring:webmvc:" + moduleName + "@" + signature;
    }

    /**
     * Get the {@link C configuration} by the specified name
     *
     * @param configName the specified configuration name
     * @return if the {@link C configuration} can't be found by the specified configuration name,
     * {@link #getDefaultConfiguration()} will be used as default
     */
    protected C getConfiguration(String configName) {
        return registry.getConfiguration(configName).orElse(getDefaultConfiguration());
    }

    /**
     * Get the default {@link C configuration}
     *
     * @return non-null
     */
    public final C getDefaultConfiguration() {
        return registry.getDefaultConfig();
    }

    /**
     * Get the class of Resilience4j's entry
     *
     * @return non-null
     */
    public final Class<E> getEntryClass() {
        return this.entryClass;
    }

    /**
     * Get the class of Resilience4j's configuration
     *
     * @return non-null
     */
    public final Class<C> getConfigurationClass() {
        return this.configurationClass;
    }

    /**
     * Get the {@link Resilience4jModule Resilience4j's module}
     *
     * @return non-null
     */
    public final Resilience4jModule getModule() {
        return module;
    }

}
