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

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * {@link ApplicationListenerInterceptor} Chain
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public interface ApplicationListenerInterceptorChain {

    /**
     * Causes the next interceptor in the chain to be invoked, or if the calling interceptor is the last interceptor
     * in the chain, causes the resource at the end of the chain to be invoked.
     *
     * @param applicationListener {@link ApplicationListener}
     * @param event               {@link ApplicationEvent}
     */
    void doIntercept(ApplicationListener<?> applicationListener, ApplicationEvent event);
}
