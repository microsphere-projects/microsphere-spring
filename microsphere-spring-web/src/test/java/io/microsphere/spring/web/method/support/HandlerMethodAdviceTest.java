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

package io.microsphere.spring.web.method.support;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * {@link HandlerMethodAdvice} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodAdvice
 * @since 1.0.0
 */
class HandlerMethodAdviceTest {

    private HandlerMethodAdvice handlerMethodAdvice;

    @BeforeEach
    void setUp() {
        this.handlerMethodAdvice = new HandlerMethodAdvice() {
        };
    }

    @Test
    void testBeforeResolveArgument() throws Exception {
        this.handlerMethodAdvice.beforeResolveArgument(null, null, null);
    }

    @Test
    void testAfterResolveArgument() throws Exception {
        this.handlerMethodAdvice.afterResolveArgument(null, null, null, null);
    }

    @Test
    void testBeforeExecuteMethod() throws Exception {
        this.handlerMethodAdvice.beforeExecuteMethod(null, null, null);
    }

    @Test
    void testAfterExecuteMethod() throws Exception {
        this.handlerMethodAdvice.afterExecuteMethod(null, null, null, null, null);
    }
}