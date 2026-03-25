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

import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Indicates one or more <em>component classes</em> with names to import, Similar to {@link Import}.
 * <p>
 * The differents between them:
 * <ul>
 *     <li>{@link ImportOptional} allows one or more component classes is absent in the classpath.
 *     If the target class is not found, it indicates nothing to be imported.
 *     However, {@link Import} must ensure any {@link Class class} of {@link Component} is present.
 *     </li>
 *     <li>{@link ImportOptional}'s {@link #value()} is not type-safe attribute.</li>
 * </ul>
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see Import
 * @since 1.0.0
 */
@Target(TYPE)
@Retention(RUNTIME)
@Documented
@Import(ImportOptionalSelector.class)
public @interface ImportOptional {

    /**
     * The class names of the Spring {@link Component Components}
     *
     * @return non-null
     */
    String[] value();
}