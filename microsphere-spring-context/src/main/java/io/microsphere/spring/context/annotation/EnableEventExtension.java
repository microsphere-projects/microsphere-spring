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
package io.microsphere.spring.context.annotation;

import io.microsphere.spring.context.event.ApplicationEventInterceptor;
import io.microsphere.spring.context.event.ApplicationListenerInterceptor;
import io.microsphere.spring.context.event.InterceptingApplicationEventMulticaster;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Import;
import org.springframework.context.support.AbstractApplicationContext;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Executor;

/**
 * Enables Spring's Event Extension
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(EventExtensionRegistrar.class)
public @interface EnableEventExtension {

    /**
     * No {@link Executor} Present
     */
    String NO_EXECUTOR = "N/E";

    /**
     * The {@link ApplicationEvent ApplicationEvents} and {@link ApplicationListener ApplicationListeners}
     * should be intercepted by {@link ApplicationEventInterceptor} and {@link ApplicationListenerInterceptor}
     * respectively or not.
     *
     * @return {@link InterceptingApplicationEventMulticaster} will be
     * {@link AbstractApplicationContext#initApplicationEventMulticaster() initialized} by the Spring
     * {@link ApplicationContext} if <code>true</code>. The default value is <code>false</code>.
     * @see InterceptingApplicationEventMulticaster
     */
    boolean intercepted() default false;

    /**
     * A qualifier value for the asynchronous listening of {@link ApplicationListener ApplicationListeners}.
     *
     * @return the qualifier value (or the bean name) of a specific Executor or TaskExecutor bean definition.
     * The default value is an empty string indicates no Executor or TaskExecutor used.
     */
    String executorForListener() default NO_EXECUTOR;

}
