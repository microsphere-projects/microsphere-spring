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
import io.microsphere.constants.PropertyConstants;
import io.microsphere.spring.context.ConfigurableApplicationContextInitializer;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.PriorityOrdered;
import org.springframework.core.env.ConfigurableEnvironment;

import static io.microsphere.constants.SymbolConstants.DOT;
import static io.microsphere.spring.constants.PropertyConstants.MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX;
import static java.lang.Boolean.parseBoolean;

/**
 * An {@link ApplicationContextInitializer} that registers processors to publish Spring bean lifecycle events
 * during context initialization.
 * <p>
 * This initializer executes with the highest priority to guarantee that bean event publishing capabilities
 * are established before other components are processed. It automatically registers an
 * {@link EventPublishingBeanBeforeProcessor} as a {@link org.springframework.beans.factory.config.BeanFactoryPostProcessor}.
 *
 * <h3>Configuration Properties</h3>
 * <ul>
 *     <li>{@code microsphere.spring.event-publishing-bean.enabled} -
 *         Whether to enable the EventPublishingBean* (default: {@code false}).</li>
 * </ul>
 *
 * <h3>Example Usage</h3>
 * <h4>1. Configuration</h4>
 * <p><strong> application.properties (Spring Boot):</strong></p>
 * <pre>
 * microsphere.spring.event-publishing-bean.enabled=true
 * </pre>
 *
 * <p><strong> spring.factories (Spring Boot):</strong></p>
 * <pre>{@code
 * org.springframework.context.ApplicationContextInitializer=\
 * io.microsphere.spring.context.event.EventPublishingBeanInitializer
 * }</pre>
 *
 * <h4>2. Programmatic</h4>
 * <pre>{@code
 * new SpringApplicationBuilder(MyApplication.class)
 *  .initializers(new EventPublishingBeanInitializer())
 *  .properties("microsphere.spring.event-publishing-bean.enabled=true")
 *  .run(args);
 * }</pre>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EventPublishingBeanBeforeProcessor
 * @see EventPublishingBeanAfterProcessor
 * @see BeanListeners
 * @see BeanListener
 * @see BeanListenerAdapter
 * @see BeanFactoryListeners
 * @see BeanFactoryListener
 * @see BeanFactoryListenerAdapter
 * @see ApplicationContextInitializer
 * @see PriorityOrdered
 * @since 1.0.0
 */
public class EventPublishingBeanInitializer extends ConfigurableApplicationContextInitializer implements PriorityOrdered {

    /**
     * The prefix of the property name of EventPublishingBean : "microsphere.spring.event-publishing-bean."
     */
    public static final String PROPERTY_NAME_PREFIX = MICROSPHERE_SPRING_PROPERTY_NAME_PREFIX + "event-publishing-bean" + DOT;

    private static final String DEFAULT_ENABLED = "false";

    /**
     * The property name of {@link EventPublishingBeanInitializer} to be 'enabled' : "microsphere.spring.event-publishing-bean.enabled"
     */
    @ConfigurationProperty(
            type = boolean.class,
            defaultValue = DEFAULT_ENABLED,
            description = "Whether to enable the EventPublishingBean"
    )
    public static final String ENABLED_PROPERTY_NAME = PROPERTY_NAME_PREFIX + PropertyConstants.ENABLED_PROPERTY_NAME;

    /**
     * The default property value of {@link EventPublishingBeanInitializer} to be 'enabled'
     */
    public static final boolean DEFAULT_ENABLED_PROPERTY_VALUE = parseBoolean(DEFAULT_ENABLED);

    @Override
    protected void initialize(ConfigurableApplicationContext context, ConfigurableEnvironment environment) {
        addBeanBeforeEventPublishingProcessor(context);
    }

    private void addBeanBeforeEventPublishingProcessor(ConfigurableApplicationContext context) {
        context.addBeanFactoryPostProcessor(new EventPublishingBeanBeforeProcessor());
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }

    @Override
    protected String getEnabledPropertyName() {
        return ENABLED_PROPERTY_NAME;
    }

    @Override
    protected boolean getDefaultEnabled() {
        return DEFAULT_ENABLED_PROPERTY_VALUE;
    }
}