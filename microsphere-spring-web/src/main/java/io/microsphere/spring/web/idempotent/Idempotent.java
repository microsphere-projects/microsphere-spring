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

import io.microsphere.spring.web.util.RequestSource;
import io.microsphere.spring.web.util.ResponseTarget;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.web.util.RequestSource.PARAMETER;
import static io.microsphere.spring.web.util.ResponseTarget.HEADER;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Idempotent Annotation
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @since 1.0.0
 */
@Target({TYPE, METHOD})
@Retention(RUNTIME)
@Inherited
@Documented
public @interface Idempotent {

    String DEFAULT_TOKEN_NAME = "_token_";

    /**
     * The name of the token
     *
     * @return {@link #DEFAULT_TOKEN_NAME "_token_"} as default
     */
    String tokenName() default DEFAULT_TOKEN_NAME;

    /**
     * The source of the token value
     *
     * @return {@link RequestSource#PARAMETER} as default
     * @see RequestSource
     */
    RequestSource source() default PARAMETER;

    /**
     * The target of the token value
     *
     * @return {@link ResponseTarget#HEADER} as default
     * @see ResponseTarget
     */
    ResponseTarget target() default HEADER;
}