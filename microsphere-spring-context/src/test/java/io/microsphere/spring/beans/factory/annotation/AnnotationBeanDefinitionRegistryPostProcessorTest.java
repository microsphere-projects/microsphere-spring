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

import io.microsphere.spring.context.annotation.ExposingClassPathBeanDefinitionScanner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.Map;

import static io.microsphere.spring.beans.factory.annotation.AnnotationBeanDefinitionRegistryPostProcessor.getAnnotation;
import static io.microsphere.spring.beans.factory.config.BeanDefinitionUtils.genericBeanDefinition;
import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link AnnotationBeanDefinitionRegistryPostProcessor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        AnnotationBeanDefinitionRegistryPostProcessorTest.ServiceAnnotationBeanDefinitionRegistryPostProcessor.class,
        AnnotationBeanDefinitionRegistryPostProcessorTest.class
})
@Configuration
class AnnotationBeanDefinitionRegistryPostProcessorTest {

    @Service
    static class MyService {
    }

    @Autowired
    private MyService myService;

    @Qualifier("stringBean")
    @Autowired
    private String stringBean;

    @Autowired
    private DefaultListableBeanFactory beanFactory;

    @Autowired
    private AnnotationBeanDefinitionRegistryPostProcessor processor;

    @Test
    void test() {
        assertNotNull(myService);
        assertEquals("Hello,World", stringBean);
    }

    @Test
    void testGetAnnotation() {
        assertNotNull(getAnnotation(MyService.class, Service.class));
    }

    @Test
    void testRegisterBeanDefinitions() {
        this.processor.registerBeanDefinitions(this.beanFactory);
    }

    @Test
    void testLogBeanDefinitions() {
        this.processor.logBeanDefinitions(emptySet(), "com.acme");
    }

    @Test
    void testPutBeanDefinitions() {
        BeanDefinitionHolder beanDefinitionHolder = new BeanDefinitionHolder(genericBeanDefinition(this.getClass()), "test");
        this.processor.putBeanDefinitions(emptyMap(), beanDefinitionHolder);
    }

    @Test
    void testGetter() {
        assertNotNull(this.processor.getPackagesToScan());
        assertNotNull(this.processor.getSupportedAnnotationTypes());
        assertNotNull(this.processor.getSupportedAnnotationTypeNames());
        assertNotNull(this.processor.getBeanFactory());
        assertNotNull(this.processor.getEnvironment());
        assertNotNull(this.processor.getResourceLoader());
        assertNotNull(this.processor.getClassLoader());
    }

    @Target({TYPE, ANNOTATION_TYPE})
    @Retention(RUNTIME)
    @Documented
    @Inherited
    @interface Service {
    }

    static class ServiceAnnotationBeanDefinitionRegistryPostProcessor extends
            AnnotationBeanDefinitionRegistryPostProcessor {

        public ServiceAnnotationBeanDefinitionRegistryPostProcessor() {
            super(Service.class, Service.class);
        }

        @Override
        protected void registerExtentedBeanDefinitions(ExposingClassPathBeanDefinitionScanner scanner,
                                                       Map<String, AnnotatedBeanDefinition> primaryBeanDefinitions,
                                                       String[] basePackages) {
            scanner.registerSingleton("stringBean", "Hello,World");

        }
    }
}
