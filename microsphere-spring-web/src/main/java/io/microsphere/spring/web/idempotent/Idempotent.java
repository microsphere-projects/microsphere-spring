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

import io.microsphere.spring.web.util.WebSource;
import io.microsphere.spring.web.util.WebTarget;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static io.microsphere.spring.web.util.WebSource.REQUEST_HEADER;
import static io.microsphere.spring.web.util.WebTarget.RESPONSE_HEADER;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

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
     * The request methods for idempotent validation.
     *
     * @return {@link RequestMethod#POST} and {@link RequestMethod#PATCH} as default
     */
    RequestMethod[] validatedMethod() default {POST, PATCH};

    /**
     * The source of the token value
     *
     * @return {@link WebSource#REQUEST_HEADER} as default
     * @see WebSource
     */
    WebSource source() default REQUEST_HEADER;

    /**
     * The target of the token value
     *
     * @return {@link WebTarget#RESPONSE_HEADER} as default
     * @see WebTarget
     */
    WebTarget target() default RESPONSE_HEADER;
}