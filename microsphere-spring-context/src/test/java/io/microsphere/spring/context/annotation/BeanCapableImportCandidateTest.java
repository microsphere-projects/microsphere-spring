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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertSame;

/**
 * {@link BeanCapableImportCandidate} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        BeanCapableImportCandidateTest.class
})
@Import(value = {
        BeanCapableImportCandidateTest.MyImportSelector.class,
        BeanCapableImportCandidateTest.MyImportBeanDefinitionRegistrar.class
})
public class BeanCapableImportCandidateTest {

    @Autowired
    private MyImportSelector myImportSelector;

    @Autowired
    private MyImportBeanDefinitionRegistrar myImportBeanDefinitionRegistrar;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    public void test() {
        assertSame(myImportSelector.applicationContext, this.applicationContext);
        assertSame(myImportSelector.environment, this.environment);
        assertSame(myImportSelector.resourceLoader, this.resourceLoader);

        assertSame(myImportBeanDefinitionRegistrar.applicationEventPublisher, this.applicationContext);
        assertSame(myImportBeanDefinitionRegistrar.environment, this.environment);
        assertSame(myImportBeanDefinitionRegistrar.resourceLoader, this.resourceLoader);
    }


    static class MyImportSelector extends BeanCapableImportCandidate implements ImportSelector, ApplicationContextAware {


        private ApplicationContext applicationContext;


        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return new String[0];
        }

        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext = applicationContext;
        }
    }

    static class MyImportBeanDefinitionRegistrar extends BeanCapableImportCandidate implements ImportBeanDefinitionRegistrar,
            ApplicationEventPublisherAware {

        @Autowired
        private ApplicationEventPublisher applicationEventPublisher;

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        }

        @Override
        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            this.applicationEventPublisher = applicationEventPublisher;
        }
    }
}
