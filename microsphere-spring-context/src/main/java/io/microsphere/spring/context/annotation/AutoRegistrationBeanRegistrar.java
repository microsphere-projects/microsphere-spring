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

import io.microsphere.spring.context.config.AutoRegistrationBean;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.io.support.SpringFactoriesLoader;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;

import static io.microsphere.spring.beans.factory.support.BeanRegistrar.registerBeanDefinition;
import static io.microsphere.spring.constants.PropertyConstants.DEFAULT_AUTO_REGISTERED_VALUE;
import static io.microsphere.spring.context.annotation.EnableAutoRegistrationBean.BEANS_AUTO_REGISTERED_PROEPRTY_NAME;
import static io.microsphere.spring.context.config.AutoRegistrationBean.getAutoRegisteredPropertyName;
import static io.microsphere.spring.core.io.support.SpringFactoriesLoaderUtils.loadFactories;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

/**
 * {@link ImportSelector} class for {@link EnableAutoRegistrationBean}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableAutoRegistrationBean
 * @see AutoRegistrationBean
 * @see SpringFactoriesLoader
 * @since 1.0.0
 */
class AutoRegistrationBeanRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        if (!isEnabled()) {
            if (logger.isTraceEnabled()) {
                logger.trace("The @EnableAutoRegistrationBean was disabled by property[{} = false]",
                        BEANS_AUTO_REGISTERED_PROEPRTY_NAME);
            }
            return;
        }

        ConfigurableApplicationContext context = getApplicationContext();
        List<AutoRegistrationBean> autoRegistrationBeans = loadFactories(context, AutoRegistrationBean.class);
        registerAutoRegisteredBeans(autoRegistrationBeans, registry);
    }

    private boolean isEnabled() {
        return this.environment.getProperty(BEANS_AUTO_REGISTERED_PROEPRTY_NAME, boolean.class, DEFAULT_AUTO_REGISTERED_VALUE);
    }

    private void registerAutoRegisteredBeans(List<AutoRegistrationBean> autoRegistrationBeans, BeanDefinitionRegistry registry) {
        for (AutoRegistrationBean autoRegistrationBean : autoRegistrationBeans) {
            registerAutoRegisteredBean(autoRegistrationBean, registry);
        }
    }

    private void registerAutoRegisteredBean(AutoRegistrationBean autoRegistrationBean, BeanDefinitionRegistry registry) {
        String beanName = autoRegistrationBean.getBeanName();
        if (registry.containsBeanDefinition(beanName)) {
            if (logger.isWarnEnabled()) {
                logger.warn("The BeanDefinition[{}] was registered already!", autoRegistrationBean.getDescription());
            }
            return;
        }

        if (!autoRegistrationBean.isAutoRegistered(this.environment)) {
            if (logger.isTraceEnabled()) {
                logger.trace("The Bean[{}] is not auto registered because of the property[{} = false]",
                        autoRegistrationBean.getDescription(), getAutoRegisteredPropertyName(beanName));
            }
            return;
        }

        Class<AutoRegistrationBean> beanType = (Class<AutoRegistrationBean>) autoRegistrationBean.getBeanType();
        String scope = autoRegistrationBean.getScope();
        BeanDefinitionBuilder beanDefinitionBuilder = genericBeanDefinition(beanType, () -> autoRegistrationBean)
                .setScope(scope);

        autoRegistrationBean.customize(beanDefinitionBuilder);

        registerBeanDefinition(registry, beanName, beanDefinitionBuilder.getBeanDefinition());
    }
}
