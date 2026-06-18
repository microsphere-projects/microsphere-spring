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
package io.microsphere.spring.beans.factory.annotation;

import io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrar;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

/**
 * The {@link ImportBeanDefinitionRegistrar Registrar class} for {@link EnableConfigurationBeanBindings}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class ConfigurationBeanBindingsRegister extends AnnotatedBeanCapableImportBeanDefinitionRegistrar<EnableConfigurationBeanBindings> {

    @Override
    protected void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry,
                                           BeanNameGenerator importBeanNameGenerator,
                                           ResolvablePlaceholderAnnotationAttributes<EnableConfigurationBeanBindings> annotationAttributes) {

        ConfigurationBeanBindingRegistrar registrar = new ConfigurationBeanBindingRegistrar();

        registrar.setEnvironment(getEnvironment());

        for (AnnotationAttributes attributes : annotationAttributes.getAnnotationArray("value")) {
            registrar.registerConfigurationBeanDefinitions(attributes, registry);
        }
    }
}