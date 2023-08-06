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

import java.util.Iterator;
import java.util.function.BiConsumer;

/**
 * {@link ApplicationEventInterceptor} Chain
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ApplicationEventInterceptor
 * @since 1.0.0
 */
class DefaultApplicationListenerInterceptorChain implements ApplicationListenerInterceptorChain {

    private final Iterator<ApplicationListenerInterceptor> iterator;

    private final BiConsumer<ApplicationListener<?>, ApplicationEvent> listenerAndEventConsumer;

    public DefaultApplicationListenerInterceptorChain(Iterable<ApplicationListenerInterceptor> interceptors,
                                                      BiConsumer<ApplicationListener<?>, ApplicationEvent> listenerAndEventConsumer) {
        this.iterator = interceptors.iterator();
        this.listenerAndEventConsumer = listenerAndEventConsumer;
    }

    @Override
    public void intercept(ApplicationListener<?> applicationListener, ApplicationEvent event) {
        while (iterator.hasNext()) {
            ApplicationListenerInterceptor interceptor = iterator.next();
            interceptor.intercept(applicationListener, event, this);
            return;
        }
        listenerAndEventConsumer.accept(applicationListener, event);
    }
}
