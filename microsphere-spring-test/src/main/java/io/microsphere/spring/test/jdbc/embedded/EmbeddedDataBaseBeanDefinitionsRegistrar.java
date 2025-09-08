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

package io.microsphere.spring.test.jdbc.embedded;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import java.lang.annotation.Annotation;

import static org.springframework.core.annotation.AnnotationAttributes.fromMap;

/**
 * {@link EnableEmbeddedDatabases} {@link ImportBeanDefinitionRegistrar}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy<a/>
 * @see EnableEmbeddedDatabases
 * @see EmbeddedDataBaseBeanDefinitionRegistrar
 * @since 1.0.0
 */
class EmbeddedDataBaseBeanDefinitionsRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = EnableEmbeddedDatabases.class;

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = fromMap(metadata.getAnnotationAttributes(ANNOTATION_TYPE.getName()));
        EmbeddedDataBaseBeanDefinitionRegistrar registrar = new EmbeddedDataBaseBeanDefinitionRegistrar();
        for (AnnotationAttributes valueAttributes : attributes.getAnnotationArray("value")) {
            registrar.registerBeanDefinitions(valueAttributes, registry);
        }
    }
}
