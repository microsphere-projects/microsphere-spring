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

import io.microsphere.spring.test.junit.jupiter.SpringLoggingTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static io.microsphere.spring.context.annotation.BeanCapableImportCandidate.NO_CLASS_TO_IMPORT;
import static io.microsphere.util.ArrayUtils.EMPTY_STRING_ARRAY;
import static io.microsphere.util.ClassUtils.getTypeName;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * {@link BeanCapableImportCandidate} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see 1.0.0
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        BeanCapableImportCandidateTest.class
})
@Import(value = {
        BeanCapableImportCandidateTest.MyImportSelector.class,
        BeanCapableImportCandidateTest.MyImportBeanDefinitionRegistrar.class
})
@SpringLoggingTest
class BeanCapableImportCandidateTest {

    @Autowired
    private MyImportSelector myImportSelector;

    @Autowired
    private MyImportBeanDefinitionRegistrar myImportBeanDefinitionRegistrar;

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    @Autowired
    private ResourceLoader resourceLoader;

    @Test
    void test() {
        assertBeanCapableImportCandidate(this.myImportSelector);
        assertBeanCapableImportCandidate(this.myImportBeanDefinitionRegistrar);
    }

    void assertBeanCapableImportCandidate(BeanCapableImportCandidate candidate) {
        assertSame(EMPTY_STRING_ARRAY, NO_CLASS_TO_IMPORT);

        assertNotNull(candidate.logger);
        assertEquals(getTypeName(candidate), candidate.logger.getName());

        assertSame(candidate.beanFactory, this.beanFactory);
        assertSame(candidate.getBeanFactory(), this.beanFactory);

        assertSame(candidate.applicationContext, this.applicationContext);
        assertSame(candidate.getApplicationContext(), this.applicationContext);

        assertSame(candidate.environment, this.environment);
        assertSame(candidate.getEnvironment(), this.environment);

        assertSame(candidate.resourceLoader, this.resourceLoader);
        assertSame(candidate.getResourceLoader(), this.resourceLoader);

        assertSame(candidate.classLoader, this.resourceLoader.getClassLoader());
        assertSame(candidate.getClassLoader(), this.resourceLoader.getClassLoader());
    }

    static class MyImportSelector extends BeanCapableImportCandidate implements ImportSelector {

        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
            return EMPTY_STRING_ARRAY;
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