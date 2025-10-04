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


import org.junit.Before;
import org.junit.Test;

/**
 * {@link HandlerMethodArgumentInterceptor} Test
 *
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 * @see HandlerMethodArgumentInterceptor
 * @since 1.0.0
 */
public class HandlerMethodArgumentInterceptorTest {

    private HandlerMethodArgumentInterceptor interceptor;

    @Before
    public void setUp() {
        this.interceptor = new HandlerMethodArgumentInterceptor() {
        };
    }

    @Test
    public void testBeforeResolveArgument() throws Exception {
        this.interceptor.beforeResolveArgument(null, null, null);
    }

    @Test
    public void testAfterResolveArgument() throws Exception {
        this.interceptor.afterResolveArgument(null, null, null, null);
    }
}