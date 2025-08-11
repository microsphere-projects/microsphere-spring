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

package io.microsphere.spring.webflux.annotation;

import io.microsphere.logging.Logger;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

import static io.microsphere.logging.LoggerFactory.getLogger;
import static io.microsphere.spring.core.annotation.AnnotationUtils.getAnnotationAttributes;

/**
 * The {@link ImportBeanDefinitionRegistrar} class for {@link EnableWebFluxExtension Spring WebFlux extensions}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableWebFluxExtension
 * @see ImportBeanDefinitionRegistrar
 * @since 1.0.0
 */
class WebFluxExtensionBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar {

    private static final Logger logger = getLogger(WebFluxExtensionBeanDefinitionRegistrar.class);

    public static final Class<EnableWebFluxExtension> ANNOTATION_CLASS = EnableWebFluxExtension.class;

    public static final String ANNOTATION_CLASS_NAME = ANNOTATION_CLASS.getName();

    @Override
    public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
        AnnotationAttributes attributes = getAttributes(metadata);
    }

    private AnnotationAttributes getAttributes(AnnotationMetadata metadata) {
        return getAnnotationAttributes(metadata, ANNOTATION_CLASS_NAME);
    }
}
