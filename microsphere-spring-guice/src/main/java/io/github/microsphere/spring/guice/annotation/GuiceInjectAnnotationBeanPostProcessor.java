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
package io.github.microsphere.spring.guice.annotation;

import com.google.inject.Inject;
import io.github.microsphere.spring.beans.factory.annotation.AnnotatedInjectionBeanPostProcessor;
import org.springframework.core.annotation.AnnotationAttributes;

import java.lang.annotation.Annotation;

/**
 * Guice {@link Inject} Annotation {@link org.springframework.beans.factory.config.BeanPostProcessor}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
class GuiceInjectAnnotationBeanPostProcessor extends AnnotatedInjectionBeanPostProcessor {

    private static final Class<? extends Annotation> ANNOTATION_TYPE = Inject.class;

    private static final String OPTIONAL_ATTRIBUTE_NAME = "optional";

    public GuiceInjectAnnotationBeanPostProcessor() {
        super(ANNOTATION_TYPE);
    }

    @Override
    protected boolean determineRequiredStatus(AnnotationAttributes attributes) {
        if (!attributes.containsKey(OPTIONAL_ATTRIBUTE_NAME)) {
            return false;
        }
        return Boolean.FALSE.equals(attributes.getBoolean(OPTIONAL_ATTRIBUTE_NAME));
    }
}
