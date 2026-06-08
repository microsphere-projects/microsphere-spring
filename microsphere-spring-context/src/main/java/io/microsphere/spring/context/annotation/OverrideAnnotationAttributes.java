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

import io.microsphere.annotation.Nonnull;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The meta-annotation that indicates the attributes of the annotation should be overridden.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Import
 * @see BeanCapableImportCandidate
 * @since 1.0.0
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface OverrideAnnotationAttributes {

    /**
     * The strategy class for overriding annotation attributes.
     *
     * @return {@link ConfigurationPropertyOverrideAnnotationAttributesStrategy} as default, the implementation class
     * must be a concrete class and have a default constructor.
     */
    @Nonnull
    Class<? extends OverrideAnnotationAttributesStrategy> strategy() default ConfigurationPropertyOverrideAnnotationAttributesStrategy.class;
}
