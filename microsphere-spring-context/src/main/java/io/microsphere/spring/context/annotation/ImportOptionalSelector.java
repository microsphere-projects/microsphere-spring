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

import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import static io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes.of;
import static io.microsphere.util.ClassLoaderUtils.resolveClass;

/**
 * The {@link ImportSelector} implementation for {@link ImportOptional @ImportOptional}.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ImportOptional
 * @see BeanCapableImportCandidate
 * @see ImportSelector
 * @since 1.0.0
 */
class ImportOptionalSelector extends BeanCapableImportCandidate implements ImportSelector {

    private static final Class<ImportOptional> ANNOTATION_TYPE = ImportOptional.class;

    @Override
    public String[] selectImports(AnnotationMetadata metadata) {
        String annotationClassName = ANNOTATION_TYPE.getName();
        Map<String, Object> annotationAttributes = metadata.getAnnotationAttributes(annotationClassName);
        ResolvablePlaceholderAnnotationAttributes attributes = of(annotationAttributes, ANNOTATION_TYPE, getEnvironment());
        String[] classNames = attributes.getStringArray("value");
        return Stream.of(classNames)
                .map(className -> resolveClass(className, getClassLoader()))
                .filter(Objects::nonNull)
                .map(Class::getName)
                .toArray(String[]::new);
    }
}