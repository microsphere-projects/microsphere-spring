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
package io.microsphere.spring.context;

import io.microsphere.logging.Logger;
import io.microsphere.logging.LoggerFactory;
import io.microsphere.util.BaseUtils;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.SpringVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static io.microsphere.spring.util.BeanFactoryUtils.getBeanPostProcessors;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;
import static io.microsphere.util.ClassUtils.cast;

/**
 * {@link ApplicationContext} Utilities
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
public abstract class ApplicationContextUtils extends BaseUtils {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationContextUtils.class);

    /**
     * The {@link org.springframework.context.support.ApplicationContextAwareProcessor} Class Name (Internal).
     *
     * @see org.springframework.context.support.ApplicationContextAwareProcessor
     */
    public static final String APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME = "org.springframework.context.support.ApplicationContextAwareProcessor";
    /**
     * The {@link org.springframework.context.support.ApplicationContextAwareProcessor} Class (Internal).
     *
     * @see org.springframework.context.support.ApplicationContextAwareProcessor
     */
    private static final Class<?> APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS = resolveClass(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME);

    public static ConfigurableApplicationContext asConfigurableApplicationContext(ApplicationContext context) {
        return cast(context, ConfigurableApplicationContext.class);
    }

    public static ApplicationContext asApplicationContext(BeanFactory beanFactory) {
        return cast(beanFactory, ApplicationContext.class);
    }

    /**
     * Get the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     *
     * @return the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     */
    @Nonnull
    public static BeanPostProcessor getApplicationContextAwareProcessor(ConfigurableApplicationContext context) {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        return getApplicationContextAwareProcessor(beanFactory);
    }

    /**
     * Get the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     *
     * @return the {@link org.springframework.context.support.ApplicationContextAwareProcessor}
     */
    @Nullable
    public static BeanPostProcessor getApplicationContextAwareProcessor(BeanFactory beanFactory) {
        List<BeanPostProcessor> beanPostProcessors = getBeanPostProcessors(beanFactory);
        BeanPostProcessor applicationContextAwareProcessor = null;
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            if (beanPostProcessor.getClass().equals(APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS)) {
                applicationContextAwareProcessor = beanPostProcessor;
                break;
            }
        }
        if (applicationContextAwareProcessor == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("The BeanPostProcessor[class : '{}' , present : {}] was not added in the BeanFactory[{}] @ Spring Framework '{}'",
                        APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS_NAME,
                        APPLICATION_CONTEXT_AWARE_PROCESSOR_CLASS != null,
                        beanFactory,
                        SpringVersion.getVersion());
            }
        }
        return applicationContextAwareProcessor;
    }

}
