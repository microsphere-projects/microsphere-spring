/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.microsphere.spring.core.convert.support;

import io.microsphere.logging.Logger;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.format.support.DefaultFormattingConversionService;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.beans.BeanUtils.getBeanIfAvailable;
import static io.microsphere.spring.beans.BeanUtils.isBeanPresent;
import static io.microsphere.text.FormatUtils.format;
import static org.springframework.context.ConfigurableApplicationContext.CONVERSION_SERVICE_BEAN_NAME;
import static org.springframework.context.ConfigurableApplicationContext.ENVIRONMENT_BEAN_NAME;

/**
 * The class to resolve a singleton instance of {@link ConversionService} that may be retrieved from Spring
 * {@link ConfigurableApplicationContext#CONVERSION_SERVICE_BEAN_NAME built-in bean} or create a new one.
 *
 * @see ConversionService
 * @see ConfigurableApplicationContext#CONVERSION_SERVICE_BEAN_NAME
 * @see SingletonBeanRegistry#registerSingleton(String, Object)
 * @since 1.0.0
 */
public class ConversionServiceResolver {

    /**
     * The bean name of a singleton instance of {@link ConversionService} has been resolved
     */
    public static final String RESOLVED_CONVERSION_SERVICE_BEAN_NAME = "resolved-" + CONVERSION_SERVICE_BEAN_NAME;

    private final Logger logger = getLogger(getClass());

    private final ConfigurableBeanFactory beanFactory;

    public ConversionServiceResolver(ConfigurableBeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    public ConversionService resolve() {
        return resolve(true);
    }

    public ConversionService resolve(boolean requireToRegister) {

        ConversionService conversionService = getResolvedBeanIfAvailable();

        if (conversionService == null) { // If not resolved, try to get from ConfigurableBeanFactory
            conversionService = getFromBeanFactory();
        }

        if (conversionService == null) { // If not found, try to get the bean from BeanFactory
            trace("The conversionService instance can't be found in Spring ConfigurableBeanFactory.getConversionService()");
            conversionService = getFromEnvironment();
        }

        if (conversionService == null) {  // If not found, try to get the bean from ConfigurableEnvironment
            trace("The conversionService instance can't be found in Spring ConfigurableEnvironment.getConversionService()");
            conversionService = getIfAvailable();
        }
        if (conversionService == null) { // If not found, will create an instance of ConversionService as default
            conversionService = createDefaultConversionService();
        }

        if (!isBeanPresent(beanFactory, RESOLVED_CONVERSION_SERVICE_BEAN_NAME, ConversionService.class)
                && requireToRegister) { // To register a singleton into SingletonBeanRegistry(ConfigurableBeanFactory)
            beanFactory.registerSingleton(RESOLVED_CONVERSION_SERVICE_BEAN_NAME, conversionService);
        }

        return conversionService;
    }

    private ConversionService getFromEnvironment() {
        if (beanFactory.containsBean(ENVIRONMENT_BEAN_NAME)) {
            ConfigurableEnvironment environment = beanFactory.getBean(ENVIRONMENT_BEAN_NAME, ConfigurableEnvironment.class);
            return environment.getConversionService();
        }
        return null;
    }

    private ConversionService getResolvedBeanIfAvailable() {
        return getBeanIfAvailable(beanFactory, RESOLVED_CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
    }

    private ConversionService getFromBeanFactory() {
        return beanFactory.getConversionService();
    }

    private ConversionService getIfAvailable() {
        return getBeanIfAvailable(beanFactory, CONVERSION_SERVICE_BEAN_NAME, ConversionService.class);
    }

    /**
     * Create the instance of {@link DefaultFormattingConversionService} as the default,
     * this method is allow to be override by the sub-class.
     *
     * @return non-null
     */
    protected ConversionService createDefaultConversionService() {
        return new DefaultFormattingConversionService();
    }

    private void trace(String message, Object... args) {
        if (logger.isTraceEnabled()) {
            logger.trace(args.length < 1 ? message : format(message, args));
        }
    }
}
