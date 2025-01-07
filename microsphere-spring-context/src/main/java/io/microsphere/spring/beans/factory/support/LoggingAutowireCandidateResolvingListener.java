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
 * {@link Logger logging} {@link AutowireCandidateResolvingListener}
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
