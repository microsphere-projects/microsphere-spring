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
package io.microsphere.spring.core.convert.annotation;

import io.microsphere.spring.core.convert.SpringConverterAdapter;
import io.microsphere.spring.core.convert.support.ConversionServiceResolver;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.ConverterRegistry;
import org.springframework.core.type.AnnotationMetadata;

/**
 * {@link EnableSpringConverterAdapter} {@link Configuration} class
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableSpringConverterAdapter
 * @see SpringConverterAdapter
 * @since 1.0.0
 */
class EnableSpringConverterAdapterRegistrar implements ImportSelector, BeanFactoryAware {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        return new String[0];
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        ConfigurableBeanFactory configurableBeanFactory = (ConfigurableBeanFactory) beanFactory;
        addSpringConverterAdapter(configurableBeanFactory);
    }

    private void addSpringConverterAdapter(ConfigurableBeanFactory beanFactory) {
        ConversionServiceResolver conversionServiceResolver = new ConversionServiceResolver(beanFactory);
        ConversionService conversionService = conversionServiceResolver.resolve();
        if (conversionService instanceof ConverterRegistry) {
            ConverterRegistry registry = (ConverterRegistry) conversionService;
            registry.addConverter(SpringConverterAdapter.INSTANCE);
        }
    }
}
