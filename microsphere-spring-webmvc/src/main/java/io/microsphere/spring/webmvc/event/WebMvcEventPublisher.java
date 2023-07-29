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
package io.microsphere.spring.webmvc.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.web.context.WebApplicationContext;

/**
 * The {@link SmartInitializingSingleton} class publishes the Spring WebMVC Events earlier than
 * {@link EventPublishingWebMvcListener} that will be triggered when {@link ContextRefreshedEvent} is raised.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingWebMvcListener
 * @see SmartInitializingSingleton
 * @since 1.0.0
 */
public class WebMvcEventPublisher implements SmartInitializingSingleton, ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(WebMvcEventPublisher.class);

    private WebApplicationContext context;

    @Override
    public void afterSingletonsInstantiated() {
        WebApplicationContext context = this.context;
        if (context == null) {
            return;
        }

        EventPublishingWebMvcListener listener = new EventPublishingWebMvcListener(context);
        listener.publishWebEvents(context);
    }


    @Override
    public void setApplicationContext(ApplicationContext context) throws BeansException {
        if (context instanceof WebApplicationContext) {
            this.context = (WebApplicationContext) context;
        } else {
            logger.info("The Non-Web ApplicationContext[id : '{}'] executes the ApplicationContextAware callback",
                    context.getId());
        }
    }
}
