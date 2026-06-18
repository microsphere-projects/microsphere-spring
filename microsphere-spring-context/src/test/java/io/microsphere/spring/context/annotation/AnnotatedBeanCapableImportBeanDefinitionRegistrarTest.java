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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.type.StandardAnnotationMetadata;

import static io.microsphere.spring.beans.BeanUtils.initializeBean;

/**
 * {@link AnnotatedBeanCapableImportBeanDefinitionRegistrar} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see AnnotatedBeanCapableImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
@ImportOptional(value = {
        "io.microsphere.spring.context.annotation.AnnotatedBeanCapableImportBeanDefinitionRegistrarTest",
})
class AnnotatedBeanCapableImportBeanDefinitionRegistrarTest {

    private DefaultListableBeanFactory beanFactory;

    private ConfigurableApplicationContext context;

    private StandardAnnotationMetadata metadata;

    private AutoRegistrationBeanRegistrar registrar;

    @BeforeEach
    void setUp() {
        this.beanFactory = new DefaultListableBeanFactory();
        this.context = new AnnotationConfigApplicationContext(beanFactory);
        this.context.refresh();
        this.metadata = new StandardAnnotationMetadata(AnnotatedBeanCapableImportBeanDefinitionRegistrarTest.class);
        this.registrar = new AutoRegistrationBeanRegistrar();
        initializeBean(this.registrar, this.context);
    }

    @Test
    void testRegisterBeanDefinitions() {
        this.registrar.registerBeanDefinitions(this.metadata, this.beanFactory);
    }
}