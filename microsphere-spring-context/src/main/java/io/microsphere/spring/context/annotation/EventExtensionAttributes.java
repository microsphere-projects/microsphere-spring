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
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.type.AnnotationMetadata;

/**
 * The {@link ResolvablePlaceholderAnnotationAttributes} for {@link EnableEventExtension @EnableEventExtension}
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see EnableEventExtension
 * @since 1.0.0
 */
class EventExtensionAttributes extends ResolvablePlaceholderAnnotationAttributes<EnableEventExtension> {

    /**
     * The {@link EnableEventExtension @EnableEventExtension} annotation class name
     */
    static final String ANNOTATION_CLASS_NAME = EnableEventExtension.class.getName();

    /**
     * The attribute name of {@link EnableEventExtension#intercepted()}
     */
    static final String INTERCEPTED_ATTRIBUTE_NAME = "intercepted";

    /**
     * The attribute name of {@link EnableEventExtension#executorForListener()}
     */
    static final String EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME = "executorForListener";

    EventExtensionAttributes(AnnotationMetadata metadata, @Nullable PropertyResolver propertyResolver) {
        super(metadata.getAnnotationAttributes(ANNOTATION_CLASS_NAME), EnableEventExtension.class, propertyResolver);
    }

    @Nonnull
    public boolean isIntercepted() {
        return getBoolean(INTERCEPTED_ATTRIBUTE_NAME);
    }

    @Nonnull
    public String getExecutorForListener() {
        return getString(EXECUTOR_FOR_LISTENER_ATTRIBUTE_NAME);
    }
}
