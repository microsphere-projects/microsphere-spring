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

package io.microsphere.spring.web.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * The enumeration of where the {@link RequestAttributes Request Context} stores.
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see RequestAttributes
 * @since 1.0.0
 */
public enum RequestContextStrategy {

    /**
     * Default strategy keeps the original {@link RequestAttributes} storage strategy
     */
    DEFAULT,

    /**
     * {@link ThreadLocal} strategy stores the {@link RequestAttributes} in {@link ThreadLocal}
     *
     * @see ThreadLocal
     * @see RequestContextHolder#setRequestAttributes(RequestAttributes)
     */
    THREAD_LOCAL,

    /**
     * {@link InheritableThreadLocal}strategy stores the {@link RequestAttributes} in {@link InheritableThreadLocal}
     *
     * @see InheritableThreadLocal
     * @see RequestContextHolder#setRequestAttributes(RequestAttributes, boolean)
     */
    INHERITABLE_THREAD_LOCAL;
}
