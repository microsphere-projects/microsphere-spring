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
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import static io.microsphere.logging.LoggerFactory.getLogger;

/**
 * {@link ApplicationListenerInterceptor} for logging
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationListenerInterceptor
 * @since 1.0.0
 */
public class LoggingApplicationListenerInterceptor implements ApplicationListenerInterceptor {

    private static final Logger logger = getLogger(LoggingApplicationListenerInterceptor.class);

    @Override
    public void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event, ApplicationListenerInterceptorChain chain) {
        logger.info("Before intercepting application listener: {}", applicationListener);
        chain.intercept(applicationListener, event);
        logger.info("After intercepting application listener: {}", applicationListener);
    }
}