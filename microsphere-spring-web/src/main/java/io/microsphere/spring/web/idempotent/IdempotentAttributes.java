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

package io.microsphere.spring.web.idempotent;

import io.microsphere.annotation.Nonnull;
import io.microsphere.annotation.Nullable;
import io.microsphere.spring.core.annotation.ResolvablePlaceholderAnnotationAttributes;
import io.microsphere.spring.web.util.WebSource;
import io.microsphere.spring.web.util.WebTarget;
import org.springframework.core.env.PropertyResolver;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * {@link ResolvablePlaceholderAnnotationAttributes} for {@link Idempotent} annotation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see ResolvablePlaceholderAnnotationAttributes
 * @since 1.0.0
 */
public class IdempotentAttributes extends ResolvablePlaceholderAnnotationAttributes<Idempotent> {

    public IdempotentAttributes(Idempotent annotation, @Nullable PropertyResolver propertyResolver) {
        super(annotation, propertyResolver);
    }

    /**
     * Get the token name
     *
     * @return non-null
     * @see Idempotent#tokenName()
     */
    @Nonnull
    public String getTokenName() {
        return getString("tokenName");
    }

    /**
     * Get the request methods
     *
     * @return non-null
     * @see Idempotent#method()
     */
    @Nonnull
    public RequestMethod[] getMethod() {
        return (RequestMethod[]) get("method");
    }

    /**
     * Get the request source
     *
     * @return non-null
     * @see Idempotent#source()
     */
    @Nonnull
    public WebSource getSource() {
        return getEnum("source");
    }

    /**
     * Get the request target
     *
     * @return non-null
     * @see Idempotent#target()
     */
    @Nonnull
    public WebTarget getTarget() {
        return getEnum("target");
    }

    /**
     * Create the {@link Idempotent} annotation attributes
     *
     * @param annotation the {@link Idempotent} annotation
     * @return non-null
     */
    @Nonnull
    public static IdempotentAttributes of(Idempotent annotation) {
        return of(annotation, null);
    }

    /**
     * Create the {@link Idempotent} annotation attributes
     *
     * @param annotation       the {@link Idempotent} annotation
     * @param propertyResolver the {@link PropertyResolver}
     * @return non-null
     */
    @Nonnull
    public static IdempotentAttributes of(Idempotent annotation, @Nullable PropertyResolver propertyResolver) {
        return new IdempotentAttributes(annotation, propertyResolver);
    }
}
