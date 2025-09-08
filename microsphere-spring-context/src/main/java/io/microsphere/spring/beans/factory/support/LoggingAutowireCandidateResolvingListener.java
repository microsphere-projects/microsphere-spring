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
package io.microsphere.spring.beans.factory.support;

import io.microsphere.logging.Logger;
import org.springframework.beans.factory.config.DependencyDescriptor;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * A logging implementation of the {@link AutowireCandidateResolvingListener} interface that logs events related to autowire
 * candidate resolution in Spring bean factories.
 *
 * <p>This class provides detailed trace-level logging for two key resolution processes:
 * <ul>
 *     <li>{@link #suggestedValueResolved(DependencyDescriptor, Object) Suggested value resolution}</li>
 *     <li>{@link #lazyProxyResolved(DependencyDescriptor, String, Object) Lazy proxy resolution}</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * When a suggested value is resolved:
 * <pre>
 * TRACE: The suggested value for field injection into [class io.microsphere.example.MyService] was resolved : someExpression
 * </pre>
 *
 * <p>When a lazy proxy is resolved:
 * <pre>
 * TRACE: The lazy proxy[descriptor : method parameter in io.microsphere.example.MyService, bean name : 'myBean'] was resolved : com.sun.proxy.$Proxy12
 * </pre>
 *
 * <p><strong>Note:</strong> This logger uses trace level logging, which should be enabled in your logging configuration to see these messages.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see AutowireCandidateResolvingListener
 * @see Logger
 * @since 1.0.0
 */
public class LoggingAutowireCandidateResolvingListener implements AutowireCandidateResolvingListener {

    private static final Logger logger = getLogger(LoggingAutowireCandidateResolvingListener.class);

    @Override
    public void suggestedValueResolved(DependencyDescriptor descriptor, Object suggestedValue) {
        if (suggestedValue != null) {
            log("The suggested value for {} was resolved : {}", descriptor, suggestedValue);
        }
    }

    @Override
    public void lazyProxyResolved(DependencyDescriptor descriptor, String beanName, Object proxy) {
        if (proxy != null) {
            log("The lazy proxy[descriptor : {} , bean name : '{}'] was resolved : {}", descriptor, beanName, proxy);
        }
    }

    protected void log(String messagePattern, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(messagePattern, args);
        }
    }

}
